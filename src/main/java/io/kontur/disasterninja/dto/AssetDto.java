package io.kontur.disasterninja.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetDto {
    private Long id;
    private String mediaType;
    private String mediaSubtype;
    private String filename;
    private String description;
    private Long ownerUserId;
    private String language;
    private OffsetDateTime lastUpdated;
    private UUID appId;
    private Long featureId;
    private byte[] asset;
}
