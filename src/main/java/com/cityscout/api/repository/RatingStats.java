package com.cityscout.api.repository;

public interface RatingStats {
    Long getPoiId();
    Double getAverageRating();
    Long getReviewCount();
}
