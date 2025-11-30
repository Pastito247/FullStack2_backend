package com.fullstack2.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CampaignPlayerResponse {
    private Long id;
    private String username;
    private String email;
}
