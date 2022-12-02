package com.mariana.demo.controller;

import com.mariana.demo.service.DemoAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/demo")
public class DemoController {

    private final DemoAppService demoAppService;

    public DemoController(DemoAppService demoAppService) {
        this.demoAppService = demoAppService;
    }

    @PostMapping("/hello")
    public ResponseEntity<?> hello(@RequestParam String token, @RequestBody DemoRequest demoRequest) {
        String result = demoAppService.hello();
        return ResponseEntity.ok(result);
    }

    static class DemoRequest {
        private String requester;

        public String getRequester() {
            return requester;
        }

        public void setRequester(String requester) {
            this.requester = requester;
        }
    }

}
