package ru.max.codereviewbot.service.command;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.max.codereviewbot.domain.PullRequestStatus.WAITING_FOR_REVIEW;
import static ru.max.codereviewbot.testutil.RandomEntity.randomUser;

@SpringBootTest
class SubmitReviewCommandTest {

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    PullRequestService pullRequestService;

    @MockitoBean
    TamTamBotAPI maxApi;

    @Autowired
    SubmitReviewCommand submitReviewCommand;

    @Test
    void execute_shouldNotifyAuthorAboutReviewComments() throws ClientException, APIException {
        User author = randomUser();
        when(userRepository.get(author.id())).thenReturn(Optional.of(author));

        User reviewer = randomUser();

        String url = "https://gitlab.ru/repository/pull/0";
        PullRequest pullRequest = new PullRequest(0, url, WAITING_FOR_REVIEW, author.id(), List.of(reviewer.id()));
        when(pullRequestService.findByIdOrUrl(pullRequest.url())).thenReturn(pullRequest);

        SendMessageQuery query = mock(SendMessageQuery.class);
        when(maxApi.sendMessage(any())).thenReturn(query);

        SendMessageQuery authorQuery = mock(SendMessageQuery.class);
        when(query.userId(author.id())).thenReturn(authorQuery);

        submitReviewCommand.execute(reviewer.id(), new String[]{url});

        verify(query, times(1)).userId(author.id());
        verify(authorQuery, times(1)).execute();
    }

}