package com.fullstack2.backend.service;

import com.fullstack2.backend.dto.CharacterCreateRequest;
import com.fullstack2.backend.dto.CharacterResponse;
import com.fullstack2.backend.entity.*;
import com.fullstack2.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    // Crear personaje (solo DM)
    @Transactional
    public CharacterResponse createCharacter(CharacterCreateRequest request, String dmUsername) {

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if (!campaign.getDm().getUsername().equals(dmUsername)) {
            throw new RuntimeException("Only the DM owner of the campaign can create characters");
        }

        User assignedPlayer = null;
        if (request.getPlayerUsername() != null) {
            assignedPlayer = userRepository.findByUsername(request.getPlayerUsername())
                    .orElseThrow(() -> new RuntimeException("Player not found"));
        }

        CharacterEntity character = CharacterEntity.builder()
                .name(request.getName())
                .dndClass(request.getDndClass())
                .race(request.getRace())
                .level(request.getLevel())
                .npc(request.isNpc())
                .imageUrl(request.getImageUrl())
                .pp(Optional.ofNullable(request.getPp()).orElse(0))
                .gp(Optional.ofNullable(request.getGp()).orElse(0))
                .ep(Optional.ofNullable(request.getEp()).orElse(0))
                .sp(Optional.ofNullable(request.getSp()).orElse(0))
                .cp(Optional.ofNullable(request.getCp()).orElse(0))
                .campaign(campaign)
                .player(assignedPlayer)
                .build();

        CharacterEntity saved = characterRepository.save(character);

        return toDto(saved);
    }

    // Player: editar nombre e imagen
    @Transactional
    public CharacterResponse editCharacter(Long id, String username, String newName, String newImage) {
        CharacterEntity character = characterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        if (character.getPlayer() == null || !character.getPlayer().getUsername().equals(username)) {
            throw new RuntimeException("You do not own this character");
        }

        if (newName != null && !newName.isBlank()) {
            character.setName(newName);
        }

        if (newImage != null && !newImage.isBlank()) {
            character.setImageUrl(newImage);
        }

        return toDto(characterRepository.save(character));
    }

    // Player ve su personaje
    public CharacterResponse getMyCharacter(String username) {
        CharacterEntity character = characterRepository.findByPlayerUsername(username)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("You do not have a character assigned"));

        return toDto(character);
    }

    // DM asigna un personaje a un player
    @Transactional
    public CharacterResponse assignCharacterToPlayer(Long characterId, String targetUsername, String dmUsername) {

        CharacterEntity character = characterRepository.findById(characterId)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        if (!character.getCampaign().getDm().getUsername().equals(dmUsername)) {
            throw new RuntimeException("Only the DM can assign players to characters");
        }

        User player = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        character.setPlayer(player);

        return toDto(characterRepository.save(character));
    }

    // Convertir a DTO
    private CharacterResponse toDto(CharacterEntity c) {
        return CharacterResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .dndClass(c.getDndClass())
                .race(c.getRace())
                .level(c.getLevel())
                .npc(c.isNpc())
                .imageUrl(c.getImageUrl())
                .pp(c.getPp())
                .gp(c.getGp())
                .ep(c.getEp())
                .sp(c.getSp())
                .cp(c.getCp())
                .campaignId(c.getCampaign().getId())
                .campaignName(c.getCampaign().getName())
                .playerUsername(c.getPlayer() != null ? c.getPlayer().getUsername() : null)
                .build();
    }
}
