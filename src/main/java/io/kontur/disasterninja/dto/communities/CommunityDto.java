package io.kontur.disasterninja.dto.communities;

import lombok.Data;

@Data
public class CommunityDto {
    private final String name;
    private final CommunityType type;
    private final String icon;
    private final String url;
}
