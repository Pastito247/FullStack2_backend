package com.fullstack2.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopResponse {

    private Long id;
    private String name;
    private String description;
    private String imageUrl;

    private Long campaignId;
    private String campaignName;
}
