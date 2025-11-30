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
@RequestMapping("/api/v1") // 👈 base genérica, no /shops
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    // ==========================
    // SHOPS
    // ==========================

    // Crear tienda en una campaña (solo DM/ADMIN)
    // POST /api/v1/campaigns/{campaignId}/shops
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
    // GET /api/v1/campaigns/{campaignId}/shops
    @GetMapping("/campaigns/{campaignId}/shops")
    @PreAuthorize("hasAnyRole('DM','PLAYER','ADMIN')")
    public ResponseEntity<List<ShopResponse>> getShopsByCampaign(
            @PathVariable Long campaignId
    ) {
        return ResponseEntity.ok(shopService.getShopsByCampaign(campaignId));
    }

    // Obtener detalle de una tienda
    // GET /api/v1/shops/{id}
    @GetMapping("/shops/{id}")
    @PreAuthorize("hasAnyRole('DM','PLAYER','ADMIN')")
    public ResponseEntity<ShopResponse> getShop(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.getShopById(id));
    }

    // Actualizar tienda (solo DM dueño o ADMIN)
    // PUT /api/v1/shops/{id}
    @PutMapping("/shops/{id}")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<ShopResponse> updateShop(
            @PathVariable Long id,
            @RequestBody ShopCreateRequest request
    ) {
        return ResponseEntity.ok(shopService.updateShop(id, request));
    }

    // Eliminar tienda (solo DM dueño o ADMIN)
    // DELETE /api/v1/shops/{id}
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
    // GET /api/v1/shops/{shopId}/items
    @GetMapping("/shops/{shopId}/items")
    @PreAuthorize("hasAnyRole('DM','PLAYER','ADMIN')")
    public ResponseEntity<List<ShopItemResponse>> getItems(
            @PathVariable Long shopId
    ) {
        return ResponseEntity.ok(shopService.getItemsByShop(shopId));
    }

    // Agregar ítem a una tienda (solo DM/ADMIN)
    // POST /api/v1/shops/{shopId}/items
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
    // PUT /api/v1/shops/shop-items/{shopItemId}
    @PutMapping("/shops/shop-items/{shopItemId}")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<ShopItemResponse> updateShopItem(
            @PathVariable Long shopItemId,
            @RequestBody ShopItemRequest request
    ) {
        return ResponseEntity.ok(shopService.updateShopItem(shopItemId, request));
    }

    // Quitar ítem de la tienda (solo DM/ADMIN)
    // DELETE /api/v1/shops/shop-items/{shopItemId}
    @DeleteMapping("/shops/shop-items/{shopItemId}")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<Void> deleteShopItem(@PathVariable Long shopItemId) {
        shopService.deleteShopItem(shopItemId);
        return ResponseEntity.noContent().build();
    }
}
