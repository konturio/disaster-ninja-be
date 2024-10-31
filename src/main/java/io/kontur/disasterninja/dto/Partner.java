package io.kontur.disasterninja.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class Partner {
    private String name;
    private int totalLocations;
    private Set<String> locations;
}
