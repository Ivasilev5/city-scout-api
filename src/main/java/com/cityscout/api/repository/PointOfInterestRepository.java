package com.cityscout.api.repository;

import com.cityscout.api.domain.PoiCategory;
import com.cityscout.api.domain.PointOfInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PointOfInterestRepository extends JpaRepository<PointOfInterest, Long> {

    List<PointOfInterest> findByCategory(PoiCategory category);
}
