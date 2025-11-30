package com.fullstack2.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopResponse {

    private Long id;
    private String name;
    private String description;
    private String imageUrl;

    private Long campaignId;
    private String campaignName;

    // Nombre de usuario del DM dueño de la campaña
    private String dmUsername;
}
