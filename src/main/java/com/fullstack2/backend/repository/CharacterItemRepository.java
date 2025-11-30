package com.fullstack2.backend.repository;

import com.fullstack2.backend.entity.CharacterEntity;
import com.fullstack2.backend.entity.CharacterItem;
import com.fullstack2.backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CharacterItemRepository extends JpaRepository<CharacterItem, Long> {

    // Para ShopService (comprar/vender por IDs)
    Optional<CharacterItem> findByCharacterIdAndItemId(Long characterId, Long itemId);

    List<CharacterItem> findByCharacterId(Long characterId);

    // Para CharacterService y ShopTransactionService (usa las entidades)
    Optional<CharacterItem> findByCharacterAndItem(CharacterEntity character, Item item);
}
