package com.feedback.service;

import com.feedback.client.GrievanceClient;
import com.egov.common.exception.ServiceException;
import com.egov.common.exception.ResourceNotFoundException;
import com.feedback.model.Feedback;
import com.feedback.model.FeedbackStats;
import com.feedback.model.Ratings;
import com.feedback.model.ReopenRequest;
import com.feedback.repository.FeedbackRepository;
import com.feedback.repository.RatingsRepository;
import com.feedback.repository.ReopenRequestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    @Autowired
	private FeedbackRepository feedbackRepository;
    
    @Autowired
    private RatingsRepository ratingRepository;
    
    @Autowired
    private ReopenRequestRepository reopenRequestRepository;
    
    @Autowired
    private GrievanceClient grievanceClient;

    // to submit a feedback only when a grievance is resolved 
    public Mono<Feedback> submitFeedback(Feedback feedback) {
        return grievanceClient.getGrievanceById(feedback.getGrievanceId())
                .flatMap(grievance -> {
                    if (!grievance.isResolvedOrClosed()) {
                        return Mono.error(
                                new ServiceException("Feedback allowed only after grievance is resolved")
                        );
                    }

                    return feedbackRepository
                            .existsByGrievanceId(feedback.getGrievanceId())
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.error(
                                            new ServiceException("Feedback already submitted for this grievance")
                                    );
                                }

                                feedback.setSubmittedAt(LocalDateTime.now());
                                return feedbackRepository.save(feedback);
                            });
                });
    }

    // to submit ratings
    public Mono<Ratings> submitRating(Ratings rating) {
        if (rating.getScore() < 1 || rating.getScore() > 5) {
            return Mono.error(new ServiceException("Rating must be between 1 and 5"));
        }

        return grievanceClient.getGrievanceById(rating.getGrievanceId())
                .flatMap(grievance -> {
                    if (!grievance.isResolvedOrClosed()) {
                        return Mono.error(
                                new ServiceException("Rating allowed only after grievance is resolved")
                        );
                    }

                    return ratingRepository
                            .existsByGrievanceId(rating.getGrievanceId())
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.error(
                                            new ServiceException("Rating already submitted for this grievance")
                                    );
                                }

                                rating.setSubmittedAt(LocalDateTime.now());
                                return ratingRepository.save(rating);
                            });
                });
    }

    // to reopen requests
    public Mono<ReopenRequest> requestReopen(ReopenRequest reopenRequest) {
        return grievanceClient.getGrievanceById(reopenRequest.getGrievanceId())
                .flatMap(grievance -> {
                    if (!grievance.isResolvedOrClosed()) {
                        return Mono.error(
                                new ServiceException("Only resolved grievances can be reopened")
                        );
                    }

                    return reopenRequestRepository
                            .existsByGrievanceId(reopenRequest.getGrievanceId())
                            .flatMap(exists -> {
                                if (exists) {
                                    return reopenRequestRepository.findByGrievanceId(reopenRequest.getGrievanceId())
                                            .flatMap(saved ->
                                                    grievanceClient
                                                            .markAsReopened(saved.getGrievanceId(), saved.getReason())
                                                            .thenReturn(saved)
                                            )
                                            .switchIfEmpty(Mono.error(
                                                    new ServiceException("Reopen request already exists")
                                            ));
                                }

                                reopenRequest.setRequestedAt(LocalDateTime.now());
                                return reopenRequestRepository.save(reopenRequest)
                                        .flatMap(saved ->
                                                grievanceClient
                                                        .markAsReopened(saved.getGrievanceId(), saved.getReason())
                                                        .thenReturn(saved)
                                        );
                            });
                });
    }

   // to get reopened request
    public Mono<ReopenRequest> getReopenRequest(String grievanceId) {
        return reopenRequestRepository.findByGrievanceId(grievanceId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Reopen request not found")));
    }

    public Flux<Feedback> getFeedbackByGrievance(String grievanceId) {
        return feedbackRepository.findByGrievanceId(grievanceId);
    }

    // to get the analysis
    public Mono<FeedbackStats> getStats() {

        Mono<Long> feedbackCount = feedbackRepository.count();
        Mono<Long> ratingCount = ratingRepository.count();
        Mono<Double> averageRating = ratingRepository
                .findAll()
                .map(Ratings::getScore)
                .collect(Collectors.averagingInt(Integer::intValue));
        Mono<Long> reopenCount = reopenRequestRepository.count();

        return Mono.zip(feedbackCount, ratingCount, averageRating, reopenCount)
                .map(tuple -> new FeedbackStats(
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT4()
                ));
    }
}
