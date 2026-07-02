package com.cityscout.api.util;

import com.cityscout.api.service.GeoUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeoUtilsTest {

    @Test
    void distanceBetweenSamePointIsZero() {
        double distance = GeoUtils.distanceKm(55.7989, 49.1064, 55.7989, 49.1064);
        assertThat(distance).isCloseTo(0.0, org.assertj.core.data.Offset.offset(0.0001));
    }

    @Test
    void distanceBetweenKazanKremlinAndBaumanStreetIsAboutOneAndHalfKm() {
        // Казанский Кремль -> ул. Баумана, известное расстояние ~1.4-1.6 км по прямой
        double distance = GeoUtils.distanceKm(55.7989, 49.1064, 55.7887, 49.1213);
        assertThat(distance).isBetween(1.0, 2.0);
    }

    @Test
    void distanceIsSymmetric() {
        double forward = GeoUtils.distanceKm(55.7989, 49.1064, 55.7887, 49.1213);
        double backward = GeoUtils.distanceKm(55.7887, 49.1213, 55.7989, 49.1064);
        assertThat(forward).isCloseTo(backward, org.assertj.core.data.Offset.offset(0.0001));
    }
}
