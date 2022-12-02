package com.mariana.demo.controller;

import com.mariana.demo.service.DemoAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/demo")
public class DemoController {

    private final DemoAppService demoAppService;

    public DemoController(DemoAppService demoAppService) {
        this.demoAppService = demoAppService;
    }

    @PostMapping("/hello")
    public ResponseEntity<String> hello(@RequestParam String token, @RequestBody DemoRequest demoRequest) {

        Assert.hasText(token, "Param <token> is required.");
        Assert.notNull(demoRequest, "Param <DemoRequest> is required.");
        Assert.hasText(demoRequest.getRequesterId(), "Param <DemoRequest.requesterId> is required.");

        String result = demoAppService.hello(demoRequest.requesterId);
        return ResponseEntity.ok(result);
    }

    private static class DemoRequest {
        private String requesterId;

        public String getRequesterId() {
            return requesterId;
        }

        public void setRequesterId(String requesterId) {
            this.requesterId = requesterId;
        }
    }

}
