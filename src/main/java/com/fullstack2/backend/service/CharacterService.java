package com.fullstack2.backend.service;

import com.fullstack2.backend.dto.*;
import com.fullstack2.backend.entity.Campaign;
import com.fullstack2.backend.entity.CharacterEntity;
import com.fullstack2.backend.entity.CharacterItem;
import com.fullstack2.backend.entity.Item;
import com.fullstack2.backend.entity.User;
import com.fullstack2.backend.repository.CampaignRepository;
import com.fullstack2.backend.repository.CharacterItemRepository;
import com.fullstack2.backend.repository.CharacterRepository;
import com.fullstack2.backend.repository.ItemRepository;
import com.fullstack2.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CharacterService {

        private final CharacterRepository characterRepository;
        private final CampaignRepository campaignRepository;
        private final UserRepository userRepository;
        private final ItemRepository itemRepository;
        private final CharacterItemRepository characterItemRepository;

        // ==========================
        // Crear personaje (solo DM)
        // ==========================
        @Transactional
        public CharacterResponse createCharacter(CharacterCreateRequest request) {

                User current = getCurrentUser();

                Campaign campaign = campaignRepository.findById(request.getCampaignId())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Campaña no encontrada"));

                // Validar que el usuario actual sea el DM dueño de la campaña
                if (!campaign.getDm().getId().equals(current.getId())) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Solo el DM dueño de la campaña puede crear personajes");
                }

                User assignedPlayer = null;
                if (request.getPlayerUsername() != null && !request.getPlayerUsername().isBlank()) {
                        assignedPlayer = userRepository.findByUsername(request.getPlayerUsername())
                                        .orElseThrow(() -> new ResponseStatusException(
                                                        HttpStatus.NOT_FOUND,
                                                        "Jugador no encontrado"));
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

        // ==========================
        // Player edita nombre/imagen
        // ==========================
        @Transactional
        public CharacterResponse editCharacter(Long id, CharacterEditRequest req) {
                User current = getCurrentUser();

                CharacterEntity character = characterRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Personaje no encontrado"));

                if (character.getPlayer() == null || !character.getPlayer().getId().equals(current.getId())) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Este personaje no te pertenece");
                }

                if (req.getName() != null && !req.getName().isBlank()) {
                        character.setName(req.getName());
                }

                if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) {
                        // Aquí guardamos la cadena tal cual (URL normal o data URL base64)
                        character.setImageUrl(req.getImageUrl());
                }

                CharacterEntity saved = characterRepository.save(character);
                return toDto(saved);
        }

        // ==========================
        // Player ve su personaje
        // ==========================
        @Transactional(readOnly = true)
        public CharacterResponse getMyCharacter() {
                User current = getCurrentUser();

                CharacterEntity character = characterRepository.findByPlayer(current)
                                .stream()
                                .findFirst()
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "No tienes ningún personaje asignado"));

                return toDto(character);
        }

        // ==========================
        // DM asigna PJ a player
        // ==========================
        @Transactional
        public CharacterResponse assignCharacterToPlayer(Long characterId, String targetUsername) {

                User current = getCurrentUser();

                CharacterEntity character = characterRepository.findById(characterId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Personaje no encontrado"));

                Campaign campaign = character.getCampaign();

                if (!campaign.getDm().getId().equals(current.getId())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "Solo el DM dueño de la campaña puede asignar jugadores");
                }

                User player = userRepository.findByUsername(targetUsername)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Jugador no encontrado"));

                // 🔥 NUEVO: validar que el jugador esté unido a la campaña
                if (campaign.getPlayers() == null ||
                                campaign.getPlayers().stream().noneMatch(u -> u.getId().equals(player.getId()))) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Este jugador no está unido a esta campaña");
                }

                character.setPlayer(player);

                return toDto(characterRepository.save(character));
        }

        // ==========================
        // Personajes de una campaña
        // ==========================
        @Transactional(readOnly = true)
        public List<CharacterResponse> getCharactersByCampaign(Long campaignId) {
                Campaign campaign = campaignRepository.findById(campaignId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Campaña no encontrada"));

                return characterRepository.findByCampaign(campaign).stream()
                                .map(this::toDto)
                                .toList();
        }

        // ==========================
        // Obtener personaje por ID
        // ==========================
        @Transactional(readOnly = true)
        public CharacterResponse getById(Long id) {
                CharacterEntity character = characterRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Personaje no encontrado"));

                return toDto(character);
        }

        // ==========================
        // DM actualiza dinero del PJ
        // ==========================
        @Transactional
        public CharacterResponse updateByDm(Long id, CharacterAdminUpdateRequest req) {
                User current = getCurrentUser();

                CharacterEntity ch = characterRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Personaje no encontrado"));

                Campaign campaign = ch.getCampaign();
                if (campaign == null || !campaign.getDm().getId().equals(current.getId())) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Solo el DM dueño de la campaña puede editar el dinero del personaje");
                }

                if (req.getPp() != null)
                        ch.setPp(req.getPp());
                if (req.getGp() != null)
                        ch.setGp(req.getGp());
                if (req.getEp() != null)
                        ch.setEp(req.getEp());
                if (req.getSp() != null)
                        ch.setSp(req.getSp());
                if (req.getCp() != null)
                        ch.setCp(req.getCp());

                CharacterEntity saved = characterRepository.save(ch);
                return toDto(saved);
        }

        // ==========================
        // Inventario: agregar item
        // ==========================
        @Transactional
        public CharacterResponse addItemToInventory(Long characterId, Long itemId, int quantity) {
                if (quantity <= 0) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "La cantidad debe ser mayor a 0");
                }

                User current = getCurrentUser();

                CharacterEntity character = characterRepository.findById(characterId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Personaje no encontrado"));

                Campaign campaign = character.getCampaign();
                if (campaign == null || !campaign.getDm().getId().equals(current.getId())) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Solo el DM dueño de la campaña puede modificar el inventario");
                }

                Item item = itemRepository.findById(itemId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Ítem no encontrado"));

                CharacterItem characterItem = characterItemRepository
                                .findByCharacterAndItem(character, item)
                                .orElse(null);

                if (characterItem == null) {
                        characterItem = CharacterItem.builder()
                                        .character(character)
                                        .item(item)
                                        .quantity(quantity)
                                        .build();
                } else {
                        characterItem.setQuantity(characterItem.getQuantity() + quantity);
                }

                characterItemRepository.save(characterItem);

                // recargar personaje con inventario actualizado
                CharacterEntity updated = characterRepository.findById(characterId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Error al recargar personaje"));

                return toDto(updated);
        }

        // ==========================
        // Inventario: quitar item
        // ==========================
        @Transactional
        public CharacterResponse removeItemFromInventory(Long characterId, Long itemId, int quantity) {
                if (quantity <= 0) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "La cantidad debe ser mayor a 0");
                }

                User current = getCurrentUser();

                CharacterEntity character = characterRepository.findById(characterId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Personaje no encontrado"));

                Campaign campaign = character.getCampaign();
                if (campaign == null || !campaign.getDm().getId().equals(current.getId())) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Solo el DM dueño de la campaña puede modificar el inventario");
                }

                Item item = itemRepository.findById(itemId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Ítem no encontrado"));

                CharacterItem characterItem = characterItemRepository
                                .findByCharacterAndItem(character, item)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "El personaje no tiene este ítem en el inventario"));

                int newQty = characterItem.getQuantity() - quantity;
                if (newQty <= 0) {
                        characterItemRepository.delete(characterItem);
                } else {
                        characterItem.setQuantity(newQty);
                        characterItemRepository.save(characterItem);
                }

                CharacterEntity updated = characterRepository.findById(characterId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Error al recargar personaje"));

                return toDto(updated);
        }

        // ==========================
        // Helpers
        // ==========================

        private CharacterResponse toDto(CharacterEntity c) {

                // Mapear inventario del personaje a DTOs simples
                List<CharacterInventoryItemResponse> inventoryDtos = (c.getInventory() == null
                                ? List.<CharacterItem>of()
                                : c.getInventory())
                                .stream()
                                .map(ci -> CharacterInventoryItemResponse.builder()
                                                .itemId(ci.getItem().getId())
                                                .name(ci.getItem().getName())
                                                .quantity(ci.getQuantity())
                                                .category(ci.getItem().getCategory())
                                                .damageDice(ci.getItem().getDamageDice())
                                                .damageType(ci.getItem().getDamageType())
                                                .basePriceGold(ci.getItem().getBasePriceGold())
                                                .build())
                                .toList();

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
                                .campaignId(c.getCampaign() != null ? c.getCampaign().getId() : null)
                                .campaignName(c.getCampaign() != null ? c.getCampaign().getName() : null)
                                .playerUsername(c.getPlayer() != null ? c.getPlayer().getUsername() : null)
                                .inventory(inventoryDtos)
                                .build();
        }

        private User getCurrentUser() {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                return userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.UNAUTHORIZED,
                                                "Usuario no encontrado"));
        }

        // ==========================
        // Player ve TODOS sus PJs
        // ==========================
        @Transactional(readOnly = true)
        public List<CharacterResponse> getMyCharacters() {
                User current = getCurrentUser();

                return characterRepository.findByPlayer(current)
                                .stream()
                                .map(this::toDto)
                                .toList();
        }
}
