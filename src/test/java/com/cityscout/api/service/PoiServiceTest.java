package com.cityscout.api.service;

import com.cityscout.api.domain.PoiCategory;
import com.cityscout.api.domain.PointOfInterest;
import com.cityscout.api.domain.Review;
import com.cityscout.api.exception.PoiNotFoundException;
import com.cityscout.api.repository.PointOfInterestRepository;
import com.cityscout.api.repository.RatingStats;
import com.cityscout.api.repository.ReviewRepository;
import com.cityscout.api.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PoiServiceTest {

    @Mock
    private PointOfInterestRepository poiRepository;

    @Mock
    private ReviewRepository reviewRepository;

    private PoiService poiService;

    @BeforeEach
    void setUp() {
        poiService = new PoiService(poiRepository, reviewRepository, 10, 100);
    }

    private PointOfInterest poi(long id, String name, PoiCategory category, double lat, double lon) {
        PointOfInterest mock = mock(PointOfInterest.class);
        lenient().when(mock.getId()).thenReturn(id);
        lenient().when(mock.getName()).thenReturn(name);
        lenient().when(mock.getCategory()).thenReturn(category);
        lenient().when(mock.getLatitude()).thenReturn(lat);
        lenient().when(mock.getLongitude()).thenReturn(lon);
        lenient().when(mock.getAddress()).thenReturn("test address");
        return mock;
    }

    private RatingStats stats(long poiId, double avg, long count) {
        RatingStats mock = mock(RatingStats.class);
        lenient().when(mock.getPoiId()).thenReturn(poiId);
        lenient().when(mock.getAverageRating()).thenReturn(avg);
        lenient().when(mock.getReviewCount()).thenReturn(count);
        return mock;
    }

    @Test
    void findNearbyFiltersPlacesOutsideRadius() {
        PointOfInterest near = poi(1L, "Кремль", PoiCategory.LANDMARK, 55.7989, 49.1064);
        PointOfInterest far = poi(2L, "Далёкое место", PoiCategory.PARK, 56.5, 50.5);

        when(poiRepository.findAll()).thenReturn(List.of(near, far));
        when(reviewRepository.findStatsForAllPois()).thenReturn(List.of());

        List<PoiSummary> result = poiService.findNearby(55.7989, 49.1064, 5.0, null, null, null, null);

        assertThat(result).extracting(PoiSummary::id).containsExactly(1L);
    }

    @Test
    void findNearbyFiltersByMinRating() {
        PointOfInterest lowRated = poi(1L, "Место A", PoiCategory.MUSEUM, 55.7989, 49.1064);
        PointOfInterest highRated = poi(2L, "Место B", PoiCategory.MUSEUM, 55.7990, 49.1065);

        RatingStats lowStats = stats(1L, 2.0, 3);
        RatingStats highStats = stats(2L, 4.5, 5);
        when(poiRepository.findAll()).thenReturn(List.of(lowRated, highRated));
        when(reviewRepository.findStatsForAllPois()).thenReturn(List.of(lowStats, highStats));

        List<PoiSummary> result = poiService.findNearby(55.7989, 49.1064, 5.0, null, 4.0, null, null);

        assertThat(result).extracting(PoiSummary::id).containsExactly(2L);
    }

    @Test
    void findNearbyRespectsLimit() {
        List<PointOfInterest> pois = List.of(
                poi(1L, "A", PoiCategory.OTHER, 55.7989, 49.1064),
                poi(2L, "B", PoiCategory.OTHER, 55.7990, 49.1065),
                poi(3L, "C", PoiCategory.OTHER, 55.7991, 49.1066)
        );
        when(poiRepository.findAll()).thenReturn(pois);
        when(reviewRepository.findStatsForAllPois()).thenReturn(List.of());

        List<PoiSummary> result = poiService.findNearby(55.7989, 49.1064, 10.0, null, null, 2, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void findNearbySortsByRatingDescending() {
        PointOfInterest a = poi(1L, "A", PoiCategory.OTHER, 55.7989, 49.1064);
        PointOfInterest b = poi(2L, "B", PoiCategory.OTHER, 55.7990, 49.1065);

        RatingStats aStats = stats(1L, 3.0, 2);
        RatingStats bStats = stats(2L, 4.8, 10);
        when(poiRepository.findAll()).thenReturn(List.of(a, b));
        when(reviewRepository.findStatsForAllPois()).thenReturn(List.of(aStats, bStats));

        List<PoiSummary> result = poiService.findNearby(55.7989, 49.1064, 10.0, null, null, null, NearbySort.RATING);

        assertThat(result).extracting(PoiSummary::id).containsExactly(2L, 1L);
    }

    @Test
    void findNearbyRejectsNonPositiveRadius() {
        assertThatThrownBy(() -> poiService.findNearby(55.0, 49.0, 0, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getByIdThrowsWhenPoiMissing() {
        when(poiRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> poiService.getById(42L))
                .isInstanceOf(PoiNotFoundException.class);
    }

    @Test
    void getByIdReturnsDetailsWithAggregatedRating() {
        PointOfInterest place = poi(1L, "Кремль", PoiCategory.LANDMARK, 55.7989, 49.1064);
        RatingStats placeStats = stats(1L, 4.5, 2);
        when(poiRepository.findById(1L)).thenReturn(Optional.of(place));
        when(reviewRepository.findStatsForPoi(1L)).thenReturn(Optional.of(placeStats));

        PoiDetails details = poiService.getById(1L);

        assertThat(details.averageRating()).isEqualTo(4.5);
        assertThat(details.reviewCount()).isEqualTo(2);
    }

    @Test
    void addReviewPersistsReviewLinkedToPoi() {
        PointOfInterest place = poi(1L, "Кремль", PoiCategory.LANDMARK, 55.7989, 49.1064);
        when(poiRepository.findById(1L)).thenReturn(Optional.of(place));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse response = poiService.addReview(1L, new CreateReviewRequest("Иван", 5, "Отлично"));

        assertThat(response.reviewerName()).isEqualTo("Иван");
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.comment()).isEqualTo("Отлично");
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void addReviewThrowsWhenPoiMissing() {
        when(poiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> poiService.addReview(99L, new CreateReviewRequest("Иван", 5, null)))
                .isInstanceOf(PoiNotFoundException.class);
        verify(reviewRepository, never()).save(any());
    }
}
