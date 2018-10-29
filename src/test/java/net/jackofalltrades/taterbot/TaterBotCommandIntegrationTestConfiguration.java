package net.jackofalltrades.taterbot;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Configuration
@ComponentScan(
        basePackages = {
                "net.jackofalltrades.taterbot.channel",
                "net.jackofalltrades.taterbot.command",
                "net.jackofalltrades.taterbot.event",
                "net.jackofalltrades.taterbot.service",
                "net.jackofalltrades.taterbot.util"
        })
@EnableAutoConfiguration
public @interface TaterBotCommandIntegrationTestConfiguration {

}
