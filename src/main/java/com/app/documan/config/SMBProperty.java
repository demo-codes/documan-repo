package com.app.documan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "smb")
@Data
public class SMBProperty {
    private String host;
    private int port;
    private String domain;  // workgroup
    private String username;
    private String password;
    private String shareName;
}