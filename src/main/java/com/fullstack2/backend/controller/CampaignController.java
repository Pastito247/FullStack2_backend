package com.fullstack2.backend.controller;

import com.fullstack2.backend.dto.CampaignCreateRequest;
import com.fullstack2.backend.dto.CampaignPlayerResponse;
import com.fullstack2.backend.dto.CampaignResponse;
import com.fullstack2.backend.dto.CampaignUpdateRequest;
import com.fullstack2.backend.dto.PlayerSummaryResponse;
import com.fullstack2.backend.entity.Campaign;
import com.fullstack2.backend.service.CampaignService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/campaigns")
@CrossOrigin(origins = {
                "http://localhost:5173", // front dev
                "https://forjayacero.netlify.app" // cuando subas el front pon la URL real (Netlify/Vercel/etc)
})
public class CampaignController {

        private final CampaignService campaignService;

        public CampaignController(CampaignService campaignService) {
                this.campaignService = campaignService;
        }

        // Crear campaña (DM / ADMIN)
        @PreAuthorize("hasRole('DM') or hasRole('ADMIN')")
        @PostMapping
        public ResponseEntity<CampaignResponse> createCampaign(
                        @RequestBody CampaignCreateRequest request) {

                Campaign campaign = campaignService.createCampaign(
                                request.getName(),
                                request.getDescription(),
                                request.getImageUrl() // usa la portada que viene del front
                );

                CampaignResponse response = CampaignResponse.builder()
                                .id(campaign.getId())
                                .name(campaign.getName())
                                .description(campaign.getDescription())
                                .imageUrl(campaign.getImageUrl())
                                .inviteCode(campaign.getInviteCode())
                                .dmUsername(campaign.getDm().getUsername())
                                .build();

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        // Listar campañas donde soy DM
        @GetMapping("/mine")
        public ResponseEntity<List<CampaignResponse>> getMyCampaigns() {
                List<CampaignResponse> list = campaignService.getMyCampaignsAsDm().stream()
                                .map(c -> CampaignResponse.builder()
                                                .id(c.getId())
                                                .name(c.getName())
                                                .description(c.getDescription())
                                                .imageUrl(c.getImageUrl())
                                                .inviteCode(c.getInviteCode())
                                                .dmUsername(c.getDm().getUsername())
                                                .build())
                                .toList();

                return ResponseEntity.ok(list);
        }

        // Campañas donde participo como Player
        @GetMapping("/joined")
        public ResponseEntity<List<CampaignResponse>> getJoinedCampaigns() {
                List<CampaignResponse> list = campaignService.getMyCampaignsAsPlayer().stream()
                                .map(c -> CampaignResponse.builder()
                                                .id(c.getId())
                                                .name(c.getName())
                                                .description(c.getDescription())
                                                .imageUrl(c.getImageUrl()) // 🔥 antes no lo estabas mandando
                                                .inviteCode(c.getInviteCode())
                                                .dmUsername(c.getDm().getUsername())
                                                .build())
                                .toList();

                return ResponseEntity.ok(list);
        }

        // Obtener campaña por id
        @GetMapping("/{id}")
        public ResponseEntity<CampaignResponse> getById(@PathVariable Long id) {
                Campaign c = campaignService.getById(id);

                CampaignResponse response = CampaignResponse.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .description(c.getDescription())
                                .imageUrl(c.getImageUrl()) // 🔥 añadido
                                .inviteCode(c.getInviteCode())
                                .dmUsername(c.getDm().getUsername())
                                .build();

                return ResponseEntity.ok(response);
        }

        // Player se une a campaña con inviteCode
        @PostMapping("/join/{inviteCode}")
        public ResponseEntity<CampaignResponse> joinCampaign(@PathVariable String inviteCode) {
                Campaign campaign = campaignService.joinCampaignByInviteCode(inviteCode);

                CampaignResponse response = CampaignResponse.builder()
                                .id(campaign.getId())
                                .name(campaign.getName())
                                .description(campaign.getDescription())
                                .imageUrl(campaign.getImageUrl()) // 🔥 añadido
                                .inviteCode(campaign.getInviteCode())
                                .dmUsername(campaign.getDm().getUsername())
                                .build();

                return ResponseEntity.ok(response);
        }

        @PreAuthorize("hasRole('DM') or hasRole('ADMIN')")
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
                campaignService.deleteCampaign(id);
                return ResponseEntity.noContent().build();
        }

        // Jugadores unidos a una campaña
        @GetMapping("/{id}/players")
        @PreAuthorize("hasAnyRole('DM','ADMIN')")
        public ResponseEntity<List<PlayerSummaryResponse>> getPlayersOfCampaign(@PathVariable Long id) {
                return ResponseEntity.ok(campaignService.getPlayersOfCampaign(id));
        }

        // Actualizar campaña (nombre/desc/imagen)
        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('DM','ADMIN')")
        public ResponseEntity<CampaignResponse> updateCampaign(
                        @PathVariable Long id,
                        @RequestBody CampaignUpdateRequest request) {
                Campaign updated = campaignService.updateCampaign(id, request);

                CampaignResponse resp = CampaignResponse.builder()
                                .id(updated.getId())
                                .name(updated.getName())
                                .description(updated.getDescription())
                                .imageUrl(updated.getImageUrl())
                                .inviteCode(updated.getInviteCode())
                                .dmUsername(updated.getDm().getUsername())
                                .build();

                return ResponseEntity.ok(resp);
        }
}
