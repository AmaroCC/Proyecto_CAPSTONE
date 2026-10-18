package cl.rincon.dualista.controlador;

import cl.rincon.dualista.modelo.RespuestaCartaDTO;
import cl.rincon.dualista.servicio.EscaneoTcgService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DualistaControlador {

    private final EscaneoTcgService escaneoService;
    private final List<RespuestaCartaDTO> catalogo = new ArrayList<>();

    public DualistaControlador(EscaneoTcgService escaneoService) {
        this.escaneoService = escaneoService;
        double dolarInicial = 950.0;

        catalogo.add(new RespuestaCartaDTO(
                "Charizard ex - 199/165 (151)", "Pokémon TCG", "Special Illustration Rare", "Near Mint (96%)",
                115.50, dolarInicial, (int) (115.50 * dolarInicial),
                "[https://assets.tcgdex.net/en/sv/sv03.5/199/high.webp](https://assets.tcgdex.net/en/sv/sv03.5/199/high.webp)"));

        catalogo.add(new RespuestaCartaDTO(
                "Monkey D. Luffy - OP05-119 (Gear 5)", "One Piece TCG", "Manga Alternate Art (SEC)", "Mint (98%)",
                420.00, dolarInicial, (int) (420.00 * dolarInicial),
                "[https://en.onepiece-cardgame.com/images/cardlist/card/OP05-119_p2.png](https://en.onepiece-cardgame.com/images/cardlist/card/OP05-119_p2.png)"));
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String usuario,
                                @RequestParam String password,
                                HttpSession session,
                                Model model) {
        if ("admin".equalsIgnoreCase(usuario) && "admin123".equals(password)) {
            session.setAttribute("usuario", "Administrador");
            session.setAttribute("rol", "ADMIN");
            return "redirect:/admin";
        } else if ("cliente".equalsIgnoreCase(usuario) && "1234".equals(password)) {
            session.setAttribute("usuario", "Duelista_CL");
            session.setAttribute("rol", "CLIENTE");
            return "redirect:/";
        }
        model.addAttribute("error", "Credenciales incorrectas. Prueba con admin/admin123 o cliente/1234");
        return "login";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/")
    public String inicio(HttpSession session, Model model) {
        model.addAttribute("usuario", session.getAttribute("usuario"));
        model.addAttribute("rol", session.getAttribute("rol"));
        model.addAttribute("cartas", catalogo);
        return "index";
    }

    @GetMapping("/publicar")
    public String publicar(HttpSession session, Model model) {
        model.addAttribute("usuario", session.getAttribute("usuario"));
        model.addAttribute("rol", session.getAttribute("rol"));
        return "publicar";
    }

    @GetMapping("/admin")
    public String panelAdmin(HttpSession session, Model model) {
        String rol = (String) session.getAttribute("rol");
        if (!"ADMIN".equals(rol)) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", session.getAttribute("usuario"));
        model.addAttribute("cartas", catalogo);
        return "admin";
    }

    @GetMapping("/api/cartas/buscar")
    @ResponseBody
    public List<RespuestaCartaDTO> buscarCartas(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "TODOS") String juego) {
        return catalogo.stream()
                .filter(c -> (juego.equals("TODOS") || c.getJuego().equalsIgnoreCase(juego)))
                .filter(c -> c.getNombre().toLowerCase().contains(q.toLowerCase()))
                .collect(Collectors.toList());
    }

    // ENDPOINT REAL DE ESCANEO CON IA + MINDICADOR.CL + TCG API
    @PostMapping("/api/cartas/escanear")
    @ResponseBody
    public ResponseEntity<?> escanearCarta(
            @RequestParam("imagen") MultipartFile archivo,
            @RequestParam(value = "juego", defaultValue = "Auto-detectar") String juego) {
        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo de imagen está vacío."));
        }
        try {
            RespuestaCartaDTO detectada = escaneoService.escanearImagenReal(archivo, juego);
            catalogo.add(0, detectada);
            return ResponseEntity.ok(detectada);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}