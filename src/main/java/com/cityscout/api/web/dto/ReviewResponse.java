package com.cityscout.api.web.dto;

import com.cityscout.api.domain.Review;
import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        String reviewerName,
        int rating,
        String comment,
        LocalDateTime createdAt
) {
    public static ReviewResponse of(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getReviewerName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
