package io.kontur.disasterninja.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

public final class TestUtil {

    public static String readFile(Object context, String file) throws IOException {
        return IOUtils.toString(context.getClass().getResourceAsStream(file), "UTF-8");
    }
}
