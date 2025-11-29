package com.fullstack2.backend.controller;

import com.fullstack2.backend.dto.CharacterCreateRequest;
import com.fullstack2.backend.dto.CharacterResponse;
import com.fullstack2.backend.service.CharacterService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    // Crear personaje en una campaña (solo DM, según el token)
    @PostMapping("/campaign/{campaignId}")
    public ResponseEntity<CharacterResponse> create(@PathVariable Long campaignId,
            @RequestBody CharacterCreateRequest request) {
        request.setCampaignId(campaignId);
        return ResponseEntity.ok(characterService.createCharacter(request));
    }

    // Player edita nombre e imagen (según el token)
    @PutMapping("/{id}/edit")
    public ResponseEntity<CharacterResponse> edit(@PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String imageUrl) {

        return ResponseEntity.ok(characterService.editCharacter(id, name, imageUrl));
    }

    // Listar personajes de una campaña
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<CharacterResponse>> getByCampaign(@PathVariable Long campaignId) {
        return ResponseEntity.ok(characterService.getCharactersByCampaign(campaignId));
    }

    // Player ve su personaje (según el token)
    @GetMapping("/me")
    public ResponseEntity<CharacterResponse> myCharacter() {
        return ResponseEntity.ok(characterService.getMyCharacter());
    }

    // DM asigna personaje a player (según el token)
    @PutMapping("/{id}/assign")
    public ResponseEntity<CharacterResponse> assign(@PathVariable Long id,
            @RequestParam String targetUsername) {

        return ResponseEntity.ok(characterService.assignCharacterToPlayer(id, targetUsername));
    }
}
