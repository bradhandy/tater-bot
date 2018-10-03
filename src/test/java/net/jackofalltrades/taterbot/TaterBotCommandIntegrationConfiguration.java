package net.jackofalltrades.taterbot;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
class TaterBotCommandIntegrationConfiguration {

}
