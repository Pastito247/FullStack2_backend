package com.fullstack2.backend.service;

import com.fullstack2.backend.dto.ShopCreateRequest;
import com.fullstack2.backend.dto.ShopItemRequest;
import com.fullstack2.backend.dto.ShopItemResponse;
import com.fullstack2.backend.dto.ShopResponse;
import com.fullstack2.backend.entity.*;
import com.fullstack2.backend.repository.CampaignRepository;
import com.fullstack2.backend.repository.ItemRepository;
import com.fullstack2.backend.repository.ShopItemRepository;
import com.fullstack2.backend.repository.ShopRepository;
import com.fullstack2.backend.repository.UserRepository;
import com.fullstack2.backend.repository.CharacterRepository;
import com.fullstack2.backend.repository.CharacterItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopItemRepository shopItemRepository;
    private final CampaignRepository campaignRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    // Para compras/ventas
    private final CharacterRepository characterRepository;
    private final CharacterItemRepository characterItemRepository;

    // ==========================
    // Helpers
    // ==========================

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    private void validateDmOwner(Campaign campaign, User current) {
        if (campaign.getDm() == null || !campaign.getDm().getId().equals(current.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el DM dueño de la campaña puede administrar sus tiendas");
        }
    }

    private ShopResponse toShopDto(Shop s) {
        return ShopResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .imageUrl(s.getImageUrl())
                .campaignId(s.getCampaign() != null ? s.getCampaign().getId() : null)
                .campaignName(s.getCampaign() != null ? s.getCampaign().getName() : null)
                .dmUsername(
                        s.getCampaign() != null && s.getCampaign().getDm() != null
                                ? s.getCampaign().getDm().getUsername()
                                : null
                )
                .build();
    }

    private ShopItemResponse toShopItemDto(ShopItem si) {
        Item item = si.getItem();

        Integer basePrice = item.getBasePriceGold();
        if (basePrice == null) {
            basePrice = 0;
        }

        Integer override = si.getPriceOverrideGold();
        if (override == null) {
            override = 0;
        }

        // Precio final que verá el jugador (override si es >0, sino base)
        Integer finalPrice = (override != null && override > 0) ? override : basePrice;

        return ShopItemResponse.builder()
                .id(si.getId())                       // id del ShopItem
                .itemId(item.getId())                 // id del Item
                .name(item.getName())                 // nombre del Item
                .category(item.getCategory())         // categoría del Item
                .source(item.getSource() != null ? item.getSource().name() : null)
                .rarity(item.getRarity())
                .basePriceGold(basePrice)
                .priceOverrideGold(override)
                .finalPriceGold(finalPrice)
                .stock(si.getStock())
                .build();
    }

    // Helpers para personajes / compras

    private CharacterEntity getCharacterOrThrow(Long characterId) {
        return characterRepository.findById(characterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personaje no encontrado"));
    }

    /**
     * Verifica que el personaje está en la misma campaña que la tienda
     * y que el usuario autenticado es el dueño del personaje.
     */
    private void validateCharacterInShopCampaign(ShopItem shopItem, CharacterEntity character, User current) {
        Campaign shopCampaign = shopItem.getShop().getCampaign();

        if (character.getCampaign() == null ||
                !character.getCampaign().getId().equals(shopCampaign.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El personaje no pertenece a la misma campaña que la tienda."
            );
        }

        if (character.getPlayer() == null ||
                !character.getPlayer().getId().equals(current.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo el dueño del personaje puede comprar/vender con él."
            );
        }
    }

    // ==========================
    // CRUD SHOPS
    // ==========================

    @Transactional
    public ShopResponse createShop(ShopCreateRequest req) {
        if (req.getCampaignId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "campaignId es obligatorio");
        }

        User current = getCurrentUser();

        Campaign campaign = campaignRepository.findById(req.getCampaignId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaña no encontrada"));

        validateDmOwner(campaign, current);

        Shop shop = Shop.builder()
                .name(req.getName())
                .description(req.getDescription())
                .imageUrl(req.getImageUrl())
                .campaign(campaign)
                .build();

        Shop saved = shopRepository.save(shop);
        return toShopDto(saved);
    }

    @Transactional(readOnly = true)
    public ShopResponse getShopById(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tienda no encontrada"));
        return toShopDto(shop);
    }

    @Transactional(readOnly = true)
    public List<ShopResponse> getShopsByCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaña no encontrada"));

        List<Shop> shops = shopRepository.findByCampaign(campaign);
        return shops.stream().map(this::toShopDto).toList();
    }

    @Transactional
    public ShopResponse updateShop(Long id, ShopCreateRequest req) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tienda no encontrada"));

        User current = getCurrentUser();
        validateDmOwner(shop.getCampaign(), current);

        if (req.getName() != null && !req.getName().isBlank()) {
            shop.setName(req.getName());
        }
        if (req.getDescription() != null) {
            shop.setDescription(req.getDescription());
        }
        if (req.getImageUrl() != null) {
            shop.setImageUrl(req.getImageUrl());
        }

        Shop saved = shopRepository.save(shop);
        return toShopDto(saved);
    }

    @Transactional
    public void deleteShop(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tienda no encontrada"));

        User current = getCurrentUser();
        validateDmOwner(shop.getCampaign(), current);

        shopRepository.delete(shop);
    }

    // ==========================
    // ÍTEMS DENTRO DE LA TIENDA
    // ==========================

    @Transactional(readOnly = true)
    public List<ShopItemResponse> getItemsByShop(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tienda no encontrada"));

        List<ShopItem> items = shopItemRepository.findByShopId(shop.getId());
        return items.stream().map(this::toShopItemDto).toList();
    }

    @Transactional
    public ShopItemResponse addItemToShop(Long shopId, ShopItemRequest req) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tienda no encontrada"));

        User current = getCurrentUser();
        validateDmOwner(shop.getCampaign(), current);

        if (req.getItemId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "itemId es obligatorio");
        }

        Item item = itemRepository.findById(req.getItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item no encontrado"));

        Integer stock = (req.getStock() != null) ? req.getStock() : 0;

        Integer priceOverrideGold = req.getPriceOverrideGold();
        if (priceOverrideGold == null) {
            priceOverrideGold = item.getBasePriceGold();
        }
        if (priceOverrideGold == null) {
            priceOverrideGold = 0;
        }

        ShopItem si = ShopItem.builder()
                .shop(shop)
                .item(item)
                .stock(stock)
                .priceOverrideGold(priceOverrideGold)
                .build();

        ShopItem saved = shopItemRepository.save(si);
        return toShopItemDto(saved);
    }

    @Transactional
    public ShopItemResponse updateShopItem(Long shopItemId, ShopItemRequest req) {
        ShopItem si = shopItemRepository.findById(shopItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ShopItem no encontrado"));

        User current = getCurrentUser();
        validateDmOwner(si.getShop().getCampaign(), current);

        if (req.getStock() != null) {
            si.setStock(req.getStock());
        }
        if (req.getPriceOverrideGold() != null) {
            si.setPriceOverrideGold(req.getPriceOverrideGold());
        }

        ShopItem saved = shopItemRepository.save(si);
        return toShopItemDto(saved);
    }

    @Transactional
    public void deleteShopItem(Long shopItemId) {
        ShopItem si = shopItemRepository.findById(shopItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ShopItem no encontrado"));

        User current = getCurrentUser();
        validateDmOwner(si.getShop().getCampaign(), current);

        shopItemRepository.delete(si);
    }

    // ==========================
    // COMPRAR / VENDER ÍTEMS
    // ==========================

    @Transactional
    public ShopItemResponse buyItem(Long shopItemId, Long characterId, int quantity) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a 0.");
        }

        ShopItem shopItem = shopItemRepository.findById(shopItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item de tienda no encontrado"));

        Item item = shopItem.getItem();

        User current = getCurrentUser();
        CharacterEntity character = getCharacterOrThrow(characterId);
        validateCharacterInShopCampaign(shopItem, character, current);

        // Stock suficiente
        int currentStock = shopItem.getStock() != null ? shopItem.getStock() : 0;
        if (currentStock < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay stock suficiente en la tienda.");
        }

        // Precio por unidad (oro)
        Integer override = shopItem.getPriceOverrideGold();
        if (override == null || override <= 0) {
            override = item.getBasePriceGold();
        }
        if (override == null || override < 0) {
            override = 0;
        }

        int unitPriceGold = override;
        int totalPriceGold = unitPriceGold * quantity;

        // Convertir precio total a cobre (gp -> cp)
        int totalPriceCopper = totalPriceGold * 100;

        // Verificar dinero del personaje
        int characterCopper = character.getTotalCopper();
        if (characterCopper < totalPriceCopper) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El personaje no tiene suficiente dinero para esta compra.");
        }

        // Descontar dinero
        int newCopper = characterCopper - totalPriceCopper;
        character.setFromTotalCopper(newCopper);

        // Actualizar stock tienda
        shopItem.setStock(currentStock - quantity);

        // Agregar al inventario del personaje
        CharacterItem characterItem = characterItemRepository
                .findByCharacterIdAndItemId(character.getId(), item.getId())
                .orElse(
                        CharacterItem.builder()
                                .character(character)
                                .item(item)
                                .quantity(0)
                                .build()
                );

        int newQuantity = (characterItem.getQuantity()) + quantity;
        characterItem.setQuantity(newQuantity);

        // Guardar
        characterRepository.save(character);
        shopItemRepository.save(shopItem);
        characterItemRepository.save(characterItem);

        return toShopItemDto(shopItem);
    }

    @Transactional
    public ShopItemResponse sellItem(Long shopItemId, Long characterId, int quantity) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a 0.");
        }

        ShopItem shopItem = shopItemRepository.findById(shopItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item de tienda no encontrado"));

        Item item = shopItem.getItem();

        User current = getCurrentUser();
        CharacterEntity character = getCharacterOrThrow(characterId);
        validateCharacterInShopCampaign(shopItem, character, current);

        // Buscar ítem en inventario del personaje
        CharacterItem characterItem = characterItemRepository
                .findByCharacterIdAndItemId(character.getId(), item.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El personaje no posee este ítem."));

        int currentQty = characterItem.getQuantity();
        if (currentQty < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El personaje no tiene suficientes unidades de este ítem para vender.");
        }

        // Precio de venta: 50% del precio de compra (típico DnD)
        Integer override = shopItem.getPriceOverrideGold();
        if (override == null || override <= 0) {
            override = item.getBasePriceGold();
        }
        if (override == null || override < 0) {
            override = 0;
        }

        int unitPriceGold = override / 2; // 50%
        int totalGold = unitPriceGold * quantity;
        int totalCopper = totalGold * 100;

        // Sumar dinero al personaje
        int currentCopper = character.getTotalCopper();
        int newCopper = currentCopper + totalCopper;
        character.setFromTotalCopper(newCopper);

        // Actualizar inventario del personaje
        int newQty = currentQty - quantity;
        if (newQty <= 0) {
            characterItemRepository.delete(characterItem);
        } else {
            characterItem.setQuantity(newQty);
            characterItemRepository.save(characterItem);
        }

        // Actualizar stock tienda (el vendedor recupera ese ítem)
        int currentStock = shopItem.getStock() != null ? shopItem.getStock() : 0;
        shopItem.setStock(currentStock + quantity);
        shopItemRepository.save(shopItem);

        characterRepository.save(character);

        return toShopItemDto(shopItem);
    }
}
