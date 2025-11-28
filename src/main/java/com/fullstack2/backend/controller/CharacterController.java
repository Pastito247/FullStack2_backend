package com.fullstack2.backend.controller;

import com.fullstack2.backend.dto.CharacterCreateRequest;
import com.fullstack2.backend.dto.CharacterResponse;
import com.fullstack2.backend.service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    // Crear personaje (solo DM)
    @PostMapping("/create")
    public ResponseEntity<CharacterResponse> create(@RequestBody CharacterCreateRequest request,
                                                    @RequestHeader("username") String dmUsername) {
        return ResponseEntity.ok(characterService.createCharacter(request, dmUsername));
    }

    // Player edita nombre e imagen
    @PutMapping("/{id}/edit")
    public ResponseEntity<CharacterResponse> edit(@PathVariable Long id,
                                                  @RequestHeader("username") String username,
                                                  @RequestParam(required = false) String name,
                                                  @RequestParam(required = false) String imageUrl) {

        return ResponseEntity.ok(characterService.editCharacter(id, username, name, imageUrl));
    }

    // Player ve su personaje
    @GetMapping("/me")
    public ResponseEntity<CharacterResponse> myCharacter(@RequestHeader("username") String username) {
        return ResponseEntity.ok(characterService.getMyCharacter(username));
    }

    // DM asigna personaje a player
    @PutMapping("/{id}/assign")
    public ResponseEntity<CharacterResponse> assign(@PathVariable Long id,
                                                    @RequestHeader("username") String dmUsername,
                                                    @RequestParam String targetUsername) {

        return ResponseEntity.ok(characterService.assignCharacterToPlayer(id, targetUsername, dmUsername));
    }
}
