package com.fullstack2.backend.dto;

import lombok.Data;

@Data
public class ShopCreateRequest {

    private Long campaignId;      // lo setea el controller desde el path
    private String name;
    private String description;
    private String imageUrl;
}
