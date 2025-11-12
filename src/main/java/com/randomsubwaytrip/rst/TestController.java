package com.randomsubwaytrip.rst; // <- 이 부분이 수정되었습니다.

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

// 1. 이 클래스가 REST API의 컨트롤러임을 선언합니다.
@RestController
// 2. 이 컨트롤러의 모든 API는 공통적으로 /api/test 경로를 갖게 됩니다.
@RequestMapping("/api/test")
public class TestController {

    // 3. HTTP GET 요청 http://localhost:8080/api/test/hello)을 처리합니다.
    @GetMapping("/hello")
    public Map<String, String> getHello() {
        // 4. JSON 형태로 {"message": "Hello World!"}를 반환합니다.
        return Collections.singletonMap("message", "Hello World!");
    }
}