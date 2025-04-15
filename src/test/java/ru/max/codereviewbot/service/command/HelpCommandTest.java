package ru.max.codereviewbot.service.command;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.NewMessageBody;
import chat.tamtam.botapi.queries.SendMessageQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class HelpCommandTest {
    @MockitoBean
    TamTamBotAPI maxApi;

    @Autowired
    Map<String, Command> commandMap;

    HelpCommand helpCommand;

    @Captor
    ArgumentCaptor<NewMessageBody> newMessageBodyArgumentCaptor;

    @BeforeEach
    void setUp() {
        helpCommand = new HelpCommand(commandMap, maxApi);
    }

    @Test
    void helpTest() throws ClientException {
        SendMessageQuery query = mock(SendMessageQuery.class);
        when(maxApi.sendMessage(any())).thenReturn(query);

        SendMessageQuery anotherQuery = mock(SendMessageQuery.class);
        when(query.userId(1L)).thenReturn(anotherQuery);

        helpCommand.execute(1L, new String[0]);

        verify(maxApi, times(1)).sendMessage(newMessageBodyArgumentCaptor.capture());

        String expectedText =
            """
                /approve {url|id} - approve PR
                /help - print commands info
                /show_my_reviews - show PRs waiting for your review
                /start_review {url} - starts review process
                /submit_review {url|id} - submit review comments
                """.trim();

        NewMessageBody newMessageBody = newMessageBodyArgumentCaptor.getValue();
        assertEquals(expectedText, newMessageBody.getText());
    }
}
