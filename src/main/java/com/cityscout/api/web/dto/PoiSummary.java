package com.cityscout.api.web.dto;

import com.cityscout.api.domain.PoiCategory;
import com.cityscout.api.domain.PointOfInterest;

public record PoiSummary(
        Long id,
        String name,
        PoiCategory category,
        double latitude,
        double longitude,
        String address,
        double distanceKm,
        double averageRating,
        long reviewCount
) {
    public static PoiSummary of(PointOfInterest poi, double distanceKm, double averageRating, long reviewCount) {
        return new PoiSummary(
                poi.getId(),
                poi.getName(),
                poi.getCategory(),
                poi.getLatitude(),
                poi.getLongitude(),
                poi.getAddress(),
                distanceKm,
                averageRating,
                reviewCount
        );
    }
}
