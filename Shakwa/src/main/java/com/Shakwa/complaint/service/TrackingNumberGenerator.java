package com.Shakwa.complaint.service;

import java.security.SecureRandom;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component
public class TrackingNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    public String generate() {
        StringBuilder builder = new StringBuilder("SHK-");
        LocalDate today = LocalDate.now();
        builder.append(today.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));
        builder.append("-");
        for (int i = 0; i < 6; i++) {
            builder.append(ALPHANUM[RANDOM.nextInt(ALPHANUM.length)]);
        }
        return builder.toString();
    }
}


