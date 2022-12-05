package com.mariana.agent.matchers;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class SpringControllerMatcher {

    private static final String SPRING_CONTROLLER_ANNOTATION_1 = "org.springframework.stereotype.Controller";
    private static final String SPRING_CONTROLLER_ANNOTATION_2 = "org.springframework.web.bind.annotation.RestController";

    public static ElementMatcher.Junction<? super TypeDescription> get() {
        return isAnnotatedWith(
                named(SPRING_CONTROLLER_ANNOTATION_1).or(named(SPRING_CONTROLLER_ANNOTATION_2))
        );
    }

}
