package com.ecec.auth.application;

public record SignUpCommand(String email, String password, String confirmedPassword, String nickname) {
}
