package com.fullstack2.backend.dto;

import lombok.Data;

@Data
public class CharacterAdminUpdateRequest {
    private Integer pp;
    private Integer gp;
    private Integer ep;
    private Integer sp;
    private Integer cp;
}