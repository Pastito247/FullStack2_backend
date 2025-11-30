package com.fullstack2.backend.controller;

import com.fullstack2.backend.dto.ShopCreateRequest;
import com.fullstack2.backend.dto.ShopItemRequest;
import com.fullstack2.backend.dto.ShopItemResponse;
import com.fullstack2.backend.dto.ShopResponse;
import com.fullstack2.backend.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    // ==========================
    // SHOPS
    // ==========================

    // Crear tienda en una campaña (solo DM/ADMIN)
    @PostMapping("/campaigns/{campaignId}/shops")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<ShopResponse> createShop(
            @PathVariable Long campaignId,
            @RequestBody ShopCreateRequest request
    ) {
        request.setCampaignId(campaignId);
        ShopResponse created = shopService.createShop(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Listar tiendas de una campaña (DM y Players de la campaña)
    @GetMapping("/campaigns/{campaignId}/shops")
    @PreAuthorize("hasAnyRole('DM','PLAYER','ADMIN')")
    public ResponseEntity<List<ShopResponse>> getShopsByCampaign(@PathVariable Long campaignId) {
        return ResponseEntity.ok(shopService.getShopsByCampaign(campaignId));
    }

    // Obtener detalle de una tienda
    @GetMapping("/shops/{id}")
    @PreAuthorize("hasAnyRole('DM','PLAYER','ADMIN')")
    public ResponseEntity<ShopResponse> getShop(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.getShopById(id));
    }

    // Actualizar tienda (solo DM dueño o ADMIN)
    @PutMapping("/shops/{id}")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<ShopResponse> updateShop(
            @PathVariable Long id,
            @RequestBody ShopCreateRequest request
    ) {
        return ResponseEntity.ok(shopService.updateShop(id, request));
    }

    // Eliminar tienda (solo DM dueño o ADMIN)
    @DeleteMapping("/shops/{id}")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================
    // ÍTEMS DENTRO DE LA TIENDA
    // ==========================

    // Listar ítems de una tienda
    @GetMapping("/shops/{shopId}/items")
    @PreAuthorize("hasAnyRole('DM','PLAYER','ADMIN')")
    public ResponseEntity<List<ShopItemResponse>> getItems(@PathVariable Long shopId) {
        return ResponseEntity.ok(shopService.getItemsByShop(shopId));
    }

    // Agregar ítem a una tienda (solo DM/ADMIN)
    @PostMapping("/shops/{shopId}/items")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<ShopItemResponse> addItem(
            @PathVariable Long shopId,
            @RequestBody ShopItemRequest request
    ) {
        ShopItemResponse created = shopService.addItemToShop(shopId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Actualizar stock / precio de un ShopItem (solo DM/ADMIN)
    @PutMapping("/shop-items/{shopItemId}")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<ShopItemResponse> updateShopItem(
            @PathVariable Long shopItemId,
            @RequestBody ShopItemRequest request
    ) {
        return ResponseEntity.ok(shopService.updateShopItem(shopItemId, request));
    }

    // Quitar ítem de la tienda (solo DM/ADMIN)
    @DeleteMapping("/shop-items/{shopItemId}")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<Void> deleteShopItem(@PathVariable Long shopItemId) {
        shopService.deleteShopItem(shopItemId);
        return ResponseEntity.noContent().build();
    }

    // ==========================
    // COMPRAR / VENDER ÍTEMS
    // ==========================

    // Comprar ítem de una tienda
    @PostMapping("/shop-items/{shopItemId}/buy")
    @PreAuthorize("hasAnyRole('PLAYER','DM','ADMIN')")
    public ResponseEntity<ShopItemResponse> buyItem(
            @PathVariable Long shopItemId,
            @RequestParam Long characterId,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        ShopItemResponse resp = shopService.buyItem(shopItemId, characterId, quantity);
        return ResponseEntity.ok(resp);
    }

    // Vender ítem a la tienda
    @PostMapping("/shop-items/{shopItemId}/sell")
    @PreAuthorize("hasAnyRole('PLAYER','DM','ADMIN')")
    public ResponseEntity<ShopItemResponse> sellItem(
            @PathVariable Long shopItemId,
            @RequestParam Long characterId,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        ShopItemResponse resp = shopService.sellItem(shopItemId, characterId, quantity);
        return ResponseEntity.ok(resp);
    }
}
