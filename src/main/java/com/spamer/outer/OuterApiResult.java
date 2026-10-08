package com.spamer.outer;

public record OuterApiResult(int statusCode, long responseTimeMs, String errorMessage) {

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
}
