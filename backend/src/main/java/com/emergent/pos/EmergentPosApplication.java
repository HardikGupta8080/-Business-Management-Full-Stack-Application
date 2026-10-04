package com.emergent.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class EmergentPosApplication {
    public static void main(String[] args) {
        // The postgresql JDBC driver sends a "SET TimeZone" using the JVM's
        // OS-reported default zone on connect. Some Windows locales report
        // deprecated zone aliases (e.g. "Asia/Calcutta") that current
        // PostgreSQL builds don't recognize, which fails every connection
        // with "FATAL: invalid value for parameter TimeZone". Pin to UTC
        // before anything else starts so this is deterministic everywhere.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(EmergentPosApplication.class, args);
    }
}
