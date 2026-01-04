package com.feedback.controller;

import com.feedback.config.SecurityConfig;
import com.feedback.model.Feedback;
import com.feedback.model.FeedbackStats;
import com.feedback.model.Ratings;
import com.feedback.model.ReopenRequest;
import com.feedback.service.FeedbackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest(MainController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "jwt.secret=test-jwt-secret-which-is-long-enough")
class MainControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private FeedbackService feedbackService;

	private WebTestClient citizenClient() {
		return webTestClient.mutateWith(mockJwt()
				.jwt(jwt -> jwt.claim("role", List.of("CITIZEN")))
				.authorities(new SimpleGrantedAuthority("ROLE_CITIZEN")));
	}

	private WebTestClient adminClient() {
		return webTestClient.mutateWith(mockJwt()
				.jwt(jwt -> jwt.claim("role", List.of("ADMIN")))
				.authorities(new SimpleGrantedAuthority("ROLE_ADMIN")));
	}

	@Test
	void submitFeedback_returnsCreatedFeedback() {
		com.feedback.requests.FeedbackRequest request = new com.feedback.requests.FeedbackRequest();
		request.setGrievanceId("g1");
		request.setCitizenId("c1");
		request.setComments("Thanks for resolving the issue quickly");

		Feedback saved = new Feedback();
		saved.setId("f1");
		saved.setGrievanceId("g1");
		saved.setCitizenId("c1");
		saved.setComments(request.getComments());
		saved.setSubmittedAt(LocalDateTime.now());

		when(feedbackService.submitFeedback(any(Feedback.class))).thenReturn(Mono.just(saved));

		citizenClient()
				.post()
				.uri("/api/feedback/add-feedback")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.id").isEqualTo("f1")
				.jsonPath("$.grievanceId").isEqualTo("g1")
				.jsonPath("$.citizenId").isEqualTo("c1")
				.jsonPath("$.comments").isEqualTo(request.getComments());

		verify(feedbackService).submitFeedback(any(Feedback.class));
	}

	@Test
	void getFeedbackByGrievance_returnsFluxFromService() {
		Feedback first = new Feedback();
		first.setId("f1");
		first.setGrievanceId("g1");
		first.setCitizenId("c1");
		first.setComments("Appreciate the support");
		first.setSubmittedAt(LocalDateTime.now());

		Feedback second = new Feedback();
		second.setId("f2");
		second.setGrievanceId("g1");
		second.setCitizenId("c2");
		second.setComments("Issue handled well");
		second.setSubmittedAt(LocalDateTime.now());

		when(feedbackService.getFeedbackByGrievance("g1")).thenReturn(Flux.just(first, second));

		citizenClient()
				.get()
				.uri("/api/feedback/grievance/{grievanceId}", "g1")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.length()").isEqualTo(2)
				.jsonPath("$[0].id").isEqualTo("f1")
				.jsonPath("$[1].id").isEqualTo("f2");

		verify(feedbackService).getFeedbackByGrievance("g1");
	}

	@Test
	void submitRating_returnsCreatedRating() {
		com.feedback.requests.RatingsRequest request = new com.feedback.requests.RatingsRequest();
		request.setGrievanceId("g2");
		request.setScore(5);

		Ratings saved = new Ratings();
		saved.setId("r1");
		saved.setGrievanceId("g2");
		saved.setScore(5);
		saved.setSubmittedAt(LocalDateTime.now());

		when(feedbackService.submitRating(any(Ratings.class))).thenReturn(Mono.just(saved));

		citizenClient()
				.post()
				.uri("/api/feedback/ratings")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.id").isEqualTo("r1")
				.jsonPath("$.grievanceId").isEqualTo("g2")
				.jsonPath("$.score").isEqualTo(5);

		verify(feedbackService).submitRating(any(Ratings.class));
	}

	@Test
	void requestReopen_returnsGeneratedId() {
		com.feedback.requests.ReopenRequest request = new com.feedback.requests.ReopenRequest();
		request.setGrievanceId("g3");
		request.setReason("Need further investigation for this issue");

		ReopenRequest saved = new ReopenRequest();
		saved.setId("rr1");
		saved.setGrievanceId("g3");
		saved.setReason(request.getReason());
		saved.setRequestedAt(LocalDateTime.now());

		when(feedbackService.requestReopen(any(ReopenRequest.class))).thenReturn(Mono.just(saved));

		citizenClient()
				.post()
				.uri("/api/feedback/reopen-requests")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.reopenRequestId").isEqualTo("rr1");

		verify(feedbackService).requestReopen(any(ReopenRequest.class));
	}

	@Test
	void getReopenRequest_returnsRequest() {
		ReopenRequest saved = new ReopenRequest();
		saved.setId("rr1");
		saved.setGrievanceId("g3");
		saved.setReason("Need further investigation for this issue");
		saved.setRequestedAt(LocalDateTime.now());

		when(feedbackService.getReopenRequest("g3")).thenReturn(Mono.just(saved));

		citizenClient()
				.get()
				.uri("/api/feedback/reopen-requests/{grievanceId}", "g3")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isEqualTo("rr1")
				.jsonPath("$.grievanceId").isEqualTo("g3")
				.jsonPath("$.reason").isEqualTo(saved.getReason());

		verify(feedbackService).getReopenRequest("g3");
	}

	@Test
	void getStats_returnsAggregatedMetrics() {
		FeedbackStats stats = new FeedbackStats(4, 3, 4.5, 1);

		when(feedbackService.getStats()).thenReturn(Mono.just(stats));

		adminClient()
				.get()
				.uri("/api/feedback/stats")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.totalFeedbacks").isEqualTo(4)
				.jsonPath("$.totalRatings").isEqualTo(3)
				.jsonPath("$.averageRating").isEqualTo(4.5)
				.jsonPath("$.totalReopenRequests").isEqualTo(1);

		verify(feedbackService).getStats();
	}
}
