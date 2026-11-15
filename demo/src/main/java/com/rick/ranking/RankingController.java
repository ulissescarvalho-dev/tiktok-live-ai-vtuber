package com.rick.ranking;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final EspectadorRepository repository;

    public RankingController(EspectadorRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/top3")
    public List<Espectador> obterTop3() {
        return repository.findTop3ByOrderByPontosDesc();
    }
}
