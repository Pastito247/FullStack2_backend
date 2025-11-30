package com.fullstack2.backend.service;

import com.fullstack2.backend.entity.Item;
import com.fullstack2.backend.entity.ItemSource;
import com.fullstack2.backend.entity.User;
import com.fullstack2.backend.repository.ItemRepository;
import com.fullstack2.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final Dnd5eClient dnd5eClient;
    private final UserRepository userRepository;

    // ==========================
    // CRUD ÍTEMS LOCALES
    // ==========================

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item no encontrado"));
    }

    public Item createCustomItem(Item item) {
        item.setId(null);
        item.setSource(ItemSource.CUSTOM);
        item.setDnd5eIndex(null);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        item.setCreatedBy(creator);

        return itemRepository.save(item);
    }

    public Item updateItem(Long id, Item updated) {
        Item existing = getItemById(id);

        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setBasePriceGold(updated.getBasePriceGold());
        existing.setDescription(updated.getDescription());
        existing.setRarity(updated.getRarity());
        existing.setImageUrl(updated.getImageUrl());
        existing.setWeaponRange(updated.getWeaponRange());
        existing.setDamageDice(updated.getDamageDice());
        existing.setDamageType(updated.getDamageType());
        existing.setRangeNormal(updated.getRangeNormal());
        existing.setRangeLong(updated.getRangeLong());
        existing.setProperties(updated.getProperties());

        return itemRepository.save(existing);
    }

    public void deleteItem(Long id) {
        Item existing = getItemById(id);
        itemRepository.delete(existing);
    }

    // ==========================
    // IMPORTAR EQUIPMENT OFICIAL
    // ==========================

    @SuppressWarnings("unchecked")
    public Item importFromDnd5eEquipment(String index) {
        Map<String, Object> response = dnd5eClient.getEquipmentByIndex(index);

        if (response == null || response.isEmpty()) {
            throw new RuntimeException("No se encontró el equipo con index: " + index);
        }

        String name = (String) response.get("name");

        String equipmentCategory = null;
        Object equipmentCategoryObj = response.get("equipment_category");
        if (equipmentCategoryObj instanceof Map<?, ?> ecMap) {
            equipmentCategory = (String) ecMap.get("name");
        }

        String weaponCategory = (String) response.get("weapon_category");
        String weaponRange = (String) response.get("weapon_range");

        Integer basePriceGold = null;
        Object costObj = response.get("cost");
        if (costObj instanceof Map<?, ?> costMap) {
            Number qtyNum = (Number) costMap.get("quantity");
            String unit = (String) costMap.get("unit");
            int qty = qtyNum != null ? qtyNum.intValue() : 0;

            if (unit != null) {
                switch (unit) {
                    case "gp" -> basePriceGold = qty;
                    case "sp" -> basePriceGold = qty / 10;
                    case "cp" -> basePriceGold = qty / 100;
                    default -> basePriceGold = qty;
                }
            } else {
                basePriceGold = qty;
            }
        }

        String damageDice = null;
        String damageType = null;

        Object damageObj = response.get("damage");
        if (damageObj instanceof Map<?, ?> damageMap) {
            damageDice = (String) damageMap.get("damage_dice");

            Object damageTypeObj = damageMap.get("damage_type");
            if (damageTypeObj instanceof Map<?, ?> damageTypeMap) {
                damageType = (String) damageTypeMap.get("name");
            }
        }

        Integer rangeNormal = null;
        Integer rangeLong = null;

        Object rangeObj = response.get("range");
        if (rangeObj instanceof Map<?, ?> rangeMap) {
            Number normalNum = (Number) rangeMap.get("normal");
            Number longNum = (Number) rangeMap.get("long");

            if (normalNum != null) rangeNormal = normalNum.intValue();
            if (longNum != null) rangeLong = longNum.intValue();
        }

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

        String dnd5eIndex = (String) response.get("index");

        String imageUrl = null;
        Object imageObj = response.get("image");
        if (imageObj instanceof String imagePath) {
            imageUrl = "https://www.dnd5eapi.co" + imagePath;
        }

        String category = equipmentCategory != null ? equipmentCategory : weaponCategory;

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
                .rarity(null)
                .description(null)
                .dnd5eIndex(dnd5eIndex)
                .imageUrl(imageUrl)
                .build();

        return itemRepository.save(item);
    }

    // ==========================
    // IMPORTAR MAGIC ITEM OFICIAL
    // ==========================

    @SuppressWarnings("unchecked")
    public Item importFromDnd5eMagicItem(String index) {
        Map<String, Object> response = dnd5eClient.getMagicItemByIndex(index);

        if (response == null || response.isEmpty()) {
            throw new RuntimeException("No se encontró el magic item con index: " + index);
        }

        String name = (String) response.get("name");

        // Intentar leer categoría (a veces viene como objeto, a veces como string)
        String category = null;
        Object catObj = response.get("equipment_category");
        if (catObj instanceof Map<?, ?> catMap) {
            category = (String) catMap.get("name");
        } else if (catObj instanceof String s) {
            category = s;
        }

        // Rarity
        String rarity = null;
        Object rarityObj = response.get("rarity");
        if (rarityObj instanceof Map<?, ?> rMap) {
            rarity = (String) rMap.get("name");
        } else if (rarityObj instanceof String s) {
            rarity = s;
        }

        // Descripción: suele venir como lista de strings en "desc"
        String description = null;
        Object descObj = response.get("desc");
        if (descObj instanceof List<?> list) {
            description = ((List<?>) list).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.joining("\n"));
        } else if (descObj instanceof String s) {
            description = s;
        }

        String dnd5eIndex = (String) response.get("index");

        String imageUrl = null;
        Object imageObj = response.get("image");
        if (imageObj instanceof String imagePath) {
            imageUrl = "https://www.dnd5eapi.co" + imagePath;
        }

        Item item = Item.builder()
                .name(name)
                .source(ItemSource.OFFICIAL)
                .category(category)
                .weaponRange(null)
                .damageDice(null)
                .damageType(null)
                .rangeNormal(0)
                .rangeLong(0)
                .properties(null)
                .basePriceGold(0) // la API de magic-items normalmente no trae coste
                .rarity(rarity)
                .description(description)
                .dnd5eIndex(dnd5eIndex)
                .imageUrl(imageUrl)
                .build();

        return itemRepository.save(item);
    }
}
