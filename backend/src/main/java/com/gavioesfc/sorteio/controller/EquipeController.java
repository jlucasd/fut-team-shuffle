package com.gavioesfc.sorteio.controller;

import com.gavioesfc.sorteio.dto.EquipeRequest;
import com.gavioesfc.sorteio.dto.EquipeResponse;
import com.gavioesfc.sorteio.service.EquipeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipes")
public class EquipeController {

    private final EquipeService equipeService;

    public EquipeController(EquipeService equipeService) {
        this.equipeService = equipeService;
    }

    @GetMapping
    public ResponseEntity<List<EquipeResponse>> list() {
        return ResponseEntity.ok(equipeService.list());
    }

    @PostMapping
    public ResponseEntity<EquipeResponse> create(@Valid @RequestBody EquipeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipeResponse> update(@PathVariable Long id, @Valid @RequestBody EquipeRequest request) {
        return ResponseEntity.ok(equipeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
