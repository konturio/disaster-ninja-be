package io.kontur.disasterninja.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Function {

    private String id;

    private String function;

    private String postfix;

    private List<String> arguments;
}
