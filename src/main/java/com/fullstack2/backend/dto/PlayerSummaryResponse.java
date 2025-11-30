package com.fullstack2.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSummaryResponse {
    private Long id;
    private String username;
    private String email;
}
