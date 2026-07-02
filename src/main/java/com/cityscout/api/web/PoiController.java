package com.cityscout.api.web;

import com.cityscout.api.domain.PoiCategory;
import com.cityscout.api.service.PoiService;
import com.cityscout.api.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/poi")
public class PoiController {

    private final PoiService poiService;

    public PoiController(PoiService poiService) {
        this.poiService = poiService;
    }

    @PostMapping
    public ResponseEntity<PoiDetails> create(@Valid @RequestBody CreatePoiRequest request) {
        PoiDetails created = poiService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public PoiDetails getById(@PathVariable Long id) {
        return poiService.getById(id);
    }

    @GetMapping("/nearby")
    public List<PoiSummary> nearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double radiusKm,
            @RequestParam(required = false) PoiCategory category,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) NearbySort sortBy
    ) {
        return poiService.findNearby(lat, lon, radiusKm, category, minRating, limit, sortBy);
    }

    @PostMapping("/{id}/ratings")
    public ResponseEntity<ReviewResponse> addRating(@PathVariable("id") Long poiId,
                                                      @Valid @RequestBody CreateRatingRequest request) {
        ReviewResponse created = poiService.addRating(poiId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ReviewResponse> addReview(@PathVariable("id") Long poiId,
                                                      @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse created = poiService.addReview(poiId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}/reviews")
    public List<ReviewResponse> reviews(@PathVariable("id") Long poiId) {
        return poiService.listReviews(poiId);
    }
}
