package com.fullstack2.backend.repository;

import com.fullstack2.backend.entity.CharacterEntity;
import com.fullstack2.backend.entity.Campaign;
import com.fullstack2.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CharacterRepository extends JpaRepository<CharacterEntity, Long> {

    List<CharacterEntity> findByCampaign(Campaign campaign);

    // Un solo personaje para un player (si usas 1 PJ por jugador)
    Optional<CharacterEntity> findByPlayer(User player);

    // Varios personajes para un player
    List<CharacterEntity> findAllByPlayer(User player);

    List<CharacterEntity> findByPlayerUsername(String username);
}
