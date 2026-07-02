package com.cityscout.api.service;

import com.cityscout.api.domain.PoiCategory;
import com.cityscout.api.domain.PointOfInterest;
import com.cityscout.api.domain.Review;
import com.cityscout.api.exception.PoiNotFoundException;
import com.cityscout.api.repository.PointOfInterestRepository;
import com.cityscout.api.repository.RatingStats;
import com.cityscout.api.repository.ReviewRepository;
import com.cityscout.api.web.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PoiService {

    private final PointOfInterestRepository poiRepository;
    private final ReviewRepository reviewRepository;
    private final int defaultLimit;
    private final int maxLimit;

    public PoiService(PointOfInterestRepository poiRepository,
                       ReviewRepository reviewRepository,
                       @Value("${city-scout.nearby.default-limit}") int defaultLimit,
                       @Value("${city-scout.nearby.max-limit}") int maxLimit) {
        this.poiRepository = poiRepository;
        this.reviewRepository = reviewRepository;
        this.defaultLimit = defaultLimit;
        this.maxLimit = maxLimit;
    }

    @Transactional
    public PoiDetails create(CreatePoiRequest request) {
        PointOfInterest poi = new PointOfInterest(
                request.name(), request.category(), request.latitude(), request.longitude(), request.address());
        PointOfInterest saved = poiRepository.save(poi);
        return PoiDetails.of(saved, 0.0, 0);
    }

    @Transactional(readOnly = true)
    public PoiDetails getById(Long id) {
        PointOfInterest poi = poiRepository.findById(id).orElseThrow(() -> new PoiNotFoundException(id));
        RatingStats stats = reviewRepository.findStatsForPoi(id).orElse(null);
        double avg = stats == null ? 0.0 : round(stats.getAverageRating());
        long count = stats == null ? 0 : stats.getReviewCount();
        return PoiDetails.of(poi, avg, count);
    }

    @Transactional(readOnly = true)
    public List<PoiSummary> findNearby(double lat, double lon, double radiusKm, PoiCategory category,
                                        Double minRating, Integer limit, NearbySort sortBy) {
        if (radiusKm <= 0) {
            throw new IllegalArgumentException("radiusKm должен быть положительным числом");
        }
        int effectiveLimit = Math.min(limit == null ? defaultLimit : limit, maxLimit);
        if (effectiveLimit <= 0) {
            throw new IllegalArgumentException("limit должен быть положительным числом");
        }
        NearbySort effectiveSort = sortBy == null ? NearbySort.DISTANCE : sortBy;

        List<PointOfInterest> candidates = category == null
                ? poiRepository.findAll()
                : poiRepository.findByCategory(category);

        Map<Long, RatingStats> statsByPoi = reviewRepository.findStatsForAllPois().stream()
                .collect(Collectors.toMap(RatingStats::getPoiId, Function.identity()));

        Comparator<PoiSummary> comparator = switch (effectiveSort) {
            case RATING -> Comparator.comparingDouble(PoiSummary::averageRating).reversed();
            case NAME -> Comparator.comparing(PoiSummary::name, String.CASE_INSENSITIVE_ORDER);
            case DISTANCE -> Comparator.comparingDouble(PoiSummary::distanceKm);
        };

        return candidates.stream()
                .map(poi -> {
                    double distance = GeoUtils.distanceKm(lat, lon, poi.getLatitude(), poi.getLongitude());
                    RatingStats stats = statsByPoi.get(poi.getId());
                    double avg = stats == null ? 0.0 : round(stats.getAverageRating());
                    long count = stats == null ? 0 : stats.getReviewCount();
                    return PoiSummary.of(poi, round(distance), avg, count);
                })
                .filter(summary -> summary.distanceKm() <= radiusKm)
                .filter(summary -> minRating == null || summary.averageRating() >= minRating)
                .sorted(comparator)
                .limit(effectiveLimit)
                .toList();
    }

    @Transactional
    public ReviewResponse addRating(Long poiId, CreateRatingRequest request) {
        return saveReview(poiId, request.reviewerName(), request.rating(), null);
    }

    @Transactional
    public ReviewResponse addReview(Long poiId, CreateReviewRequest request) {
        return saveReview(poiId, request.reviewerName(), request.rating(), request.comment());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listReviews(Long poiId) {
        if (!poiRepository.existsById(poiId)) {
            throw new PoiNotFoundException(poiId);
        }
        return reviewRepository.findByPointOfInterestIdOrderByCreatedAtDesc(poiId).stream()
                .map(ReviewResponse::of)
                .toList();
    }

    private ReviewResponse saveReview(Long poiId, String reviewerName, int rating, String comment) {
        PointOfInterest poi = poiRepository.findById(poiId).orElseThrow(() -> new PoiNotFoundException(poiId));
        Review saved = reviewRepository.save(new Review(poi, reviewerName, rating, comment));
        return ReviewResponse.of(saved);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
