package io.kontur.disasterninja.dto;

import lombok.Data;

@Data
public class EventFeedDto {

    private final String feed;
    private final String description;
    private boolean isDefault;
}
