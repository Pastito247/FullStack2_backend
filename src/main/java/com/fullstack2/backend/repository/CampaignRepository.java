package com.fullstack2.backend.repository;

import com.fullstack2.backend.entity.Campaign;
import com.fullstack2.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    // Campañas donde soy Dungeon Master
    List<Campaign> findByDm(User dm);

    // Buscar por código de invitación
    Optional<Campaign> findByInviteCode(String inviteCode);

    // Campañas donde participo como Player (la lista "players" contiene al usuario)
    List<Campaign> findByPlayersContaining(User player);
}
