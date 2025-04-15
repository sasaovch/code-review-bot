package ru.max.codereviewbot.service.command;

import java.util.Map;

import org.springframework.stereotype.Component;

import chat.tamtam.botapi.TamTamBotAPI;

@Component
public class HelpCommand extends BaseCommand {
    private String helpDescription;
    private final Map<String, Command> commandMap;

    public HelpCommand(Map<String, Command> commands, TamTamBotAPI maxApi) {
        super(maxApi);
        this.commandMap = commands;
    }

    @Override
    public void execute(long userId, String[] args) {
        if (helpDescription == null) {
            initHelpDescription();
        }
        trySendMessage(userId, helpDescription);
    }

    private void initHelpDescription() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Command value : commandMap.values()) {
            stringBuilder
                .append(value.getCommandDescription())
                .append("\n");
        }

        helpDescription = stringBuilder.toString().trim();
    }

    @Override
    public String getCommandDescription() {
        return "/help - print commands info";
    }
}
