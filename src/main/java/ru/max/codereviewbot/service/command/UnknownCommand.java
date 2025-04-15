package ru.max.codereviewbot.service.command;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import chat.tamtam.botapi.TamTamBotAPI;

@Component
public class UnknownCommand extends BaseCommand {
    private final String helpDescription;

    @Autowired
    public UnknownCommand(@Qualifier("namesToCommands") Map<String, Command> commands, TamTamBotAPI maxApi) {
        super(maxApi);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(
            "Incorrect command. See help description\n\n"
        );

        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            stringBuilder
                .append(entry.getValue().getCommandDescription())
                .append("\n");
        }
        helpDescription = stringBuilder.toString().trim();
    }

    @Override
    public String getCommandDescription() {
        return "";
    }

    @Override
    public void execute(long userId, String[] args) {
        trySendMessage(userId, helpDescription);
    }
}
