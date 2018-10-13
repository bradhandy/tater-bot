package net.jackofalltrades.taterbot.command;

import org.springframework.context.ApplicationContext;

/**
 * Implementation of BotCommandParserBaseVisitor for creating the service commands.
 *
 * @author bhandy
 */
class ServiceCommandParserVisitor extends BotCommandParserBaseVisitor<Command> {

    private final ApplicationContext applicationContext;

    private Command currentCommand;

    public ServiceCommandParserVisitor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Command visitService_action(BotCommandParser.Service_actionContext ctx) {
        currentCommand = applicationContext.getBean(String.format("service-%s", ctx.getText()), Command.class);
        return currentCommand;
    }

    @Override
    public Command visitService_help_action(BotCommandParser.Service_help_actionContext ctx) {
        return null;
    }

    @Override
    public Command visitService_list_action(BotCommandParser.Service_list_actionContext ctx) {
        return null;
    }

    @Override
    public Command visitService_type(BotCommandParser.Service_typeContext ctx) {
        if (currentCommand instanceof ServiceNameAware) {
            ((ServiceNameAware) currentCommand).setServiceName(ctx.getText());
            return currentCommand;
        }

        return null;
    }

}
