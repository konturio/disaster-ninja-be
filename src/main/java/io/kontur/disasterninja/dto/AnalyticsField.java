package io.kontur.disasterninja.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AnalyticsField {

    private String name;

    private String description;

    private List<Function> functions;
}
