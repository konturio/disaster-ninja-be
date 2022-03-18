package io.kontur.disasterninja.dto;


import lombok.Data;

import java.util.UUID;

@Data
public class AppSummaryDto {
    private final UUID id;
    private final String name;
}
