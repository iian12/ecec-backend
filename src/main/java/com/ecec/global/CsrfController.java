package com.ecec.global;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class CsrfController {

    /*
     * React 흐름
     * 1. GET /api/v1/auth/csrf
     * 2. XSRF-TOKEN 쿠키 확인
     * 3. POST /api/v1/auth/login
     * 4. X-XSRF-TOKEN 헤더에 쿠키 값 전송
     * 5. credentials: "include" 설정
     * ex)
     * const api = axios.create({
       baseURL: "http://localhost:8080",
       withCredentials: true,
       xsrfCookieName: "XSRF-TOKEN",
       xsrfHeaderName: "X-XSRF-TOKEN",
       });
    */
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }
}
