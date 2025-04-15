package ru.max.codereviewbot.controller;

import chat.tamtam.botapi.model.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.max.codereviewbot.handler.UpdateHandler;

@RestController
public class UpdateController {

    private final UpdateHandler updateHandler;

    @Autowired
    UpdateController(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    @PostMapping(path = "/update")
    void update(@RequestBody Update update) {
        update.visit(updateHandler);
    }
}
