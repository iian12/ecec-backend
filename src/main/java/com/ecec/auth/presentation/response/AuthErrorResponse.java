package com.ecec.auth.presentation.response;

import java.util.Map;

public record AuthErrorResponse(String code, String message, Map<String, String> fieldErrors) {
}
