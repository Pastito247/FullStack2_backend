package com.fullstack2.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre de la campaña
    @Column(nullable = false)
    private String name;

    // Descripción corta
    @Column(length = 1000)
    private String description;

    // Imagen de portada (data URL base64 o URL externa)
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    // Código de invitación para que los players se unan
    @Column(nullable = false, unique = true)
    private String inviteCode;

    // Dungeon Master dueño de la campaña
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dm_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User dm;

    // Tiendas dentro de la campaña
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Shop> shops = new ArrayList<>();

    // Players que se han unido a esta campaña
    @ManyToMany
    @JoinTable(name = "campaign_players", joinColumns = @JoinColumn(name = "campaign_id"), inverseJoinColumns = @JoinColumn(name = "player_id"))
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<User> players = new ArrayList<>();

    // Personajes de la campaña (PJ + NPC)
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<CharacterEntity> characters = new ArrayList<>();
}
