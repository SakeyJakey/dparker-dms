package com.davidparker.dms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LlmSecurityConfig {

    @Bean
    public ContentFilter contentFilter() {
        return ContentFilter.builder()
            .maxQueryLength(2000)
            .blockedPatterns(List.of(
                "ignore previous instructions",
                "system prompt",
                "reveal.*secret"
            ))
            .sanitizeOutput(true)
            .build();
    }

    @Bean
    public RateLimiter llmQueryRateLimiter() {
        return RateLimiter.builder()
            .perApplication(100)  // queries per minute
            .perUser(20)
            .globalLimit(1000)
            .build();
    }

    public static class ContentFilter {
        private int maxQueryLength;
        private List<String> blockedPatterns;
        private boolean sanitizeOutput;

        public static Builder builder() {
            return new Builder();
        }

        public int getMaxQueryLength() {
            return maxQueryLength;
        }

        public List<String> getBlockedPatterns() {
            return blockedPatterns;
        }

        public boolean isSanitizeOutput() {
            return sanitizeOutput;
        }

        public static class Builder {
            private ContentFilter filter = new ContentFilter();

            public Builder maxQueryLength(int length) {
                filter.maxQueryLength = length;
                return this;
            }

            public Builder blockedPatterns(List<String> patterns) {
                filter.blockedPatterns = patterns;
                return this;
            }

            public Builder sanitizeOutput(boolean sanitize) {
                filter.sanitizeOutput = sanitize;
                return this;
            }

            public ContentFilter build() {
                return filter;
            }
        }
    }

    public static class RateLimiter {
        private int perApplication;
        private int perUser;
        private int globalLimit;

        public static Builder builder() {
            return new Builder();
        }

        public int getPerApplication() {
            return perApplication;
        }

        public int getPerUser() {
            return perUser;
        }

        public int getGlobalLimit() {
            return globalLimit;
        }

        public static class Builder {
            private RateLimiter limiter = new RateLimiter();

            public Builder perApplication(int limit) {
                limiter.perApplication = limit;
                return this;
            }

            public Builder perUser(int limit) {
                limiter.perUser = limit;
                return this;
            }

            public Builder globalLimit(int limit) {
                limiter.globalLimit = limit;
                return this;
            }

            public RateLimiter build() {
                return limiter;
            }
        }
    }
}
