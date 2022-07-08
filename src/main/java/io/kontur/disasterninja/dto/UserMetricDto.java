package io.kontur.disasterninja.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserMetricDto {

    private final String name;
    private final Double value;
    private final UserMetricDtoType type;
    private final UUID appId;
    private final UUID userId;

    public enum UserMetricDtoType {
        SUMMARY
    }
}
