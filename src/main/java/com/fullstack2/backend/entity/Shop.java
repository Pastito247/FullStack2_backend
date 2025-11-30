package com.fullstack2.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shops")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre visible de la tienda (Herrero, Alquimista, etc.)
    @Column(nullable = false)
    private String name;

    // Opcional: pequeña descripción
    @Column(length = 1000)
    private String description;

    // Opcional: imagen de la tienda (url o base64)
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    // Campaña a la que pertenece la tienda
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    @JsonIgnore
    private Campaign campaign;

    // Ítems dentro de la tienda (relación intermedia ShopItem)
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<ShopItem> shopItems = new ArrayList<>();
}
