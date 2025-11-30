package com.fullstack2.backend.controller;

import com.fullstack2.backend.entity.Item;
import com.fullstack2.backend.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // ==========================
    // ÍTEMS EN LA BD LOCAL
    // ==========================

    // Listar todos los ítems de la BD (custom + oficiales ya importados)
    @GetMapping
    @PreAuthorize("hasAnyRole('DM','PLAYER','ADMIN')")
    public List<Item> getAllItems() {
        return itemService.getAllItems();
    }

    // Obtener ítem por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DM','PLAYER','ADMIN')")
    public Item getItemById(@PathVariable Long id) {
        return itemService.getItemById(id);
    }

    // Crear ítem CUSTOM (propio del DM)
    @PostMapping
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        Item created = itemService.createCustomItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Actualizar ítem (solo DM / ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public Item updateItem(@PathVariable Long id, @RequestBody Item item) {
        return itemService.updateItem(id, item);
    }

    // Eliminar ítem
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================
    // IMPORTAR EQUIPMENT OFICIAL
    // ==========================

    // Importar un "equipment" oficial por index de la API dnd5e
    @PostMapping("/import/equipment/{index}")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<Item> importOfficialEquipment(@PathVariable String index) {
        Item imported = itemService.importFromDnd5eEquipment(index);
        return ResponseEntity.status(HttpStatus.CREATED).body(imported);
    }

    // ==========================
    // IMPORTAR MAGIC ITEM OFICIAL
    // ==========================

    @PostMapping("/import/magic-items/{index}")
    @PreAuthorize("hasAnyRole('DM','ADMIN')")
    public ResponseEntity<Item> importOfficialMagicItem(@PathVariable String index) {
        Item imported = itemService.importFromDnd5eMagicItem(index);
        return ResponseEntity.status(HttpStatus.CREATED).body(imported);
    }
}
