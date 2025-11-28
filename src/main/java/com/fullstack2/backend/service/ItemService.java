package com.fullstack2.backend.service;

import com.fullstack2.backend.entity.Item;
import com.fullstack2.backend.entity.ItemSource;
import com.fullstack2.backend.entity.User;
import com.fullstack2.backend.repository.ItemRepository;
import com.fullstack2.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final Dnd5eClient dnd5eClient;
    private final UserRepository userRepository;

    public ItemService(ItemRepository itemRepository,
                       Dnd5eClient dnd5eClient,
                       UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.dnd5eClient = dnd5eClient;
        this.userRepository = userRepository;
    }

    // Listar todos los items
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    // Obtener item por ID
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item no encontrado"));
    }

    // Crear item CUSTOM (propio del DM)
    public Item createCustomItem(Item item) {
        item.setId(null); // por si viene con algo
        item.setSource(ItemSource.CUSTOM);
        item.setDnd5eIndex(null); // no viene de la API oficial

        // 🔥 Obtener usuario logueado desde JWT/SecurityContext
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        // 🔥 Setear quién creó el ítem
        item.setCreatedBy(creator);

        return itemRepository.save(item);
    }

    // Actualizar item (campos básicos)
    public Item updateItem(Long id, Item updated) {
        Item existing = getItemById(id);

        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setBasePriceGold(updated.getBasePriceGold());
        existing.setDescription(updated.getDescription());
        existing.setRarity(updated.getRarity());
        existing.setImageUrl(updated.getImageUrl());

        return itemRepository.save(existing);
    }

    // Eliminar item
    public void deleteItem(Long id) {
        Item existing = getItemById(id);
        itemRepository.delete(existing);
    }

    // ==========================
    // IMPORTAR EQUIPMENT OFICIAL
    // ==========================

    @SuppressWarnings("unchecked")
    public Item importFromDnd5eEquipment(String index) {
        // Llamamos a tu cliente genérico
        Map<String, Object> response = dnd5eClient.getEquipmentByIndex(index);

        if (response == null || response.isEmpty()) {
            throw new RuntimeException("No se encontró el equipo con index: " + index);
        }

        // === Campos básicos ===
        String name = (String) response.get("name");

        // equipment_category: { "name": "Weapon", ... }
        String equipmentCategory = null;
        Object equipmentCategoryObj = response.get("equipment_category");
        if (equipmentCategoryObj instanceof Map<?, ?> ecMap) {
            equipmentCategory = (String) ecMap.get("name");
        }

        // weapon_category: "Martial", "Simple", etc. (no lo guardamos directo, pero nos sirve para categoría)
        String weaponCategory = (String) response.get("weapon_category");

        // weapon_range: "Melee", "Ranged", "Melee or Ranged"
        String weaponRange = (String) response.get("weapon_range");

        // === Coste -> convertir a oro ===
        Integer basePriceGold = null;
        Object costObj = response.get("cost");
        if (costObj instanceof Map<?, ?> costMap) {
            Number qtyNum = (Number) costMap.get("quantity");
            String unit = (String) costMap.get("unit"); // "gp", "sp", "cp"

            int qty = qtyNum != null ? qtyNum.intValue() : 0;

            if (unit != null) {
                switch (unit) {
                    case "gp" -> basePriceGold = qty;
                    case "sp" -> basePriceGold = qty / 10;   // 10 sp = 1 gp
                    case "cp" -> basePriceGold = qty / 100;  // 100 cp = 1 gp
                    default -> basePriceGold = qty;
                }
            } else {
                basePriceGold = qty;
            }
        }

        // === Daño ===
        String damageDice = null;
        String damageType = null;

        Object damageObj = response.get("damage");
        if (damageObj instanceof Map<?, ?> damageMap) {
            damageDice = (String) damageMap.get("damage_dice");

            Object damageTypeObj = damageMap.get("damage_type");
            if (damageTypeObj instanceof Map<?, ?> damageTypeMap) {
                damageType = (String) damageTypeMap.get("name"); // "Slashing", "Piercing", etc.
            }
        }

        // === Rango ===
        Integer rangeNormal = null;
        Integer rangeLong = null;

        Object rangeObj = response.get("range");
        if (rangeObj instanceof Map<?, ?> rangeMap) {
            Number normalNum = (Number) rangeMap.get("normal");
            Number longNum = (Number) rangeMap.get("long");

            if (normalNum != null) {
                rangeNormal = normalNum.intValue();
            }
            if (longNum != null) {
                rangeLong = longNum.intValue();
            }
        }

        // === Propiedades (lista) ===
        String properties = null;
        Object propsObj = response.get("properties");
        if (propsObj instanceof List<?> list) {
            properties = list.stream()
                    .filter(e -> e instanceof Map<?, ?>)
                    .map(e -> (Map<?, ?>) e)
                    .map(m -> (String) m.get("name"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
        }

        // === Index oficial (para re-importar o linkear) ===
        String dnd5eIndex = (String) response.get("index");

        // === Imagen ===
        String imageUrl = null;
        Object imageObj = response.get("image");
        if (imageObj instanceof String imagePath) {
            imageUrl = "https://www.dnd5eapi.co" + imagePath;
        }

        // === Definir categoría general ===
        // Si viene equipmentCategory (Weapon, Armor, Gear, etc) la usamos,
        // si no, tratamos de usar weaponCategory, y si no, null.
        String category = equipmentCategory != null ? equipmentCategory : weaponCategory;

        // === Construir el Item ===
        Item item = Item.builder()
                .name(name)
                .source(ItemSource.OFFICIAL)
                .category(category)
                .weaponRange(weaponRange)
                .damageDice(damageDice)
                .damageType(damageType)
                .rangeNormal(rangeNormal != null ? rangeNormal : 0)
                .rangeLong(rangeLong != null ? rangeLong : 0)
                .properties(properties)
                .basePriceGold(basePriceGold != null ? basePriceGold : 0)
                .rarity(null)       // si luego quieres mapear rarezas, lo cambiamos
                .description(null)  // podrías rellenar con algo custom si quieres
                .dnd5eIndex(dnd5eIndex)
                .imageUrl(imageUrl)
                .build();

        return itemRepository.save(item);
    }

}
