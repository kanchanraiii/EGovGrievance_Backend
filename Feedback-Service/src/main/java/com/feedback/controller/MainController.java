package com.feedback.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.feedback.model.Feedback;
import com.feedback.model.Ratings;
import com.feedback.model.ReopenRequest;
import com.feedback.service.FeedbackService;

import jakarta.validation.Valid;
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

	// to add a rating
	@PostMapping("/ratings")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Ratings> submitRating(@Valid @RequestBody Ratings rating) {
		return feedbackService.submitRating(rating);
	}

	// to reopen request
	@PostMapping("/reopen-requests")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<ReopenRequest> requestReopen(@Valid @RequestBody ReopenRequest reopenRequest) {
		return feedbackService.requestReopen(reopenRequest);
	}
}
