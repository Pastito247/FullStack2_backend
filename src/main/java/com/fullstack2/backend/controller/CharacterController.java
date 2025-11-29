package com.fullstack2.backend.controller;

import com.fullstack2.backend.dto.*;
import com.fullstack2.backend.service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:5173",      // front dev
        "https://TU-FRONT-DEPLOY"     // cuando subas el front pon la URL real (Netlify/Vercel/etc)
})
public class CharacterController {

    private final CharacterService characterService;

    // ==================================
    // Crear personaje en una campaña
    // (solo DM / ADMIN, según el token)
    // ==================================
    @PostMapping("/campaign/{campaignId}")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<CharacterResponse> create(
            @PathVariable Long campaignId,
            @RequestBody CharacterCreateRequest request
    ) {
        request.setCampaignId(campaignId);
        CharacterResponse response = characterService.createCharacter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================================
    // Player edita NOMBRE e IMAGEN
    // (usa CharacterEditRequest como JSON)
    // ==================================
    @PutMapping("/{id}/edit")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<CharacterResponse> edit(
            @PathVariable Long id,
            @RequestBody CharacterEditRequest request
    ) {
        CharacterResponse response = characterService.editCharacter(id, request);
        return ResponseEntity.ok(response);
    }

    // ==================================
    // Listar personajes de una campaña
    // (DM o Player, de momento abierto)
    // ==================================
    @GetMapping("/campaign/{campaignId}")
    @PreAuthorize("hasAnyRole('DM','PLAYER','ADMIN')")
    public ResponseEntity<List<CharacterResponse>> getByCampaign(@PathVariable Long campaignId) {
        return ResponseEntity.ok(characterService.getCharactersByCampaign(campaignId));
    }

    // ==================================
    // Player ve su personaje (MiPersonaje)
    // ==================================
    @GetMapping("/me")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<CharacterResponse> myCharacter() {
        return ResponseEntity.ok(characterService.getMyCharacter());
    }

    // ==================================
    // Obtener personaje por ID (DetallePersonaje)
    // ==================================
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DM','PLAYER','ADMIN')")
    public ResponseEntity<CharacterResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(characterService.getById(id));
    }

    // ==================================
    // DM / ADMIN actualiza dinero del PJ
    // (DetallePersonaje, botón "Editar dinero")
    // ==================================
    @PutMapping("/{id}/admin-update")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<CharacterResponse> updateByDm(
            @PathVariable Long id,
            @RequestBody CharacterAdminUpdateRequest request
    ) {
        CharacterResponse response = characterService.updateByDm(id, request);
        return ResponseEntity.ok(response);
    }

    // ==================================
    // DM / ADMIN asigna PJ a player
    // (DetallePersonaje, form "Asignar a jugador")
    // ==================================
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<CharacterResponse> assign(
            @PathVariable Long id,
            @RequestParam String targetUsername
    ) {
        CharacterResponse response = characterService.assignCharacterToPlayer(id, targetUsername);
        return ResponseEntity.ok(response);
    }

    // ==================================
    // DM / ADMIN agrega item al inventario
    // (para cuando implementemos esa UI)
    // ==================================
    @PostMapping("/{id}/inventory")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<CharacterResponse> addItemToInventory(
            @PathVariable Long id,
            @RequestBody CharacterInventoryUpdateRequest request
    ) {
        CharacterResponse response = characterService.addItemToInventory(
                id,
                request.getItemId(),
                request.getQuantity()
        );
        return ResponseEntity.ok(response);
    }

    // ==================================
    // DM / ADMIN quita item del inventario
    // ==================================
    @DeleteMapping("/{id}/inventory")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<CharacterResponse> removeItemFromInventory(
            @PathVariable Long id,
            @RequestBody CharacterInventoryUpdateRequest request
    ) {
        CharacterResponse response = characterService.removeItemFromInventory(
                id,
                request.getItemId(),
                request.getQuantity()
        );
        return ResponseEntity.ok(response);
    }
}
