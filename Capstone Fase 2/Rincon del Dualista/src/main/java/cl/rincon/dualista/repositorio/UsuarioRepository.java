package cl.rincon.dualista.repositorio;

import cl.rincon.dualista.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsernameIgnoreCase(String username);
    Optional<Usuario> findByEmailIgnoreCase(String email);
    Optional<Usuario> findByTokenConfirmacion(String token);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
}