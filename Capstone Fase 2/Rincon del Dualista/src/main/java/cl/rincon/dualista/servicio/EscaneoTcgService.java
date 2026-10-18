package cl.rincon.dualista.servicio;

import cl.rincon.dualista.modelo.RespuestaCartaDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class EscaneoTcgService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    // 1. Obtener el valor REAL del dólar en Chile hoy desde mindicador.cl
    public double obtenerDolarActualChile() {
        try {
            String url = "https://mindicador.cl/api/dolar";
            String respuesta = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(respuesta);
            return root.path("serie").get(0).path("valor").asDouble(950.0);
        } catch (Exception e) {
            return 950.0; // Respaldo si mindicador.cl tarda en responder
        }
    }

    // 2. Buscar precio real en API oficial Pokémon TCG (TCGPlayer) si está disponible
    public double consultarPrecioRealPokemon(String nombreLimpio, double precioEstimadoIA) {
        try {
            String query = URLEncoder.encode("name:\"" + nombreLimpio + "\"", StandardCharsets.UTF_8);
            String url = "https://api.pokemontcg.io/v2/cards?q=" + query + "&pageSize=1";
            String json = restTemplate.getForObject(url, String.class);
            JsonNode data = mapper.readTree(json).path("data");
            if (data.isArray() && !data.isEmpty()) {
                JsonNode prices = data.get(0).path("tcgplayer").path("prices");
                Iterator<JsonNode> tiposPrecio = prices.elements();
                if (tiposPrecio.hasNext()) {
                    double market = tiposPrecio.next().path("market").asDouble(0.0);
                    if (market > 0) return market;
                }
            }
        } catch (Exception ignored) {}
        return precioEstimadoIA;
    }

    // 3. Escanear la imagen real con Gemini Vision + Precio de Mercado + Dólar CLP
    public RespuestaCartaDTO escanearImagenReal(MultipartFile archivo, String juegoSeleccionado) throws Exception {
        String mimeType = archivo.getContentType() != null ? archivo.getContentType() : "image/jpeg";
        String base64Puro = Base64.getEncoder().encodeToString(archivo.getBytes());
        String dataImageUrl = "data:" + mimeType + ";base64," + base64Puro;

        double dolarHoy = obtenerDolarActualChile();

        // Si el usuario aún no pega su API Key, le avisamos claramente o usamos detección básica
        if (geminiApiKey == null || geminiApiKey.isBlank() || geminiApiKey.contains("PEGA_AQUI")) {
            throw new IllegalStateException("Falta configurar gemini.api.key en application.properties");
        }

        String urlGemini = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=" + geminiApiKey.trim();

        String prompt = "Analiza esta imagen de una carta TCG. Identifica con exactitud la carta y devuelve UNICAMENTE un objeto JSON válido (sin bloques markdown ```json) con esta estructura exacta: " +
                "{\"nombre\": \"Nombre completo de la carta + código de colección (ej: Charizard ex 199/165 o Monkey D. Luffy OP05-119)\", " +
                "\"nombreBusqueda\": \"Solo el nombre del personaje sin sufijos (ej: Charizard o Monkey D. Luffy)\", " +
                "\"juego\": \"Pokémon TCG o One Piece TCG (o el TCG que corresponda)\", " +
                "\"rareza\": \"Tipo de carta y rareza exacta (ej: Special Illustration Rare, Secret Rare, Leader, Holo)\", " +
                "\"estado\": \"Estado visual estimado viendo bordes y centrado (ej: Near Mint 95%, Mint 98%, Lightly Played 85%)\", " +
                "\"precioUSD\": valor numérico en dólares del precio real de mercado actual en TCGPlayer/PriceCharting para esta carta específica (ej: 45.50)}";

        Map<String, Object> inlineData = Map.of("mime_type", mimeType, "data", base64Puro);
        Map<String, Object> partText = Map.of("text", prompt);
        Map<String, Object> partImage = Map.of("inline_data", inlineData);
        Map<String, Object> content = Map.of("parts", List.of(partText, partImage));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(urlGemini, entity, String.class);
        JsonNode root = mapper.readTree(response.getBody());

        String textoRespuesta = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        // Limpiar posible formato markdown si el modelo lo incluye
        textoRespuesta = textoRespuesta.replace("```json", "").replace("```", "").trim();
        JsonNode datosCarta = mapper.readTree(textoRespuesta);

        String nombre = datosCarta.path("nombre").asText("Carta TCG Detectada");
        String nombreBusqueda = datosCarta.path("nombreBusqueda").asText("Pikachu");
        String juego = datosCarta.path("juego").asText(juegoSeleccionado);
        String rareza = datosCarta.path("rareza").asText("Rare");
        String estado = datosCarta.path("estado").asText("Near Mint (95%)");
        double precioIA = datosCarta.path("precioUSD").asDouble(15.00);

        // Si es Pokémon, contrastamos además con la API oficial de PokémonTCG.io (TCGPlayer)
        double precioFinalUSD = precioIA;
        if (juego.toLowerCase().contains("pok")) {
            precioFinalUSD = consultarPrecioRealPokemon(nombreBusqueda, precioIA);
        }

        int precioCLP = (int) Math.round(precioFinalUSD * dolarHoy);

        return new RespuestaCartaDTO(
                nombre,
                juego,
                rareza,
                estado,
                Math.round(precioFinalUSD * 100.0) / 100.0,
                dolarHoy,
                precioCLP,
                dataImageUrl
        );
    }
}