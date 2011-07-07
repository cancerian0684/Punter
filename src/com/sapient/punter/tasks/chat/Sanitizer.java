package com.sapient.punter.tasks.chat;

public class Sanitizer {

    public static String maxLength(String message, int maxLength) {
        if (message == null) {
            return null;
        }
        if (message.length() > maxLength) {
            message = message.substring(0, maxLength - 3) + "...";
        }
        return message;
    }

    public static String validChars(String message) {
        if (message == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if ((c == '\t' || c == '\n' || c == '\r' || c == '\7') || (c >= 32 && c < 127) ) {
                sb.append(c);
            } else {
                sb.append('*');
            }
        }
        return sb.toString();
    }
}

