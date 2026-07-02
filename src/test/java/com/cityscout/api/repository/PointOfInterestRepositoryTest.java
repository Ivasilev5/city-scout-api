package com.cityscout.api.repository;

import com.cityscout.api.domain.PoiCategory;
import com.cityscout.api.domain.PointOfInterest;
import com.cityscout.api.domain.Review;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PointOfInterestRepositoryTest {

    @Autowired
    private PointOfInterestRepository poiRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void findByCategoryReturnsOnlyMatchingPois() {
        poiRepository.save(new PointOfInterest("Кремль", PoiCategory.LANDMARK, 55.7989, 49.1064, "адрес"));
        poiRepository.save(new PointOfInterest("Парк", PoiCategory.PARK, 55.79, 49.11, "адрес"));

        List<PointOfInterest> landmarks = poiRepository.findByCategory(PoiCategory.LANDMARK);

        assertThat(landmarks).hasSize(1);
        assertThat(landmarks.get(0).getName()).isEqualTo("Кремль");
    }

    @Test
    void statsAreAggregatedPerPoi() {
        PointOfInterest poi = poiRepository.save(
                new PointOfInterest("Кремль", PoiCategory.LANDMARK, 55.7989, 49.1064, "адрес"));
        reviewRepository.save(new Review(poi, "Иван", 4, "хорошо"));
        reviewRepository.save(new Review(poi, "Мария", 5, "отлично"));

        RatingStats stats = reviewRepository.findStatsForPoi(poi.getId()).orElseThrow();

        assertThat(stats.getReviewCount()).isEqualTo(2);
        assertThat(stats.getAverageRating()).isEqualTo(4.5);
    }

    @Test
    void statsForAllPoisGroupsByPoiId() {
        PointOfInterest first = poiRepository.save(
                new PointOfInterest("Кремль", PoiCategory.LANDMARK, 55.7989, 49.1064, "адрес"));
        PointOfInterest second = poiRepository.save(
                new PointOfInterest("Парк", PoiCategory.PARK, 55.79, 49.11, "адрес"));
        reviewRepository.save(new Review(first, "Иван", 4, null));
        reviewRepository.save(new Review(second, "Мария", 2, null));

        Map<Long, RatingStats> byPoiId = reviewRepository.findStatsForAllPois().stream()
                .collect(Collectors.toMap(RatingStats::getPoiId, Function.identity()));

        assertThat(byPoiId).containsOnlyKeys(first.getId(), second.getId());
        assertThat(byPoiId.get(first.getId()).getAverageRating()).isEqualTo(4.0);
    }
}
