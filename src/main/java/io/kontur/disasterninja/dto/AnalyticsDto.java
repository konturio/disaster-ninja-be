package io.kontur.disasterninja.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalyticsDto {

    private String name;

    private String description;

    private Integer percentValue;

    private String text;
}
