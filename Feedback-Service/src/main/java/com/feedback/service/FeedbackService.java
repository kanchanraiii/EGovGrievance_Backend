package com.feedback.service;

import com.feedback.client.GrievanceClient;
import com.feedback.exception.ServiceException;
import com.feedback.model.Feedback;
import com.feedback.model.Ratings;
import com.feedback.model.ReopenRequest;
import com.feedback.repository.FeedbackRepository;
import com.feedback.repository.RatingsRepository;
import com.feedback.repository.ReopenRequestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

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

    
    // to submit a feedback 
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

   

    // to submit a rating
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

   
    // request to reopen
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
                                    return Mono.error(
                                            new ServiceException("Reopen request already exists")
                                    );
                                }

                                reopenRequest.setRequestedAt(LocalDateTime.now());
                                return reopenRequestRepository.save(reopenRequest);
                            });
                });
    }
}
