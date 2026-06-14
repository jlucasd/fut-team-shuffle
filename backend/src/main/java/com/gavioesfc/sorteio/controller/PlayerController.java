package com.gavioesfc.sorteio.controller;

import com.gavioesfc.sorteio.dto.PageResponse;
import com.gavioesfc.sorteio.dto.PlayerCreateRequest;
import com.gavioesfc.sorteio.dto.PlayerResponse;
import com.gavioesfc.sorteio.dto.PlayerUpdateRequest;
import com.gavioesfc.sorteio.model.enums.Position;
import com.gavioesfc.sorteio.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jogadores")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<PlayerResponse>> list(
            @RequestParam(required = false) Position posicao,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<PlayerResponse> response = playerService.list(posicao, ativo, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> getById(@PathVariable Long id) {
        PlayerResponse response = playerService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PlayerResponse> create(@Valid @RequestBody PlayerCreateRequest request) {
        PlayerResponse response = playerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PlayerUpdateRequest request) {
        PlayerResponse response = playerService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PlayerResponse> toggleStatus(@PathVariable Long id) {
        PlayerResponse response = playerService.toggleStatus(id);
        return ResponseEntity.ok(response);
    }
}
