package com.rick.ranking;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EspectadorRepository extends JpaRepository<Espectador, String> {
    List<Espectador> findTop3ByOrderByPontosDesc();
}
