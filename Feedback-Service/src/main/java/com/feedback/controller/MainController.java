package com.feedback.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.feedback.model.Feedback;
import com.feedback.model.FeedbackStats;
import com.feedback.model.Ratings;
import com.feedback.model.ReopenRequest;
import com.feedback.requests.FeedbackRequest;
import com.feedback.requests.RatingsRequest;
import com.feedback.service.FeedbackService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/feedback")
public class MainController {

	private final FeedbackService feedbackService;

	public MainController(FeedbackService feedbackService) {
		this.feedbackService = feedbackService;
	}

	
	// to add a feedback
	@PostMapping("/add-feedback")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Feedback> submitFeedback(@Valid @RequestBody FeedbackRequest feedback) {
		Feedback entity = new Feedback();
		entity.setGrievanceId(feedback.getGrievanceId());
		entity.setCitizenId(feedback.getCitizenId());
		entity.setComments(feedback.getComments());
		return feedbackService.submitFeedback(entity);
	}

	
	// to get feedback by grievance id
	@GetMapping("/grievance/{grievanceId}")
	@ResponseStatus(HttpStatus.OK)
	public Flux<Feedback> getFeedbackByGrievance(@PathVariable String grievanceId) {
		return feedbackService.getFeedbackByGrievance(grievanceId);
	}

	// to add ratings
	@PostMapping("/ratings")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Ratings> submitRating(@Valid @RequestBody RatingsRequest rating) {
		Ratings entity = new Ratings();
		entity.setGrievanceId(rating.getGrievanceId());
		entity.setScore(rating.getScore());
		return feedbackService.submitRating(entity);
	}

	// to reopen a request
	@PostMapping("/reopen-requests")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Map<String, String>> requestReopen(@Valid @RequestBody com.feedback.requests.ReopenRequest reopenRequest) {
		ReopenRequest entity = new ReopenRequest();
		entity.setGrievanceId(reopenRequest.getGrievanceId());
		entity.setReason(reopenRequest.getReason());
		return feedbackService.requestReopen(entity)
				.map(saved -> Map.of("reopenRequestId", saved.getId()));
	}

	// to get reopen request by grievance id
	@GetMapping("/reopen-requests/{grievanceId}")
	@ResponseStatus(HttpStatus.OK)
	public Mono<ReopenRequest> getReopenRequest(@PathVariable String grievanceId) {
		return feedbackService.getReopenRequest(grievanceId);
	}

	// to get analysis of all feedbacks
	@GetMapping("/stats")
	@ResponseStatus(HttpStatus.OK)
	public Mono<FeedbackStats> getStats() {
		return feedbackService.getStats();
	}
}
