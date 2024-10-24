package io.kontur.disasterninja.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailDto {
    private String subject;
    private String textBody;
    private String htmlBody;
}
