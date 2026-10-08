package com.spamer.outer.integration;

public final class PhoneNormalizer {

    private PhoneNormalizer() {
    }

    public static String toE164(String phone) {
        String digits = phone.replaceAll("\\D", "");
        if (digits.startsWith("8") && digits.length() == 11) {
            digits = "7" + digits.substring(1);
        }
        if (digits.length() == 10) {
            digits = "7" + digits;
        }
        if (digits.startsWith("7") && digits.length() == 11) {
            return "+" + digits;
        }
        if (phone.startsWith("+")) {
            return phone;
        }
        return "+" + digits;
    }

    public static String toNational(String phone) {
        String digits = phone.replaceAll("\\D", "");
        if (digits.startsWith("7") && digits.length() == 11) {
            return "8" + digits.substring(1);
        }
        if (digits.startsWith("8") && digits.length() == 11) {
            return digits;
        }
        return digits;
    }

    /** 11 digits starting with 7, e.g. 79312893745 (Webbankir). */
    public static String toMobile7(String phone) {
        String digits = phone.replaceAll("\\D", "");
        if (digits.startsWith("8") && digits.length() == 11) {
            return "7" + digits.substring(1);
        }
        if (digits.length() == 10) {
            return "7" + digits;
        }
        return digits;
    }
}
