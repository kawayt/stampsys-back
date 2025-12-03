package com.example.stampsysback.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.UUID;

/**
 * このアプリケーションインスタンス固有の ID を提供する。
 */
@Getter
@Setter
@Component
public class InstanceIdProvider {
    private String instanceId;

    @PostConstruct
    public void init() {
        this.instanceId = UUID.randomUUID().toString();
    }
}