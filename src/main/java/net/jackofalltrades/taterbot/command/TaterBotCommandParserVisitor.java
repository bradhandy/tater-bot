package net.jackofalltrades.taterbot.command;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import net.jackofalltrades.taterbot.event.EventContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.RuleNode;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
class TaterBotCommandParserVisitor extends BotCommandParserBaseVisitor<Command> implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Command visitRaw_command(BotCommandParser.Raw_commandContext parserRuleContent) {
        if (EventContext.isGroupEvent() && prefixIsMissing(parserRuleContent)) {
            return null;
        }

        return super.visitRaw_command(parserRuleContent);
    }

    @Override
    public Command visitHelp_command(BotCommandParser.Help_commandContext ctx) {
        return retrieveCommand(HelpCommand.NAME);
    }

    @Override
    public Command visitRecord_command(BotCommandParser.Record_commandContext ctx) {
        BotCommandParser.Record_actionContext recordActionContext =
                ctx.getRuleContext(BotCommandParser.Record_actionContext.class, 0);
        if (recordActionContext != null) {
            return recordActionContext.getText().equals("help") ? retrieveCommand(RecordHelpCommand.NAME) : null;
        }

        return null;
    }

    @Override
    public Command visitService_command(BotCommandParser.Service_commandContext ctx) {
        if (EventContext.isGroupEvent()) {
            BotCommandParser.Service_actionContext serviceActionContext =
                    ctx.getRuleContext(BotCommandParser.Service_actionContext.class, 0);
            String action = Optional.fromNullable(serviceActionContext)
                    .transform((ruleContext) -> ruleContext.getText())
                    .or("");
            if (action.equals("status")) {
                Command statusCommand = applicationContext.getBean(ChannelServiceStatusCommand.NAME, Command.class);
                BotCommandParser.Service_typeContext serviceTypeContext =
                        ctx.getRuleContext(BotCommandParser.Service_typeContext.class, 0);
                String serviceName = Optional.fromNullable(serviceTypeContext)
                        .transform((ruleContext) -> ruleContext.getText())
                        .or("");
                if (!Strings.isNullOrEmpty(serviceName)) {
                    ((ChannelServiceStatusCommand) statusCommand).setServiceName(serviceName);
                    return statusCommand;
                }
            }
        }

        return null;
    }

    @Override
    protected Command defaultResult() {
        return retrieveCommand(UnknownCommand.NAME);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Command retrieveCommand(String name) {
        return applicationContext.getBean(name, Command.class);
    }

    private boolean prefixIsMissing(ParserRuleContext context) {
        ParserRuleContext scopeContext = context;
        while (scopeContext != null && scopeContext.getToken(BotCommandLexer.PREFIX, 0) == null) {
            scopeContext = scopeContext.getParent();
        }

        return scopeContext == null;
    }

    @Override
    protected boolean shouldVisitNextChild(RuleNode node, Command currentResult) {
        return currentResult == null || currentResult.getName().equals(UnknownCommand.NAME);
    }

}
