package com.davidparker.dms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Iso27001Controls {

    @Bean
    public AccessControlPolicy accessControlPolicy() {
        return AccessControlPolicy.builder()
            .requireMfa(true)
            .sessionTimeout(Duration.ofMinutes(30))
            .maxFailedAttempts(5)
            .lockoutDuration(Duration.ofMinutes(30))
            .passwordPolicy(PasswordPolicy.ISO27001_COMPLIANT)
            .build();
    }

    @Bean
    public CryptographyPolicy cryptographyPolicy() {
        return CryptographyPolicy.builder()
            .encryptionAlgorithm("AES-256-GCM")
            .keyRotationPeriod(Duration.ofDays(365))
            .keyDerivationFunction("PBKDF2")
            .minimumKeyLength(256)
            .build();
    }

    @Bean
    public SecurityMonitoringConfig securityMonitoring() {
        return SecurityMonitoringConfig.builder()
            .enableRealTimeAlerts(true)
            .logRetentionDays(365)
            .anomalyDetectionEnabled(true)
            .vulnerabilityScanSchedule("0 0 2 * * SUN")
            .build();
    }

    public static class AccessControlPolicy {
        private boolean requireMfa;
        private Duration sessionTimeout;
        private int maxFailedAttempts;
        private Duration lockoutDuration;
        private PasswordPolicy passwordPolicy;

        public static Builder builder() {
            return new Builder();
        }

        public boolean isRequireMfa() {
            return requireMfa;
        }

        public Duration getSessionTimeout() {
            return sessionTimeout;
        }

        public int getMaxFailedAttempts() {
            return maxFailedAttempts;
        }

        public Duration getLockoutDuration() {
            return lockoutDuration;
        }

        public PasswordPolicy getPasswordPolicy() {
            return passwordPolicy;
        }

        public static class Builder {
            private AccessControlPolicy policy = new AccessControlPolicy();

            public Builder requireMfa(boolean requireMfa) {
                policy.requireMfa = requireMfa;
                return this;
            }

            public Builder sessionTimeout(Duration sessionTimeout) {
                policy.sessionTimeout = sessionTimeout;
                return this;
            }

            public Builder maxFailedAttempts(int maxFailedAttempts) {
                policy.maxFailedAttempts = maxFailedAttempts;
                return this;
            }

            public Builder lockoutDuration(Duration lockoutDuration) {
                policy.lockoutDuration = lockoutDuration;
                return this;
            }

            public Builder passwordPolicy(PasswordPolicy passwordPolicy) {
                policy.passwordPolicy = passwordPolicy;
                return this;
            }

            public AccessControlPolicy build() {
                return policy;
            }
        }
    }

    public enum PasswordPolicy {
        ISO27001_COMPLIANT
    }

    public static class CryptographyPolicy {
        private String encryptionAlgorithm;
        private Duration keyRotationPeriod;
        private String keyDerivationFunction;
        private int minimumKeyLength;

        public static Builder builder() {
            return new Builder();
        }

        public String getEncryptionAlgorithm() {
            return encryptionAlgorithm;
        }

        public Duration getKeyRotationPeriod() {
            return keyRotationPeriod;
        }

        public String getKeyDerivationFunction() {
            return keyDerivationFunction;
        }

        public int getMinimumKeyLength() {
            return minimumKeyLength;
        }

        public static class Builder {
            private CryptographyPolicy policy = new CryptographyPolicy();

            public Builder encryptionAlgorithm(String algorithm) {
                policy.encryptionAlgorithm = algorithm;
                return this;
            }

            public Builder keyRotationPeriod(Duration period) {
                policy.keyRotationPeriod = period;
                return this;
            }

            public Builder keyDerivationFunction(String function) {
                policy.keyDerivationFunction = function;
                return this;
            }

            public Builder minimumKeyLength(int length) {
                policy.minimumKeyLength = length;
                return this;
            }

            public CryptographyPolicy build() {
                return policy;
            }
        }
    }

    public static class SecurityMonitoringConfig {
        private boolean enableRealTimeAlerts;
        private int logRetentionDays;
        private boolean anomalyDetectionEnabled;
        private String vulnerabilityScanSchedule;

        public static Builder builder() {
            return new Builder();
        }

        public boolean isEnableRealTimeAlerts() {
            return enableRealTimeAlerts;
        }

        public int getLogRetentionDays() {
            return logRetentionDays;
        }

        public boolean isAnomalyDetectionEnabled() {
            return anomalyDetectionEnabled;
        }

        public String getVulnerabilityScanSchedule() {
            return vulnerabilityScanSchedule;
        }

        public static class Builder {
            private SecurityMonitoringConfig config = new SecurityMonitoringConfig();

            public Builder enableRealTimeAlerts(boolean enable) {
                config.enableRealTimeAlerts = enable;
                return this;
            }

            public Builder logRetentionDays(int days) {
                config.logRetentionDays = days;
                return this;
            }

            public Builder anomalyDetectionEnabled(boolean enabled) {
                config.anomalyDetectionEnabled = enabled;
                return this;
            }

            public Builder vulnerabilityScanSchedule(String schedule) {
                config.vulnerabilityScanSchedule = schedule;
                return this;
            }

            public SecurityMonitoringConfig build() {
                return config;
            }
        }
    }
}
