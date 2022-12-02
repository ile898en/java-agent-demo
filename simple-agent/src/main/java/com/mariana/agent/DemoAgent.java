package com.mariana.agent;

import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class DemoAgent {

    private static final String SPRING_CONTROLLER_ANNOTATION_1 = "org.springframework.stereotype.Controller";
    private static final String SPRING_CONTROLLER_ANNOTATION_2 = "org.springframework.web.bind.annotation.RestController";

    public static void premain(String arg, Instrumentation instrumentation) {
        new AgentBuilder.Default()
                .type(isAnnotatedWith(
                        named(SPRING_CONTROLLER_ANNOTATION_1).or(named(SPRING_CONTROLLER_ANNOTATION_2))
                ))
                .transform()
    }

}
