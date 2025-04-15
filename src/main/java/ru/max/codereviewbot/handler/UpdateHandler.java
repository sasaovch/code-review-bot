package ru.max.codereviewbot.handler;

import chat.tamtam.botapi.model.BotAddedToChatUpdate;
import chat.tamtam.botapi.model.BotRemovedFromChatUpdate;
import chat.tamtam.botapi.model.BotStartedUpdate;
import chat.tamtam.botapi.model.ChatTitleChangedUpdate;
import chat.tamtam.botapi.model.MessageCallbackUpdate;
import chat.tamtam.botapi.model.MessageChatCreatedUpdate;
import chat.tamtam.botapi.model.MessageConstructedUpdate;
import chat.tamtam.botapi.model.MessageConstructionRequest;
import chat.tamtam.botapi.model.MessageCreatedUpdate;
import chat.tamtam.botapi.model.MessageEditedUpdate;
import chat.tamtam.botapi.model.MessageRemovedUpdate;
import chat.tamtam.botapi.model.Update;
import chat.tamtam.botapi.model.User;
import chat.tamtam.botapi.model.UserAddedToChatUpdate;
import chat.tamtam.botapi.model.UserRemovedFromChatUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.max.codereviewbot.service.Bot;
import ru.max.codereviewbot.service.command.Command;
import ru.max.codereviewbot.service.CommandAndArgs;
import ru.max.codereviewbot.service.CommandReader;

import java.util.Optional;

@Component
public class UpdateHandler implements Update.Visitor {

    private static final Logger log = LoggerFactory.getLogger(UpdateHandler.class);
    private final Bot bot;
    private final CommandReader commandReader;

    @Autowired
    public UpdateHandler(Bot bot, CommandReader commandReader) {
        this.bot = bot;
        this.commandReader = commandReader;
    }

    @Override
    public void visit(MessageCreatedUpdate update) {
        User user = update.getMessage().getSender();
        String messageText = update.getMessage().getBody().getText();

        Optional<CommandAndArgs> commandAndArgs = commandReader.read(messageText);
        if (commandAndArgs.isEmpty()) {
            doNothing();
            return;
        }

        Command command = commandAndArgs.get().command();
        String[] args = commandAndArgs.get().args();
        bot.executeCommand(user.getUserId(), command, args);
    }

    @Override
    public void visit(MessageCallbackUpdate update) {
        visitDefault(update);
    }

    @Override
    public void visit(MessageEditedUpdate update) {
        visitDefault(update);
    }

    @Override
    public void visit(MessageRemovedUpdate update) {
        visitDefault(update);
    }

    @Override
    public void visit(BotAddedToChatUpdate update) {
        visitDefault(update);
    }

    @Override
    public void visit(BotRemovedFromChatUpdate update) {
        visitDefault(update);
    }

    @Override
    public void visit(UserAddedToChatUpdate update) {
        visitDefault(update);
    }

    @Override
    public void visit(UserRemovedFromChatUpdate update) {
        visitDefault(update);
    }

    @Override
    public void visit(BotStartedUpdate update) {
        User user = update.getUser();
        bot.start(user.getUserId(), user.getName(), user.getUsername());
    }

    @Override
    public void visit(ChatTitleChangedUpdate update) {
        visitDefault(update);
    }

    @Override
    public void visit(MessageConstructionRequest update) {
        visitDefault(update);
    }

    @Override
    public void visit(MessageConstructedUpdate update) {
        visitDefault(update);
    }

    @Override
    public void visit(MessageChatCreatedUpdate update) {
        visitDefault(update);
    }

    @Override
    public void visitDefault(Update update) {
        log.warn("Unsupported update type: {}", update.getClass().getSimpleName());
    }

    private void doNothing() {}
}
