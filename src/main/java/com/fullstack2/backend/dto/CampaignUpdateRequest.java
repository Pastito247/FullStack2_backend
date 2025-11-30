package com.fullstack2.backend.dto;

import lombok.Data;

@Data
public class CampaignUpdateRequest {
    private String name;
    private String description;
    private String imageUrl;
}
