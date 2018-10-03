package net.jackofalltrades.taterbot;

import net.jackofalltrades.taterbot.command.BotCommandParserBaseVisitor;
import net.jackofalltrades.taterbot.command.BotCommandParserVisitor;
import net.jackofalltrades.taterbot.command.Command;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(
        basePackages = {
                "net.jackofalltrades.taterbot.service",
                "net.jackofalltrades.taterbot.channel",
                "net.jackofalltrades.taterbot.event",
                "net.jackofalltrades.taterbot.util"
        })
@EnableAutoConfiguration
class ChannelMembershipIntegrationConfiguration {

    @Bean
    BotCommandParserVisitor<Command> mockParserVisitor() {
        return new BotCommandParserBaseVisitor<>();
    }

}
