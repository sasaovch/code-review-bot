package ru.max.codereviewbot.service.command;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.queries.SendMessageQuery;
import ru.max.codereviewbot.domain.PullRequest;
import ru.max.codereviewbot.domain.User;
import ru.max.codereviewbot.persist.UserRepository;
import ru.max.codereviewbot.service.PullRequestService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.max.codereviewbot.testutil.RandomEntity.randomUser;

@SpringBootTest
class StartReviewCommandTest {

    private static final String PR_URL = "https://url.ru/pull-request/123";

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    TamTamBotAPI maxApi;

    @MockitoBean
    private PullRequestService pullRequestService;

    @Autowired
    StartReviewCommand startReviewCommand;

    @Captor
    ArgumentCaptor<PullRequest> pullRequestCaptor;

    @Test
    void execute_shouldSaveReviewAndNotifyEveryone_ifEverythingCorrect() throws ClientException, APIException {
        User author = randomUser();
        when(userRepository.get(author.id())).thenReturn(Optional.of(author));

        User reviewer = randomUser();
        when(userRepository.findAll()).thenReturn(List.of(author, reviewer));

        SendMessageQuery query = mock(SendMessageQuery.class);
        when(maxApi.sendMessage(any())).thenReturn(query);

        SendMessageQuery authorQuery = mock(SendMessageQuery.class);
        when(query.userId(author.id())).thenReturn(authorQuery);

        SendMessageQuery reviewerQuery = mock(SendMessageQuery.class);
        when(query.userId(reviewer.id())).thenReturn(reviewerQuery);

        startReviewCommand.execute(author.id(), new String[]{PR_URL});

        // PR saved and is correct
        verify(pullRequestService, times(1)).save(pullRequestCaptor.capture());

        PullRequest pullRequest = pullRequestCaptor.getValue();
        assertEquals(author.id(), pullRequest.authorId());
        assertEquals(List.of(reviewer.id()), pullRequest.assignees());
        assertEquals(PR_URL, pullRequest.url());

        // Author notified
        verify(query, times(1)).userId(author.id());
        verify(authorQuery, times(1)).execute();

        // Reviewer notified
        verify(query, times(1)).userId(reviewer.id());
        verify(reviewerQuery, times(1)).execute();
    }

    @Test
    void execute_shouldInformAuthorAndDontSavePr_ifNoAvailableReviewers() throws ClientException, APIException {
        User author = randomUser();
        when(userRepository.get(author.id())).thenReturn(Optional.of(author));

        SendMessageQuery query = mock(SendMessageQuery.class);
        when(maxApi.sendMessage(any())).thenReturn(query);

        SendMessageQuery authorQuery = mock(SendMessageQuery.class);
        when(query.userId(author.id())).thenReturn(authorQuery);

        when(userRepository.findAll()).thenReturn(List.of());

        startReviewCommand.execute(author.id(), new String[]{PR_URL});

        verify(pullRequestService, never()).save(any(PullRequest.class));

        verify(query, times(1)).userId(author.id());
        verify(authorQuery, times(1)).execute();
    }

    @Test
    void execute_shouldInformAuthorAndDontSavePr_ifNoArgsProvided() throws ClientException, APIException {
        User author = randomUser();
        when(userRepository.get(author.id())).thenReturn(Optional.of(author));

        SendMessageQuery query = mock(SendMessageQuery.class);
        when(maxApi.sendMessage(any())).thenReturn(query);

        SendMessageQuery authorQuery = mock(SendMessageQuery.class);
        when(query.userId(author.id())).thenReturn(authorQuery);

        startReviewCommand.execute(author.id(), new String[0]);

        verify(pullRequestService, never()).save(any(PullRequest.class));

        verify(query, times(1)).userId(author.id());
        verify(authorQuery, times(1)).execute();
    }
}