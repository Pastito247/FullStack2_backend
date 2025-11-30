package com.fullstack2.backend.controller;

import com.fullstack2.backend.entity.Item;
import com.fullstack2.backend.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
@CrossOrigin(origins = "*")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // GET /api/v1/items
    @GetMapping
    public List<Item> getAllItems() {
        return itemService.getAllItems();
    }

    // GET /api/v1/items/{id}
    @GetMapping("/{id}")
    public Item getItemById(@PathVariable Long id) {
        return itemService.getItemById(id);
    }

    // POST /api/v1/items
    // Crea un item CUSTOM (propio del DM)
    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        Item created = itemService.createCustomItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/v1/items/{id}
    @PutMapping("/{id}")
    public Item updateItem(@PathVariable Long id, @RequestBody Item item) {
        return itemService.updateItem(id, item);
    }

    // DELETE /api/v1/items/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================
    // IMPORTAR EQUIPMENT OFICIAL
    // ==========================

    // POST /api/v1/items/import/equipment/{index}
    @PostMapping("/import/equipment/{index}")
    public ResponseEntity<Item> importOfficialEquipment(@PathVariable String index) {
        Item imported = itemService.importFromDnd5eEquipment(index);
        return ResponseEntity.status(HttpStatus.CREATED).body(imported);
    }

    // ==========================
    // IMPORTAR MAGIC ITEMS
    // ==========================

    // POST /api/v1/items/import/magic-items/{index}
    @PostMapping("/import/magic-items/{index}")
    public ResponseEntity<Item> importMagicItem(@PathVariable String index) {
        Item imported = itemService.importFromDnd5eMagicItem(index);
        return ResponseEntity.status(HttpStatus.CREATED).body(imported);
    }
}
