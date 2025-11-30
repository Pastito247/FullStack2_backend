package com.fullstack2.backend.service;

import com.fullstack2.backend.dto.ShopCreateRequest;
import com.fullstack2.backend.dto.ShopItemRequest;
import com.fullstack2.backend.dto.ShopItemResponse;
import com.fullstack2.backend.dto.ShopResponse;
import com.fullstack2.backend.entity.Campaign;
import com.fullstack2.backend.entity.Item;
import com.fullstack2.backend.entity.Shop;
import com.fullstack2.backend.entity.ShopItem;
import com.fullstack2.backend.entity.User;
import com.fullstack2.backend.repository.CampaignRepository;
import com.fullstack2.backend.repository.ItemRepository;
import com.fullstack2.backend.repository.ShopItemRepository;
import com.fullstack2.backend.repository.ShopRepository;
import com.fullstack2.backend.repository.UserRepository;
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
                .build();
    }

    private ShopItemResponse toShopItemDto(ShopItem si) {
        Item item = si.getItem();

        return ShopItemResponse.builder()
                .id(si.getId())
                .itemId(item.getId())
                .itemName(item.getName())
                .itemCategory(item.getCategory())
                .itemRarity(item.getRarity())
                .basePriceGold(item.getBasePriceGold())
                .stock(si.getStock())
                .priceOverrideGold(si.getPriceOverrideGold())
                .build();
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
        // No hace falta ser DM, basta estar autenticado (el controller controlará roles)
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

        Integer stock = req.getStock() != null ? req.getStock() : 0;

        ShopItem si = ShopItem.builder()
                .shop(shop)
                .item(item)
                .stock(stock)
                .priceOverrideGold(req.getPriceOverrideGold())
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
}
