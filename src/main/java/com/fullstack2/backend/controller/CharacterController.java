package com.fullstack2.backend.controller;

import com.fullstack2.backend.dto.CharacterCreateRequest;
import com.fullstack2.backend.dto.CharacterResponse;
import com.fullstack2.backend.service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
@CrossOrigin(origins = {
        "http://localhost:5173",      // front en dev
        "https://TU-FRONT-DEPLOY"     // reemplaza por la URL real cuando lo subas
})
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    // Crear personaje en una campaña (solo DM, según el token)
    @PostMapping("/campaign/{campaignId}")
    @PreAuthorize("hasRole('DM')")
    public ResponseEntity<CharacterResponse> create(
            @PathVariable Long campaignId,
            @RequestBody CharacterCreateRequest request
    ) {
        request.setCampaignId(campaignId);
        CharacterResponse response = characterService.createCharacter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Player edita nombre e imagen (según el token)
    @PutMapping("/{id}/edit")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<CharacterResponse> edit(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String imageUrl
    ) {
        return ResponseEntity.ok(characterService.editCharacter(id, name, imageUrl));
    }

    // Listar personajes de una campaña (DM o Player dentro de la campaña)
    @GetMapping("/campaign/{campaignId}")
    @PreAuthorize("hasAnyRole('DM','PLAYER')")
    public ResponseEntity<List<CharacterResponse>> getByCampaign(@PathVariable Long campaignId) {
        return ResponseEntity.ok(characterService.getCharactersByCampaign(campaignId));
    }

    // Player ve su personaje (según el token)
    @GetMapping("/me")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<CharacterResponse> myCharacter() {
        return ResponseEntity.ok(characterService.getMyCharacter());
    }

    // DM asigna personaje a player (según el token)
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('DM')")
    public ResponseEntity<CharacterResponse> assign(
            @PathVariable Long id,
            @RequestParam String targetUsername
    ) {
        return ResponseEntity.ok(characterService.assignCharacterToPlayer(id, targetUsername));
    }
}
