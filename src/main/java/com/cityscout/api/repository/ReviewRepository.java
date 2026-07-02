package com.cityscout.api.repository;

import com.cityscout.api.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByPointOfInterestIdOrderByCreatedAtDesc(Long poiId);

    @Query("""
            SELECT r.pointOfInterest.id AS poiId, AVG(r.rating) AS averageRating, COUNT(r) AS reviewCount
            FROM Review r
            WHERE r.pointOfInterest.id = :poiId
            GROUP BY r.pointOfInterest.id
            """)
    Optional<RatingStats> findStatsForPoi(@Param("poiId") Long poiId);

    @Query("""
            SELECT r.pointOfInterest.id AS poiId, AVG(r.rating) AS averageRating, COUNT(r) AS reviewCount
            FROM Review r
            GROUP BY r.pointOfInterest.id
            """)
    List<RatingStats> findStatsForAllPois();
}
