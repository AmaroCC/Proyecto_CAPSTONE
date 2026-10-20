package cl.rincon.dualista.repositorio;

import cl.rincon.dualista.modelo.RespuestaCartaDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartaRepository extends JpaRepository<RespuestaCartaDTO, Long> {
    List<RespuestaCartaDTO> findAllByOrderByIdDesc();
}