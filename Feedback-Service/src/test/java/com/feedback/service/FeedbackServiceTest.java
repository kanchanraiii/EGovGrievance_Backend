package com.feedback.service;

import com.feedback.client.GrievanceClient;
import com.feedback.client.GrievanceResponse;
import com.feedback.exception.ConflictException;
import com.feedback.exception.ServiceException;
import com.feedback.exception.ResourceNotFoundException;
import com.feedback.model.Feedback;
import com.feedback.model.Ratings;
import com.feedback.model.ReopenRequest;
import com.feedback.repository.FeedbackRepository;
import com.feedback.repository.RatingsRepository;
import com.feedback.repository.ReopenRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private RatingsRepository ratingsRepository;

    @Mock
    private ReopenRequestRepository reopenRequestRepository;

    @Mock
    private GrievanceClient grievanceClient;

    @InjectMocks
    private FeedbackService feedbackService;

    private GrievanceResponse resolvedGrievance() {
        GrievanceResponse response = new GrievanceResponse();
        response.setId("g1");
        response.setStatus("RESOLVED");
        return response;
    }

    @Test
    void submitFeedback_savesWhenNotExisting() {
        Feedback feedback = new Feedback();
        feedback.setGrievanceId("g1");
        feedback.setCitizenId("c1");
        feedback.setComments("This is a valid comment text");

        when(grievanceClient.getGrievanceById("g1")).thenReturn(Mono.just(resolvedGrievance()));
        when(feedbackRepository.existsByGrievanceId("g1")).thenReturn(Mono.just(false));
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
            Feedback saved = invocation.getArgument(0);
            saved.setId("f1");
            return Mono.just(saved);
        });

        StepVerifier.create(feedbackService.submitFeedback(feedback))
                .assertNext(saved -> {
                    assertThat(saved.getId()).isEqualTo("f1");
                    assertThat(saved.getSubmittedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void submitFeedback_rejectsWhenNotResolved() {
        Feedback feedback = new Feedback();
        feedback.setGrievanceId("g1");
        feedback.setCitizenId("c1");
        feedback.setComments("This is a valid comment text");

        GrievanceResponse unresolved = new GrievanceResponse();
        unresolved.setId("g1");
        unresolved.setStatus("IN_PROGRESS");

        when(grievanceClient.getGrievanceById("g1")).thenReturn(Mono.just(unresolved));

        StepVerifier.create(feedbackService.submitFeedback(feedback))
                .expectError(ServiceException.class)
                .verify();
    }

    @Test
    void submitFeedback_rejectsWhenDuplicate() {
        Feedback feedback = new Feedback();
        feedback.setGrievanceId("g1");
        feedback.setCitizenId("c1");
        feedback.setComments("This is a valid comment text");

        when(grievanceClient.getGrievanceById("g1")).thenReturn(Mono.just(resolvedGrievance()));
        when(feedbackRepository.existsByGrievanceId("g1")).thenReturn(Mono.just(true));

        StepVerifier.create(feedbackService.submitFeedback(feedback))
                .expectError(ConflictException.class)
                .verify();
    }

    @Test
    void submitRating_rejectsOutOfRange() {
        Ratings rating = new Ratings();
        rating.setGrievanceId("g1");
        rating.setScore(6);

        StepVerifier.create(feedbackService.submitRating(rating))
                .expectError(ServiceException.class)
                .verify();
    }

    @Test
    void submitRating_rejectsBelowRange() {
        Ratings rating = new Ratings();
        rating.setGrievanceId("g1");
        rating.setScore(0);

        StepVerifier.create(feedbackService.submitRating(rating))
                .expectError(ServiceException.class)
                .verify();
    }

    @Test
    void submitRating_rejectsWhenNotResolved() {
        Ratings rating = new Ratings();
        rating.setGrievanceId("g1");
        rating.setScore(4);

        GrievanceResponse unresolved = new GrievanceResponse();
        unresolved.setId("g1");
        unresolved.setStatus("SUBMITTED");

        when(grievanceClient.getGrievanceById("g1")).thenReturn(Mono.just(unresolved));

        StepVerifier.create(feedbackService.submitRating(rating))
                .expectError(ServiceException.class)
                .verify();
    }

    @Test
    void submitRating_rejectsWhenDuplicate() {
        Ratings rating = new Ratings();
        rating.setGrievanceId("g1");
        rating.setScore(4);

        when(grievanceClient.getGrievanceById("g1")).thenReturn(Mono.just(resolvedGrievance()));
        when(ratingsRepository.existsByGrievanceId("g1")).thenReturn(Mono.just(true));

        StepVerifier.create(feedbackService.submitRating(rating))
                .expectError(ConflictException.class)
                .verify();
    }

    @Test
    void submitRating_savesWhenValidAndUnique() {
        Ratings rating = new Ratings();
        rating.setGrievanceId("g1");
        rating.setScore(5);

        when(grievanceClient.getGrievanceById("g1")).thenReturn(Mono.just(resolvedGrievance()));
        when(ratingsRepository.existsByGrievanceId("g1")).thenReturn(Mono.just(false));
        when(ratingsRepository.save(any(Ratings.class))).thenAnswer(invocation -> {
            Ratings saved = invocation.getArgument(0);
            saved.setId("r1");
            return Mono.just(saved);
        });

        StepVerifier.create(feedbackService.submitRating(rating))
                .assertNext(saved -> {
                    assertThat(saved.getId()).isEqualTo("r1");
                    assertThat(saved.getSubmittedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void requestReopen_createsAndUpdatesStatus() {
        ReopenRequest request = new ReopenRequest();
        request.setGrievanceId("g1");
        request.setReason("Need further review");

        when(grievanceClient.getGrievanceById("g1")).thenReturn(Mono.just(resolvedGrievance()));
        when(reopenRequestRepository.existsByGrievanceId("g1")).thenReturn(Mono.just(false));
        when(reopenRequestRepository.save(any(ReopenRequest.class))).thenAnswer(invocation -> {
            ReopenRequest saved = invocation.getArgument(0);
            saved.setId("rr1");
            return Mono.just(saved);
        });
        when(grievanceClient.markAsReopened(eq("g1"), any())).thenReturn(Mono.empty());

        StepVerifier.create(feedbackService.requestReopen(request))
                .assertNext(saved -> {
                    assertThat(saved.getId()).isEqualTo("rr1");
                    assertThat(saved.getRequestedAt()).isNotNull();
                })
                .verifyComplete();

        verify(grievanceClient).markAsReopened(eq("g1"), eq("Need further review"));
    }

    @Test
    void requestReopen_rejectsWhenNotResolved() {
        ReopenRequest request = new ReopenRequest();
        request.setGrievanceId("g1");
        request.setReason("Need further review");

        GrievanceResponse unresolved = new GrievanceResponse();
        unresolved.setId("g1");
        unresolved.setStatus("IN_PROGRESS");

        when(grievanceClient.getGrievanceById("g1")).thenReturn(Mono.just(unresolved));

        StepVerifier.create(feedbackService.requestReopen(request))
                .expectError(ServiceException.class)
                .verify();
    }

    @Test
    void requestReopen_returnsExistingWhenAlreadyPresent() {
        ReopenRequest existing = new ReopenRequest();
        existing.setId("rr1");
        existing.setGrievanceId("g1");
        existing.setReason("Existing reason");

        when(grievanceClient.getGrievanceById("g1")).thenReturn(Mono.just(resolvedGrievance()));
        when(reopenRequestRepository.existsByGrievanceId("g1")).thenReturn(Mono.just(true));
        when(reopenRequestRepository.findByGrievanceId("g1")).thenReturn(Mono.just(existing));
        when(grievanceClient.markAsReopened("g1", "Existing reason")).thenReturn(Mono.empty());

        StepVerifier.create(feedbackService.requestReopen(existing))
                .expectNext(existing)
                .verifyComplete();
    }

    @Test
    void requestReopen_existingMissingRecordThrowsConflict() {
        ReopenRequest request = new ReopenRequest();
        request.setGrievanceId("g1");
        request.setReason("reason");

        when(grievanceClient.getGrievanceById("g1")).thenReturn(Mono.just(resolvedGrievance()));
        when(reopenRequestRepository.existsByGrievanceId("g1")).thenReturn(Mono.just(true));
        when(reopenRequestRepository.findByGrievanceId("g1")).thenReturn(Mono.empty());

        StepVerifier.create(feedbackService.requestReopen(request))
                .expectError(com.feedback.exception.ConflictException.class)
                .verify();
    }

    @Test
    void getFeedbackByGrievance_returnsFluxFromRepository() {
        Feedback f1 = new Feedback();
        f1.setId("f1");
        f1.setGrievanceId("g1");
        f1.setSubmittedAt(LocalDateTime.now());

        Feedback f2 = new Feedback();
        f2.setId("f2");
        f2.setGrievanceId("g1");
        f2.setSubmittedAt(LocalDateTime.now());

        when(feedbackRepository.findByGrievanceId("g1")).thenReturn(Flux.just(f1, f2));

        StepVerifier.create(feedbackService.getFeedbackByGrievance("g1"))
                .expectNext(f1)
                .expectNext(f2)
                .verifyComplete();
    }

    @Test
    void getReopenRequest_throwsWhenMissing() {
        when(reopenRequestRepository.findByGrievanceId("missing")).thenReturn(Mono.empty());

        StepVerifier.create(feedbackService.getReopenRequest("missing"))
                .verifyError(ResourceNotFoundException.class);
    }

    @Test
    void getStats_aggregatesCounts() {
        when(feedbackRepository.count()).thenReturn(Mono.just(2L));
        when(ratingsRepository.count()).thenReturn(Mono.just(3L));
        when(ratingsRepository.findAll()).thenReturn(Flux.fromIterable(java.util.List.of(
                ratingWithScore(4),
                ratingWithScore(2),
                ratingWithScore(5)
        )));
        when(reopenRequestRepository.count()).thenReturn(Mono.just(1L));

        StepVerifier.create(feedbackService.getStats())
                .assertNext(stats -> {
                    assertThat(stats.getTotalFeedbacks()).isEqualTo(2);
                    assertThat(stats.getTotalRatings()).isEqualTo(3);
                    assertThat(stats.getAverageRating()).isEqualTo(11.0 / 3);
                    assertThat(stats.getTotalReopenRequests()).isEqualTo(1);
                })
                .verifyComplete();
    }

    private Ratings ratingWithScore(int score) {
        Ratings ratings = new Ratings();
        ratings.setScore(score);
        return ratings;
    }
}
