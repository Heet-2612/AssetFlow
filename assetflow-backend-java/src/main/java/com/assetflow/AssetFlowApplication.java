package com.assetflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. NOTE: this is a minimal placeholder so the module is independently runnable/testable
 * for the service layer - the API/infra teammate owning config/ (SecurityConfig, JwtConfig) and any
 * controllers should feel free to flesh this out further.
 */
@SpringBootApplication
public class AssetFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssetFlowApplication.class, args);
    }
}
