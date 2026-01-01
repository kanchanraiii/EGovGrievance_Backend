package com.feedback.model;

import com.feedback.requests.FeedbackRequest;
import com.feedback.requests.RatingsRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ModelRequestTest {

    @Test
    void feedbackModelGettersWork() {
        Feedback feedback = new Feedback();
        feedback.setId("f1");
        feedback.setGrievanceId("g1");
        feedback.setCitizenId("c1");
        feedback.setComments("text longer than ten chars");
        LocalDateTime now = LocalDateTime.now();
        feedback.setSubmittedAt(now);

        assertThat(feedback.getId()).isEqualTo("f1");
        assertThat(feedback.getGrievanceId()).isEqualTo("g1");
        assertThat(feedback.getCitizenId()).isEqualTo("c1");
        assertThat(feedback.getComments()).contains("text");
        assertThat(feedback.getSubmittedAt()).isEqualTo(now);
    }

    @Test
    void ratingsModelGettersWork() {
        Ratings ratings = new Ratings();
        ratings.setId("r1");
        ratings.setGrievanceId("g1");
        ratings.setScore(4);
        LocalDateTime now = LocalDateTime.now();
        ratings.setSubmittedAt(now);

        assertThat(ratings.getId()).isEqualTo("r1");
        assertThat(ratings.getGrievanceId()).isEqualTo("g1");
        assertThat(ratings.getScore()).isEqualTo(4);
        assertThat(ratings.getSubmittedAt()).isEqualTo(now);
    }

    @Test
    void reopenModelGettersWork() {
        com.feedback.model.ReopenRequest reopen = new com.feedback.model.ReopenRequest();
        reopen.setId("rr1");
        reopen.setGrievanceId("g1");
        reopen.setReason("Reason text long enough");
        LocalDateTime now = LocalDateTime.now();
        reopen.setRequestedAt(now);

        assertThat(reopen.getId()).isEqualTo("rr1");
        assertThat(reopen.getGrievanceId()).isEqualTo("g1");
        assertThat(reopen.getReason()).contains("Reason");
        assertThat(reopen.getRequestedAt()).isEqualTo(now);
    }

    @Test
    void requestDtosGettersWork() {
        FeedbackRequest feedbackRequest = new FeedbackRequest();
        feedbackRequest.setGrievanceId("g1");
        feedbackRequest.setCitizenId("c1");
        feedbackRequest.setComments("Some comments long enough");

        RatingsRequest ratingsRequest = new RatingsRequest();
        ratingsRequest.setGrievanceId("g1");
        ratingsRequest.setScore(5);

        com.feedback.requests.ReopenRequest reopenRequest = new com.feedback.requests.ReopenRequest();
        reopenRequest.setGrievanceId("g1");
        reopenRequest.setReason("Reopen reason");

        assertThat(feedbackRequest.getCitizenId()).isEqualTo("c1");
        assertThat(feedbackRequest.getComments()).contains("Some comments");
        assertThat(ratingsRequest.getScore()).isEqualTo(5);
        assertThat(reopenRequest.getReason()).contains("Reopen");
    }
}
