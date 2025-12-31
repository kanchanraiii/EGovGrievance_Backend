package com.feedback.model;

/**
 * Aggregated feedback metrics exposed via the stats endpoint.
 */
public class FeedbackStats {

    private long totalFeedbacks;
    private long totalRatings;
    private double averageRating;
    private long totalReopenRequests;

    public FeedbackStats() {
    }

    public FeedbackStats(long totalFeedbacks, long totalRatings, double averageRating, long totalReopenRequests) {
        this.totalFeedbacks = totalFeedbacks;
        this.totalRatings = totalRatings;
        this.averageRating = averageRating;
        this.totalReopenRequests = totalReopenRequests;
    }

    public long getTotalFeedbacks() {
        return totalFeedbacks;
    }

    public void setTotalFeedbacks(long totalFeedbacks) {
        this.totalFeedbacks = totalFeedbacks;
    }

    public long getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(long totalRatings) {
        this.totalRatings = totalRatings;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public long getTotalReopenRequests() {
        return totalReopenRequests;
    }

    public void setTotalReopenRequests(long totalReopenRequests) {
        this.totalReopenRequests = totalReopenRequests;
    }
}
