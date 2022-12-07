package com.mariana.agent.core.logging.core;

import com.google.common.collect.ImmutableMap;
import com.mariana.agent.core.config.Config;
import com.mariana.agent.core.logging.core.converters.*;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class ParserTest {

    private final String PATTERN = Config.Logging.PATTERN;
    private final Map<String, Class<? extends Converter>> CONVERTER_MAP = ImmutableMap.of(
            "thread", ThreadConverter.class,
            "level", LevelConverter.class,
            "agent_name", AgentNameConverter.class,
            "timestamp", DateConverter.class,
            "msg", MessageConverter.class,
            "throwable", ThrowableConverter.class,
            "class", ClassConverter.class
    );

    @Test
    public void testParse() {
        Parser parser = new Parser(PATTERN, CONVERTER_MAP);
        List<Converter> converters = parser.parse();
        System.out.println(converters.size());
    }

}
