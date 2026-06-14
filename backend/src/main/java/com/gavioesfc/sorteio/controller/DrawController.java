package com.gavioesfc.sorteio.controller;

import com.gavioesfc.sorteio.dto.DrawRequest;
import com.gavioesfc.sorteio.dto.DrawResponse;
import com.gavioesfc.sorteio.dto.PageResponse;
import com.gavioesfc.sorteio.dto.TeamEditRequest;
import com.gavioesfc.sorteio.service.DrawService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sorteio")
public class DrawController {

    private final DrawService drawService;

    public DrawController(DrawService drawService) {
        this.drawService = drawService;
    }

    @PostMapping
    public ResponseEntity<DrawResponse> executeDraw(@Valid @RequestBody DrawRequest request) {
        DrawResponse response = drawService.executeDraw(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/historico")
    public ResponseEntity<PageResponse<DrawResponse>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<DrawResponse> response = drawService.getHistory(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DrawResponse> getById(@PathVariable Long id) {
        DrawResponse response = drawService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/times")
    public ResponseEntity<DrawResponse> updateTeams(
            @PathVariable Long id,
            @RequestBody TeamEditRequest request) {
        DrawResponse response = drawService.updateTeams(id, request);
        return ResponseEntity.ok(response);
    }
}
