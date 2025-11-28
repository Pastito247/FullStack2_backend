package com.fullstack2.backend.service;

import com.fullstack2.backend.dto.ShopTransactionRequest;
import com.fullstack2.backend.entity.*;
import com.fullstack2.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class ShopTransactionService {

    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final CharacterItemRepository characterItemRepository;
    private final ShopRepository shopRepository;
    private final ShopItemRepository shopItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public ShopTransactionService(UserRepository userRepository,
                                  CharacterRepository characterRepository,
                                  CharacterItemRepository characterItemRepository,
                                  ShopRepository shopRepository,
                                  ShopItemRepository shopItemRepository,
                                  OrderRepository orderRepository,
                                  OrderItemRepository orderItemRepository) {
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
        this.characterItemRepository = characterItemRepository;
        this.shopRepository = shopRepository;
        this.shopItemRepository = shopItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // =======================
    // COMPRA
    // =======================
    public void buyItem(ShopTransactionRequest request) {

        if (request.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a 0");
        }

        User current = getCurrentUser();

        CharacterEntity character = characterRepository.findById(request.getCharacterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personaje no encontrado"));

        if (character.getPlayer() == null || !character.getPlayer().getId().equals(current.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este personaje no pertenece al jugador actual");
        }

        ShopItem shopItem = shopItemRepository.findById(request.getShopItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ShopItem no encontrado"));

        Shop shop = shopItem.getShop();

        // Validar que la tienda pertenece a la misma campaña que el PJ
        if (!shop.getCampaign().getId().equals(character.getCampaign().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La tienda no pertenece a la misma campaña que el personaje");
        }

        if (shopItem.getStock() < request.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente en la tienda");
        }

        Item item = shopItem.getItem();

        int unitPriceGold = shopItem.getPriceOverrideGold() > 0
                ? shopItem.getPriceOverrideGold()
                : item.getBasePriceGold();

        int totalPriceGold = unitPriceGold * request.getQuantity();
        int totalPriceCp = goldToCopper(totalPriceGold);

        int currentCp = characterToCopper(character);

        if (currentCp < totalPriceCp) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dinero insuficiente");
        }

        // Descontar dinero
        int remainingCp = currentCp - totalPriceCp;
        applyCopperToCharacter(character, remainingCp);

        // Actualizar stock tienda
        shopItem.setStock(shopItem.getStock() - request.getQuantity());
        shopItemRepository.save(shopItem);

        // Actualizar inventario del PJ
        CharacterItem characterItem = characterItemRepository
                .findByCharacterAndItem(character, item)
                .orElse(CharacterItem.builder()
                        .character(character)
                        .item(item)
                        .quantity(0)
                        .build());

        characterItem.setQuantity(characterItem.getQuantity() + request.getQuantity());
        characterItemRepository.save(characterItem);

        // Crear orden de compra
        Order order = Order.builder()
                .buyer(current)
                .shop(shop)
                .createdAt(LocalDateTime.now())
                .totalGold(totalPriceGold)
                .build();

        orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .item(item)
                .quantity(request.getQuantity())
                .priceGold(unitPriceGold)
                .build();

        orderItemRepository.save(orderItem);

        // Guardar personaje con dinero actualizado
        characterRepository.save(character);
    }

    // =======================
    // VENTA
    // =======================
    public void sellItem(ShopTransactionRequest request) {

        if (request.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a 0");
        }

        User current = getCurrentUser();

        CharacterEntity character = characterRepository.findById(request.getCharacterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personaje no encontrado"));

        if (character.getPlayer() == null || !character.getPlayer().getId().equals(current.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este personaje no pertenece al jugador actual");
        }

        ShopItem shopItem = shopItemRepository.findById(request.getShopItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ShopItem no encontrado"));

        Shop shop = shopItem.getShop();

        // Validar que la tienda pertenece a la misma campaña que el PJ
        if (!shop.getCampaign().getId().equals(character.getCampaign().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La tienda no pertenece a la misma campaña que el personaje");
        }

        Item item = shopItem.getItem();

        CharacterItem characterItem = characterItemRepository
                .findByCharacterAndItem(character, item)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El personaje no tiene ese ítem"));

        if (characterItem.getQuantity() < request.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantidad insuficiente en el inventario");
        }

        // Por simplicidad, vendemos al mismo precio que compra.
        // Si quieres 50%, aquí haces: unitPriceGold = unitPriceGold / 2;
        int unitPriceGold = shopItem.getPriceOverrideGold() > 0
                ? shopItem.getPriceOverrideGold()
                : item.getBasePriceGold();

        int totalPriceGold = unitPriceGold * request.getQuantity();
        int totalPriceCp = goldToCopper(totalPriceGold);

        // Sumar dinero al PJ
        int currentCp = characterToCopper(character);
        int newCp = currentCp + totalPriceCp;
        applyCopperToCharacter(character, newCp);

        // Actualizar inventario del PJ
        characterItem.setQuantity(characterItem.getQuantity() - request.getQuantity());
        if (characterItem.getQuantity() <= 0) {
            characterItemRepository.delete(characterItem);
        } else {
            characterItemRepository.save(characterItem);
        }

        // Actualizar stock tienda
        shopItem.setStock(shopItem.getStock() + request.getQuantity());
        shopItemRepository.save(shopItem);

        // Crear orden de venta (totalGold NEGATIVO para marcar salida de stock del PJ)
        Order order = Order.builder()
                .buyer(current)
                .shop(shop)
                .createdAt(LocalDateTime.now())
                .totalGold(-totalPriceGold)
                .build();

        orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .item(item)
                .quantity(request.getQuantity())
                .priceGold(unitPriceGold)
                .build();

        orderItemRepository.save(orderItem);

        characterRepository.save(character);
    }

    // =======================
    // HELPERS DE DINERO
    // =======================

    // 1 pp = 10 gp = 100 sp = 1000 cp
    // 1 gp = 10 sp = 100 cp
    // 1 ep = 5 sp = 50 cp
    // 1 sp = 10 cp
    private int characterToCopper(CharacterEntity c) {
        int cp = 0;
        cp += c.getPp() * 1000;
        cp += c.getGp() * 100;
        cp += c.getEp() * 50;
        cp += c.getSp() * 10;
        cp += c.getCp();
        return cp;
    }

    private int goldToCopper(int gold) {
        return gold * 100;
    }

    private void applyCopperToCharacter(CharacterEntity c, int totalCp) {
        if (totalCp < 0) {
            totalCp = 0;
        }

        int pp = totalCp / 1000;
        int remainder = totalCp % 1000;

        int gp = remainder / 100;
        remainder = remainder % 100;

        int ep = remainder / 50;
        remainder = remainder % 50;

        int sp = remainder / 10;
        int cp = remainder % 10;

        c.setPp(pp);
        c.setGp(gp);
        c.setEp(ep);
        c.setSp(sp);
        c.setCp(cp);
    }

    // =======================
    // USUARIO ACTUAL
    // =======================
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }
}
