package xyz.hstudio.apexbattle.util;

import java.util.regex.Pattern;

public class NumberUtil {

    public static boolean isInt(final String input) {
        Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
        return pattern.matcher(input).matches();
    }
}