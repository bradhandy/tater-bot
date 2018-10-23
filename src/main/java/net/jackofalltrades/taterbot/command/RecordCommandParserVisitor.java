package net.jackofalltrades.taterbot.command;

import org.springframework.context.ApplicationContext;

class RecordCommandParserVisitor extends BotCommandParserBaseVisitor<Command> {

    private final ApplicationContext applicationContext;

    RecordCommandParserVisitor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Command visitRecord_action(BotCommandParser.Record_actionContext ctx) {
        String action = ctx.getText();
        if ("help".equals(action)) {
            return applicationContext.getBean(RecordHelpCommand.NAME, Command.class);
        } else if ("start".equals(action) || "stop".equals(action)) {
            return applicationContext.getBean(String.format("record-%s", action), Command.class);
        }

        return null;
    }

}

