package com.feedback.model;

import com.feedback.client.GrievanceResponse;
import com.feedback.requests.FeedbackRequest;
import com.feedback.requests.RatingsRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ModelEqualityTest {

    @Test
    void feedbackEqualityAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        Feedback f1 = populatedFeedback(now);
        Feedback f2 = populatedFeedback(now);

        assertThat(f1).isEqualTo(f2).hasSameHashCodeAs(f2);
        assertThat(f1.toString()).contains("grievance");

        f2.setComments("different");
        assertThat(f1).isNotEqualTo(f2);
        assertThat(f1).isNotEqualTo(null);
        assertThat(f1).isNotEqualTo("other");

        Feedback empty1 = new Feedback();
        Feedback empty2 = new Feedback();
        assertThat(empty1).isEqualTo(empty2);

        Feedback withIdOnly = new Feedback();
        withIdOnly.setId("id");
        assertThat(empty1).isNotEqualTo(withIdOnly);

        Feedback nullCitizen = populatedFeedback(now);
        nullCitizen.setCitizenId(null);
        assertThat(nullCitizen).isNotEqualTo(f1);

        Feedback nullGrievance = populatedFeedback(now);
        nullGrievance.setGrievanceId(null);
        assertThat(nullGrievance).isNotEqualTo(f1);
    }

    @Test
    void ratingsEqualityAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        Ratings r1 = populatedRating(now);
        Ratings r2 = populatedRating(now);

        assertThat(r1).isEqualTo(r2).hasSameHashCodeAs(r2);
        assertThat(r1.toString()).contains("score");

        r2.setScore(2);
        assertThat(r1).isNotEqualTo(r2);

        Ratings empty1 = new Ratings();
        Ratings empty2 = new Ratings();
        assertThat(empty1).isEqualTo(empty2);

        Ratings nullGrievance = populatedRating(now);
        nullGrievance.setGrievanceId(null);
        assertThat(nullGrievance).isNotEqualTo(r1);

        Ratings nullScore = populatedRating(now);
        nullScore.setScore(null);
        assertThat(nullScore).isNotEqualTo(r1);
    }

    @Test
    void reopenRequestEqualityAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        ReopenRequest r1 = populatedReopen(now);
        ReopenRequest r2 = populatedReopen(now);

        assertThat(r1).isEqualTo(r2).hasSameHashCodeAs(r2);
        assertThat(r1.toString()).contains("reason");

        r2.setReason("other");
        assertThat(r1).isNotEqualTo(r2);

        ReopenRequest empty1 = new ReopenRequest();
        ReopenRequest empty2 = new ReopenRequest();
        assertThat(empty1).isEqualTo(empty2);

        ReopenRequest nullGrievance = populatedReopen(now);
        nullGrievance.setGrievanceId(null);
        assertThat(nullGrievance).isNotEqualTo(r1);
    }

    @Test
    void feedbackStatsGettersAndSetters() {
        FeedbackStats stats = new FeedbackStats();
        stats.setTotalFeedbacks(1);
        stats.setTotalRatings(2);
        stats.setAverageRating(3.5);
        stats.setTotalReopenRequests(4);

        assertThat(stats.getTotalFeedbacks()).isEqualTo(1);
        assertThat(stats.getTotalRatings()).isEqualTo(2);
        assertThat(stats.getAverageRating()).isEqualTo(3.5);
        assertThat(stats.getTotalReopenRequests()).isEqualTo(4);

        FeedbackStats allArgs = new FeedbackStats(5, 6, 4.5, 7);
        assertThat(allArgs.getTotalFeedbacks()).isEqualTo(5);
    }

    @Test
    void grievanceResponseEqualsHashCodeAndResolvedFlag() {
        GrievanceResponse g1 = populatedGrievance("RESOLVED");
        GrievanceResponse g2 = populatedGrievance("RESOLVED");

        assertThat(g1).isEqualTo(g2).hasSameHashCodeAs(g2);
        assertThat(g1.toString()).contains("RESOLVED");
        assertThat(g1.isResolvedOrClosed()).isTrue();

        GrievanceResponse closed = populatedGrievance("CLOSED");
        assertThat(closed.isResolvedOrClosed()).isTrue();

        GrievanceResponse notResolved = populatedGrievance("OPEN");
        assertThat(notResolved.isResolvedOrClosed()).isFalse();
        assertThat(g1).isNotEqualTo(notResolved);
        assertThat(g1).isNotEqualTo(null);
        assertThat(g1).isNotEqualTo("other");
    }

    @Test
    void requestDtosEqualityAndHashCode() {
        FeedbackRequest fr1 = new FeedbackRequest();
        fr1.setCitizenId("c1");
        fr1.setGrievanceId("g1");
        fr1.setComments("long enough comment");
        FeedbackRequest fr2 = new FeedbackRequest();
        fr2.setCitizenId("c1");
        fr2.setGrievanceId("g1");
        fr2.setComments("long enough comment");

        assertThat(fr1).isEqualTo(fr2).hasSameHashCodeAs(fr2);
        fr2.setComments("other");
        assertThat(fr1).isNotEqualTo(fr2);

        RatingsRequest rr1 = new RatingsRequest();
        rr1.setGrievanceId("g1");
        rr1.setScore(5);
        RatingsRequest rr2 = new RatingsRequest();
        rr2.setGrievanceId("g1");
        rr2.setScore(5);
        assertThat(rr1).isEqualTo(rr2).hasSameHashCodeAs(rr2);
        rr2.setScore(4);
        assertThat(rr1).isNotEqualTo(rr2);

        com.feedback.requests.ReopenRequest reopen1 = new com.feedback.requests.ReopenRequest();
        reopen1.setGrievanceId("g1");
        reopen1.setReason("reason");
        com.feedback.requests.ReopenRequest reopen2 = new com.feedback.requests.ReopenRequest();
        reopen2.setGrievanceId("g1");
        reopen2.setReason("reason");
        assertThat(reopen1).isEqualTo(reopen2).hasSameHashCodeAs(reopen2);
        reopen2.setReason("different");
        assertThat(reopen1).isNotEqualTo(reopen2);
    }

    private Feedback populatedFeedback(LocalDateTime submittedAt) {
        Feedback feedback = new Feedback();
        feedback.setId("id");
        feedback.setGrievanceId("grievance");
        feedback.setCitizenId("citizen");
        feedback.setComments("sufficiently long comment");
        feedback.setSubmittedAt(submittedAt);
        return feedback;
    }

    private Ratings populatedRating(LocalDateTime submittedAt) {
        Ratings ratings = new Ratings();
        ratings.setId("id");
        ratings.setGrievanceId("grievance");
        ratings.setScore(5);
        ratings.setSubmittedAt(submittedAt);
        return ratings;
    }

    private ReopenRequest populatedReopen(LocalDateTime requestedAt) {
        ReopenRequest reopenRequest = new ReopenRequest();
        reopenRequest.setId("id");
        reopenRequest.setGrievanceId("grievance");
        reopenRequest.setReason("Reason that is long");
        reopenRequest.setRequestedAt(requestedAt);
        return reopenRequest;
    }

    private GrievanceResponse populatedGrievance(String status) {
        GrievanceResponse response = new GrievanceResponse();
        response.setId("g1");
        response.setStatus(status);
        return response;
    }
}
