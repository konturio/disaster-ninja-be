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
    private final UUID appId;
    private boolean isUserLoggedIn;
    private final String buildVersion;
}
