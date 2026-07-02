package com.cityscout.api.web.dto;

import com.cityscout.api.domain.PoiCategory;
import com.cityscout.api.domain.PointOfInterest;

public record PoiDetails(
        Long id,
        String name,
        PoiCategory category,
        double latitude,
        double longitude,
        String address,
        double averageRating,
        long reviewCount
) {
    public static PoiDetails of(PointOfInterest poi, double averageRating, long reviewCount) {
        return new PoiDetails(
                poi.getId(),
                poi.getName(),
                poi.getCategory(),
                poi.getLatitude(),
                poi.getLongitude(),
                poi.getAddress(),
                averageRating,
                reviewCount
        );
    }
}
