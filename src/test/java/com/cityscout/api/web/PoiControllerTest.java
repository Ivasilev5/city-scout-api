package com.cityscout.api.web;

import com.cityscout.api.domain.PoiCategory;
import com.cityscout.api.exception.PoiNotFoundException;
import com.cityscout.api.service.PoiService;
import com.cityscout.api.web.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PoiController.class)
class PoiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PoiService poiService;

    @Test
    void createReturnsCreatedPoi() throws Exception {
        CreatePoiRequest request = new CreatePoiRequest("Кремль", PoiCategory.LANDMARK, 55.7989, 49.1064, "адрес");
        PoiDetails response = new PoiDetails(1L, "Кремль", PoiCategory.LANDMARK, 55.7989, 49.1064, "адрес", 0.0, 0);
        when(poiService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/poi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Кремль"));
    }

    @Test
    void createRejectsInvalidCoordinates() throws Exception {
        CreatePoiRequest request = new CreatePoiRequest("Кремль", PoiCategory.LANDMARK, 200.0, 49.1064, "адрес");

        mockMvc.perform(post("/api/poi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdReturnsDetails() throws Exception {
        PoiDetails response = new PoiDetails(1L, "Кремль", PoiCategory.LANDMARK, 55.7989, 49.1064, "адрес", 4.5, 3);
        when(poiService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/poi/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.reviewCount").value(3));
    }

    @Test
    void getByIdReturnsNotFoundForMissingPoi() throws Exception {
        when(poiService.getById(404L)).thenThrow(new PoiNotFoundException(404L));

        mockMvc.perform(get("/api/poi/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void nearbyReturnsFilteredList() throws Exception {
        PoiSummary summary = new PoiSummary(1L, "Кремль", PoiCategory.LANDMARK, 55.7989, 49.1064, "адрес", 0.5, 4.5, 3);
        when(poiService.findNearby(eq(55.7989), eq(49.1064), eq(2.0), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(summary));

        mockMvc.perform(get("/api/poi/nearby")
                        .param("lat", "55.7989")
                        .param("lon", "49.1064")
                        .param("radiusKm", "2.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].distanceKm").value(0.5));
    }

    @Test
    void addRatingReturnsCreated() throws Exception {
        ReviewResponse response = new ReviewResponse(10L, "Иван", 5, null, LocalDateTime.now());
        when(poiService.addRating(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/poi/1/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewerName\":\"Иван\",\"rating\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void addRatingRejectsOutOfRangeValue() throws Exception {
        mockMvc.perform(post("/api/poi/1/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewerName\":\"Иван\",\"rating\":7}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReviewsReturnsAllReviewsForPoi() throws Exception {
        ReviewResponse response = new ReviewResponse(1L, "Мария", 4, "Хорошо", LocalDateTime.now());
        when(poiService.listReviews(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/poi/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewerName").value("Мария"));
    }
}
