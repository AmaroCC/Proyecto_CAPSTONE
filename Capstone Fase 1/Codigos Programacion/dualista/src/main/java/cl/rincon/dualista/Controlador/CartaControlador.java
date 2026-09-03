package cl.rincon.dualista.controlador;
import cl.rincon.dualista.modelo.RespuestaCartaDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Controller
public class CartaControlador {

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

   @GetMapping("/publicar")
@ResponseBody
public String publicar() {
    return "<h1>Prueba exitosa: el controlador responde directo</h1>";
}

    @PostMapping("/api/cartas/escanear")
    @ResponseBody
    public ResponseEntity<RespuestaCartaDTO> escanearCarta(@RequestParam("imagen") MultipartFile archivo) {
        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String imagenEnBase64 = "data:" + archivo.getContentType() + ";base64," +
                    Base64.getEncoder().encodeToString(archivo.getBytes());

            RespuestaCartaDTO respuesta = new RespuestaCartaDTO(
                    "Charizard ex - Special Illustration Rare",
                    "Near Mint (95%)",
                    97.63,
                    921.85,
                    90000,
                    imagenEnBase64);

            return ResponseEntity.ok(respuesta);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}