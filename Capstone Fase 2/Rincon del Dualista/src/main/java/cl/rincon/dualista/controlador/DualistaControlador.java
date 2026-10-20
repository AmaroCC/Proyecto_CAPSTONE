package cl.rincon.dualista.controlador;

import cl.rincon.dualista.modelo.RespuestaCartaDTO;
import cl.rincon.dualista.modelo.Usuario;
import cl.rincon.dualista.repositorio.CartaRepository;
import cl.rincon.dualista.repositorio.UsuarioRepository;
import cl.rincon.dualista.servicio.EmailService;
import cl.rincon.dualista.servicio.EscaneoTcgService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class DualistaControlador {

    private final EscaneoTcgService escaneoService;
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepo;
    private final CartaRepository cartaRepo;

    public DualistaControlador(EscaneoTcgService escaneoService,
                               EmailService emailService,
                               UsuarioRepository usuarioRepo,
                               CartaRepository cartaRepo) {
        this.escaneoService = escaneoService;
        this.emailService = emailService;
        this.usuarioRepo = usuarioRepo;
        this.cartaRepo = cartaRepo;
    }

    @PostConstruct
    public void inicializarDatosBase() {
        if (usuarioRepo.count() == 0) {
            usuarioRepo.save(new Usuario("admin", "admin@dualista.cl", "admin123", "ADMIN", true, null));
            usuarioRepo.save(new Usuario("cliente", "cliente@dualista.cl", "1234", "CLIENTE", true, null));
        }

        if (cartaRepo.count() == 0) {
            double d = 950.0;
            cartaRepo.save(new RespuestaCartaDTO("Charizard ex - 199/165 (Pokémon 151)", "Pokémon TCG", "Special Illustration Rare", "Near Mint (96%)", 125.00, d, (int)(125.00 * d), "https://assets.tcgdex.net/en/sv/sv03.5/199/high.webp"));
            cartaRepo.save(new RespuestaCartaDTO("Umbreon VMAX - 215/203 (Evolving Skies)", "Pokémon TCG", "Alt Art Secret Rare", "Mint (98%)", 920.00, d, (int)(920.00 * d), "https://assets.tcgdex.net/en/swsh/swsh7/215/high.webp"));
        }
    }

    @GetMapping("/login")
    public String mostrarLogin(HttpSession session) {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String usuario,
                                @RequestParam String password,
                                HttpSession session,
                                Model model) {
        Optional<Usuario> userOpt = usuarioRepo.findByUsernameIgnoreCase(usuario.trim());

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            Usuario u = userOpt.get();
            session.setAttribute("usuario", u.getUsername());
            session.setAttribute("rol", u.getRol());
            session.setMaxInactiveInterval(30 * 60);
            return "redirect:/";
        }
        model.addAttribute("error", "Nombre de usuario o contraseña incorrectos.");
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro() { return "registro"; }

    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam String email,
                                   @RequestParam String username,
                                   @RequestParam String password,
                                   @RequestParam String rol,
                                   @RequestParam(required = false) String codigoAdmin,
                                   Model model) {
        if (usuarioRepo.existsByUsernameIgnoreCase(username.trim())) {
            model.addAttribute("error", "Ese nombre de usuario ya existe.");
            return "registro";
        }
        if ("ADMIN".equalsIgnoreCase(rol) && !"DUALISTA2026".equals(codigoAdmin)) {
            model.addAttribute("error", "Código de Administrador incorrecto (Usa: DUALISTA2026).");
            return "registro";
        }

        // REGISTRO DIRECTO SIN NECESIDAD DE CONFIRMACIÓN POR CORREO
        Usuario nuevo = new Usuario(username.trim(), email.trim(), password, rol.toUpperCase(), true, null);
        usuarioRepo.save(nuevo);

        model.addAttribute("exito", "✅ ¡Cuenta creada con éxito! Ya puedes iniciar sesión.");
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
        model.addAttribute("cartas", cartaRepo.findAllByOrderByIdDesc());
        if ("ADMIN".equals(session.getAttribute("rol"))) {
            model.addAttribute("usuarios", usuarioRepo.findAll());
        }
        return "index";
    }

    @GetMapping("/publicar")
    public String publicar(HttpSession session, Model model) {
        model.addAttribute("usuario", session.getAttribute("usuario"));
        model.addAttribute("rol", session.getAttribute("rol"));
        return "publicar";
    }

    @PostMapping("/admin/cartas/agregar")
    public String agregarCartaAdmin(@RequestParam String nombre,
                                    @RequestParam String juego,
                                    @RequestParam String rareza,
                                    @RequestParam String estado,
                                    @RequestParam String precioUSD,
                                    @RequestParam String imagenUrl,
                                    HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }
        double dolarHoy = escaneoService.obtenerDolarActualChile();
        double precioVal = escaneoService.limpiarPrecioUSD(precioUSD);
        int precioCLP = (int) Math.round(precioVal * dolarHoy);
        
        RespuestaCartaDTO nueva = new RespuestaCartaDTO(nombre, juego, rareza, estado, precioVal, dolarHoy, precioCLP, imagenUrl);
        cartaRepo.save(nueva);
        return "redirect:/";
    }

    @PostMapping("/admin/cartas/eliminar/{id}")
    public String eliminarCartaAdmin(@PathVariable Long id, HttpSession session) {
        if ("ADMIN".equals(session.getAttribute("rol"))) {
            cartaRepo.deleteById(id);
        }
        return "redirect:/";
    }

    @PostMapping("/admin/usuarios/eliminar/{id}")
    public String eliminarUsuarioAdmin(@PathVariable Long id, HttpSession session) {
        if ("ADMIN".equals(session.getAttribute("rol"))) {
            usuarioRepo.deleteById(id);
        }
        return "redirect:/";
    }

    @GetMapping("/api/cartas/buscar")
    @ResponseBody
    public List<RespuestaCartaDTO> buscarCartas(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "TODOS") String juego) {
        return cartaRepo.findAllByOrderByIdDesc().stream()
                .filter(c -> (juego.equals("TODOS") || c.getJuego().equalsIgnoreCase(juego)))
                .filter(c -> c.getNombre().toLowerCase().contains(q.toLowerCase()) ||
                             c.getRareza().toLowerCase().contains(q.toLowerCase()))
                .collect(Collectors.toList());
    }

    @PostMapping("/api/cartas/escanear")
    @ResponseBody
    public ResponseEntity<?> escanearCarta(
            @RequestParam("imagen") MultipartFile archivo,
            @RequestParam(value = "juego", defaultValue = "Auto-detectar") String juego) {
        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Archivo vacío."));
        }
        try {
            RespuestaCartaDTO detectada = escaneoService.escanearImagenReal(archivo, juego);
            cartaRepo.save(detectada);
            return ResponseEntity.ok(detectada);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}