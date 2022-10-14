package io.kontur.disasterninja.dto;

import lombok.*;

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
}
