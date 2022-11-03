package io.kontur.disasterninja.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class UserMetricDto {
    @NotNull
    private final String name;
    @NotNull
    private final Double value;
    @NotNull
    private final UserMetricDtoType type;
    private final UUID appId;
    private final UUID userId;
    private final String buildVersion;

    public enum UserMetricDtoType {
        SUMMARY
    }
}
