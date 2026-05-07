package com.example.vibe_store.security;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Component;

@Component
public class DeviceInfoParser {

    private final UserAgentAnalyzer analyzer;

    public DeviceInfoParser() {
        this.analyzer = UserAgentAnalyzer.newBuilder()
                .withFields(
                        UserAgent.AGENT_NAME,
                        UserAgent.AGENT_VERSION_MAJOR,
                        UserAgent.OPERATING_SYSTEM_VERSION_MAJOR,
                        UserAgent.DEVICE_CLASS
                )
                .withCache(1000)
                .build();
    }

    public String parse(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            return "Unknown Device";
        }

        UserAgent ua = analyzer.parse(userAgentString);

        String browser = ua.getValue(UserAgent.AGENT_NAME);
        String browserVersion = ua.getValue(UserAgent.AGENT_VERSION_MAJOR);
        String os = ua.getValue(UserAgent.OPERATING_SYSTEM_NAME);
        String osVersion = ua.getValue(UserAgent.OPERATING_SYSTEM_VERSION_MAJOR);
        String deviceClass = ua.getValue(UserAgent.DEVICE_CLASS);

        return format(browser, browserVersion)
                + " · " + format(os, osVersion)
                + " · " + deviceClass;
    }
    private String format(String name, String version) {
        if ("??".equals(name) || name == null) return "Unknown";
        if ("??".equals(version) || version == null) return name;
        return name + " " + version;
    }
}