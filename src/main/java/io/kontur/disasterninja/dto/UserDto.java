package io.kontur.disasterninja.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDto {
    private String username;
    private String email;
    private String fullName;
    private String language;
    private boolean useMetricUnits;
    private boolean subscribedToKonturUpdates;
    private String bio;
    private String osmEditor;
    private String defaultFeed;
    private String theme;
    private String linkedin;
    private String phone;
    private boolean callConsentGiven;
    private OffsetDateTime createdAt;
    private String accountNotes;
    private String objectives;
    private String companyName;
    private String position;
    private String amountOfGis;
}
