package io.kontur.disasterninja.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FormatUtil {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.##", new DecimalFormatSymbols(Locale.US));

    public static String formatNumber(Object number) {
        if (number == null) {
            return "0";
        }
        return DECIMAL_FORMAT.format(new BigDecimal(String.valueOf(number)));
    }
}
