package ru.max.codereviewbot.service.command;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.queries.SendMessageQuery;
import ru.max.codereviewbot.domain.PullRequest;
import ru.max.codereviewbot.domain.PullRequestStatus;
import ru.max.codereviewbot.domain.User;
import ru.max.codereviewbot.persist.UserRepository;
import ru.max.codereviewbot.service.PullRequestService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.max.codereviewbot.testutil.RandomEntity.randomUser;

@SpringBootTest
public class ApproveReviewCommandTest {
    private static final String PR_URL = "https://url.ru/pull-request/123";

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    PullRequestService pullRequestService;

    @MockitoBean
    TamTamBotAPI maxApi;

    @Autowired
    ApproveReviewCommand approveReviewCommand;

    @Captor
    ArgumentCaptor<PullRequest> pullRequestCaptor;

    @Test
    void approveReviewAndNotify() throws ClientException, APIException {
        User author = randomUser();
        User reviewer = randomUser();
        PullRequest pr = new PullRequest(1, PR_URL, PullRequestStatus.WAITING_FOR_APPROVAL, author.id(), List.of(reviewer.id()));

        when(userRepository.get(ArgumentMatchers.eq(author.id()))).thenReturn(Optional.of(author));
        when(userRepository.get(ArgumentMatchers.eq(reviewer.id()))).thenReturn(Optional.of(reviewer));

        when(pullRequestService.findByIdOrUrl(ArgumentMatchers.eq(PR_URL))).thenReturn(pr);

        SendMessageQuery query = mock(SendMessageQuery.class);
        when(maxApi.sendMessage(any())).thenReturn(query);

        SendMessageQuery reviewerQuery = mock(SendMessageQuery.class);
        when(query.userId(reviewer.id())).thenReturn(reviewerQuery);

        SendMessageQuery authorQuery = mock(SendMessageQuery.class);
        when(query.userId(author.id())).thenReturn(authorQuery);

        approveReviewCommand.execute(reviewer.id(), new String[]{PR_URL});

        verify(pullRequestService, times(1)).save(pullRequestCaptor.capture());

        PullRequest pullRequest = pullRequestCaptor.getValue();
        assertEquals(PullRequestStatus.READY_TO_BE_MERGED, pullRequest.status());

        verify(query, times(1)).userId(author.id());
        verify(authorQuery, times(1)).execute();

        verify(query, times(1)).userId(reviewer.id());
        verify(reviewerQuery, times(1)).execute();
    }

    @Test
    void tryToApproveByWrongUser() throws ClientException, APIException {
        User wrongUser = randomUser();
        PullRequest pr = new PullRequest(1, PR_URL, PullRequestStatus.WAITING_FOR_APPROVAL, wrongUser.id() + 1L, List.of(wrongUser.id() + 2L));

        when(pullRequestService.findByIdOrUrl(ArgumentMatchers.eq(PR_URL))).thenReturn(pr);

        SendMessageQuery query = mock(SendMessageQuery.class);
        when(maxApi.sendMessage(any())).thenReturn(query);

        SendMessageQuery wrongQuery = mock(SendMessageQuery.class);
        when(query.userId(wrongUser.id())).thenReturn(wrongQuery);

        approveReviewCommand.execute(wrongUser.id(), new String[]{PR_URL});

        verify(query, times(1)).userId(wrongUser.id());
        verify(wrongQuery, times(1)).execute();
    }
}
