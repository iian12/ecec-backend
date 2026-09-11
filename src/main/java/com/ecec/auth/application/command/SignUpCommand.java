package com.ecec.auth.application.command;

public record SignUpCommand(String email, String password, String confirmedPassword, String nickname) {
}
