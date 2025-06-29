package io.kontur.disasterninja.notifications.slack;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SlackMessageFormatterTest {

    @Test
    public void sanitizeEventNameReplacesProblemCharacters() {
        String result = SlackMessageFormatter.sanitizeEventName("Event >=5 >4");
        assertEquals("Event ≥5 ≥4", result);
    }
}
