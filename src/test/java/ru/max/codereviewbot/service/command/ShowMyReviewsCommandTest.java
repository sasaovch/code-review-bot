package ru.max.codereviewbot.service.command;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.NewMessageBody;
import chat.tamtam.botapi.queries.SendMessageQuery;
import ru.max.codereviewbot.domain.PullRequest;
import ru.max.codereviewbot.domain.PullRequestStatus;
import ru.max.codereviewbot.domain.User;
import ru.max.codereviewbot.persist.PullRequestRepository;
import ru.max.codereviewbot.persist.UserRepository;
import ru.max.codereviewbot.service.PullRequestService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.max.codereviewbot.testutil.RandomEntity.randomUser;

@SpringBootTest
public class ShowMyReviewsCommandTest {
    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    PullRequestService pullRequestService;

    @MockitoBean
    TamTamBotAPI maxApi;

    @Autowired
    ShowMyReviewsCommand showMyReviewsCommand;

    @Captor
    ArgumentCaptor<NewMessageBody> newMessageBodyArgumentCaptor;

    @Test
    void showMyReviews() throws ClientException {
        User author = randomUser();

        User reviewer1 = randomUser();
        User reviewer2 = randomUser();

        PullRequest pr1 = new PullRequest(1, "http://pr.1", PullRequestStatus.WAITING_FOR_APPROVAL, author.id(), List.of(reviewer1.id()));
        PullRequest pr2 = new PullRequest(2, "http://pr.2", PullRequestStatus.READY_TO_BE_MERGED, author.id(), List.of(reviewer2.id()));

        when(pullRequestService.findAllByAuthorId(ArgumentMatchers.eq(author.id()))).thenReturn(List.of(pr1, pr2));
        when(userRepository.findAllByIds(anySet())).thenReturn(List.of(reviewer1, reviewer2));

        SendMessageQuery query = mock(SendMessageQuery.class);
        when(maxApi.sendMessage(any())).thenReturn(query);

        SendMessageQuery anotherQuery = mock(SendMessageQuery.class);
        when(query.userId(author.id())).thenReturn(anotherQuery);

        showMyReviewsCommand.execute(author.id(), new String[0]);

        verify(maxApi, times(1)).sendMessage(newMessageBodyArgumentCaptor.capture());

        String expectedText =
            "Review with id - 1. Status - WAITING_FOR_APPROVAL. Assigned to @" + reviewer1.username() + " Url - http://pr.1\n" +
                "Review with id - 2. Status - READY_TO_BE_MERGED. Assigned to @" + reviewer2.username() + " Url - http://pr.2\n";

        NewMessageBody newMessageBody = newMessageBodyArgumentCaptor.getValue();
        assertEquals(expectedText, newMessageBody.getText());
    }

    @Test
    void showMyReviewsWhenEmpty() throws ClientException {
        User author = randomUser();

        when(pullRequestService.findAllByAuthorId(ArgumentMatchers.eq(author.id()))).thenReturn(List.of());

        SendMessageQuery query = mock(SendMessageQuery.class);
        when(maxApi.sendMessage(any())).thenReturn(query);

        SendMessageQuery anotherQuery = mock(SendMessageQuery.class);
        when(query.userId(author.id())).thenReturn(anotherQuery);

        showMyReviewsCommand.execute(author.id(), new String[0]);

        verify(maxApi, times(1)).sendMessage(newMessageBodyArgumentCaptor.capture());

        String expectedText = "You don't have any pull requests";

        NewMessageBody newMessageBody = newMessageBodyArgumentCaptor.getValue();
        assertEquals(expectedText, newMessageBody.getText());
    }
}
