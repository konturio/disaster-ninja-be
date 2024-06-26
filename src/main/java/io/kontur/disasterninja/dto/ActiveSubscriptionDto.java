package io.kontur.disasterninja.dto;

import lombok.*;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ActiveSubscriptionDto {
    private String id;
    private String billingPlanId;
    private String billingSubscriptionId;
}
