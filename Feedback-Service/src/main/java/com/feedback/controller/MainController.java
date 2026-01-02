package com.feedback.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.feedback.service.FeedbackService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/feedback")
public class MainController {

	@Autowired
	private FeedbackService feedbackService;

	
	// to add a feedback
	@PostMapping("/add-feedback")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Feedback> submitFeedback(@Valid @RequestBody Feedback feedback) {
		return feedbackService.submitFeedback(feedback);
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
	public Mono<Ratings> submitRating(@Valid @RequestBody Ratings rating) {
		return feedbackService.submitRating(rating);
	}

	// to reopen a request
	@PostMapping("/reopen-requests")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Map<String, String>> requestReopen(@Valid @RequestBody ReopenRequest reopenRequest) {
		return feedbackService.requestReopen(reopenRequest)
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
