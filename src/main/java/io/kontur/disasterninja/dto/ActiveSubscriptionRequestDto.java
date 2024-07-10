package io.kontur.disasterninja.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSubscriptionRequestDto {
    @NotNull(message = "appId is required")
    private UUID appId;

    @NotBlank(message = "billingPlanId is required")
    @Size(min = 26, max = 26, message = "billingPlanId must be exactly 26 characters long")
    private String billingPlanId;

    @NotBlank(message = "billingSubscriptionId is required")
    @Size(min = 3, max = 50, message = "billingSubscriptionId must be from 3 to 50 characters long")
    private String billingSubscriptionId;
}
