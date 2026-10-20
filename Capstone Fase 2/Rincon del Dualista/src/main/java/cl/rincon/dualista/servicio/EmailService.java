package cl.rincon.dualista.servicio;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    public void enviarCorreoConfirmacion(String destino, String username, String token) {
        String link = "http://localhost:8080/confirmar?token=" + token;
        System.out.println("\n==================================================================");
        System.out.println("📧 ENLACE DE CONFIRMACIÓN PARA: " + destino);
        System.out.println("👤 Usuario: " + username);
        System.out.println("🔗 Haz clic aquí para activar tu cuenta:");
        System.out.println(link);
        System.out.println("==================================================================\n");
    }
}