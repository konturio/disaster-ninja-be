package io.kontur.disasterninja.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Deprecated(since = "0.23.0")
public class AnalyticsDto {

    private String name;

    private String description;

    private Integer percentValue;

    private String text;
}
