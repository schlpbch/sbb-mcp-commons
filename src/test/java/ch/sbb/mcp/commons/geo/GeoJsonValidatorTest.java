package ch.sbb.mcp.commons.geo;

import ch.sbb.mcp.commons.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GeoJsonValidator Tests")
class GeoJsonValidatorTest {

    @Test
    @DisplayName("Should parse valid polygon")
    void shouldParseValidPolygon() {
        String validPolygon = "[[[8.5, 47.3], [8.6, 47.3], [8.6, 47.4], [8.5, 47.4], [8.5, 47.3]]]";
        
        Polygon polygon = GeoJsonValidator.parsePolygon(validPolygon);
        
        assertThat(polygon).isNotNull();
        assertThat(polygon.getCoordinates()).hasSize(5);
    }

    @Test
    @DisplayName("Should reject null polygon")
    void shouldRejectNullPolygon() {
        assertThatThrownBy(() -> GeoJsonValidator.parsePolygon(null))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Should reject empty polygon")
    void shouldRejectEmptyPolygon() {
        assertThatThrownBy(() -> GeoJsonValidator.parsePolygon(""))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Should reject polygon with too few coordinates")
    void shouldRejectPolygonWithTooFewCoordinates() {
        String invalidPolygon = "[[[8.5, 47.3], [8.6, 47.3], [8.5, 47.3]]]"; // Only 3 coordinates
        
        assertThatThrownBy(() -> GeoJsonValidator.parsePolygon(invalidPolygon))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("at least 4 coordinates");
    }

    @Test
    @DisplayName("Should reject unclosed polygon")
    void shouldRejectUnclosedPolygon() {
        String unclosedPolygon = "[[[8.5, 47.3], [8.6, 47.3], [8.6, 47.4], [8.5, 47.4]]]"; // Not closed
        
        assertThatThrownBy(() -> GeoJsonValidator.parsePolygon(unclosedPolygon))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be closed");
    }

    @Test
    @DisplayName("Should reject invalid longitude")
    void shouldRejectInvalidLongitude() {
        String invalidPolygon = "[[[200.0, 47.3], [8.6, 47.3], [8.6, 47.4], [8.5, 47.4], [200.0, 47.3]]]";
        
        assertThatThrownBy(() -> GeoJsonValidator.parsePolygon(invalidPolygon))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Longitude must be between");
    }

    @Test
    @DisplayName("Should reject invalid latitude")
    void shouldRejectInvalidLatitude() {
        String invalidPolygon = "[[[8.5, 95.0], [8.6, 47.3], [8.6, 47.4], [8.5, 47.4], [8.5, 95.0]]]";
        
        assertThatThrownBy(() -> GeoJsonValidator.parsePolygon(invalidPolygon))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Latitude must be between");
    }

    @Test
    @DisplayName("Should parse valid point")
    void shouldParseValidPoint() {
        String validPoint = "[8.5441, 47.4115]";
        
        Point point = GeoJsonValidator.parsePoint(validPoint);
        
        assertThat(point).isNotNull();
        assertThat(point.getX()).isEqualTo(8.5441);
        assertThat(point.getY()).isEqualTo(47.4115);
    }

    @Test
    @DisplayName("Should reject null point")
    void shouldRejectNullPoint() {
        assertThatThrownBy(() -> GeoJsonValidator.parsePoint(null))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Should reject point with wrong number of coordinates")
    void shouldRejectPointWithWrongNumberOfCoordinates() {
        String invalidPoint = "[8.5441]"; // Only 1 coordinate
        
        assertThatThrownBy(() -> GeoJsonValidator.parsePoint(invalidPoint))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be an array of [longitude, latitude]");
    }

    @Test
    @DisplayName("Should calculate bounding box correctly")
    void shouldCalculateBoundingBoxCorrectly() {
        String polygonCoords = "[[[8.5, 47.3], [8.6, 47.3], [8.6, 47.4], [8.5, 47.4], [8.5, 47.3]]]";
        Polygon polygon = GeoJsonValidator.parsePolygon(polygonCoords);
        
        double[] bbox = GeoJsonValidator.calculateBoundingBox(polygon);
        
        assertThat(bbox).hasSize(4);
        assertThat(bbox[0]).isEqualTo(8.5);  // minLon
        assertThat(bbox[1]).isEqualTo(47.3); // minLat
        assertThat(bbox[2]).isEqualTo(8.6);  // maxLon
        assertThat(bbox[3]).isEqualTo(47.4); // maxLat
    }

    @Test
    @DisplayName("Should calculate center correctly")
    void shouldCalculateCenterCorrectly() {
        double[] bbox = new double[] {8.5, 47.3, 8.6, 47.4};
        
        double[] center = GeoJsonValidator.calculateCenter(bbox);
        
        assertThat(center).hasSize(2);
        assertThat(center[0]).isEqualTo(8.55);  // centerLon
        assertThat(center[1]).isCloseTo(47.35, within(0.0001)); // centerLat
    }

    @Test
    @DisplayName("Should calculate radius correctly")
    void shouldCalculateRadiusCorrectly() {
        double[] bbox = new double[] {8.5, 47.3, 8.6, 47.4};
        
        int radius = GeoJsonValidator.calculateRadius(bbox);
        
        // Radius should be positive and reasonable (diagonal distance + buffer)
        assertThat(radius).isGreaterThan(0);
        assertThat(radius).isLessThan(20000); // Less than 20km for this small box
    }

    @Test
    @DisplayName("Should correctly identify point inside polygon")
    void shouldCorrectlyIdentifyPointInsidePolygon() {
        String polygonCoords = "[[[8.5, 47.3], [8.6, 47.3], [8.6, 47.4], [8.5, 47.4], [8.5, 47.3]]]";
        Polygon polygon = GeoJsonValidator.parsePolygon(polygonCoords);
        
        String insidePoint = "[8.55, 47.35]"; // Center of polygon
        Point point = GeoJsonValidator.parsePoint(insidePoint);
        
        boolean isInside = GeoJsonValidator.isPointInPolygon(point, polygon);
        
        assertThat(isInside).isTrue();
    }

    @Test
    @DisplayName("Should correctly identify point outside polygon")
    void shouldCorrectlyIdentifyPointOutsidePolygon() {
        String polygonCoords = "[[[8.5, 47.3], [8.6, 47.3], [8.6, 47.4], [8.5, 47.4], [8.5, 47.3]]]";
        Polygon polygon = GeoJsonValidator.parsePolygon(polygonCoords);
        
        String outsidePoint = "[8.7, 47.5]"; // Outside polygon
        Point point = GeoJsonValidator.parsePoint(outsidePoint);
        
        boolean isInside = GeoJsonValidator.isPointInPolygon(point, polygon);
        
        assertThat(isInside).isFalse();
    }

    @Test
    @DisplayName("Should handle Zürich city center polygon")
    void shouldHandleZurichCityCenterPolygon() {
        // Real-world example: Zürich city center approximate boundary
        String zurichPolygon = "[[[8.52, 47.36], [8.56, 47.36], [8.56, 47.38], [8.52, 47.38], [8.52, 47.36]]]";
        
        Polygon polygon = GeoJsonValidator.parsePolygon(zurichPolygon);
        
        assertThat(polygon).isNotNull();
        
        // Zürich HB should be inside
        Point zurichHB = GeoJsonValidator.parsePoint("[8.5403, 47.3779]");
        assertThat(GeoJsonValidator.isPointInPolygon(zurichHB, polygon)).isTrue();
        
        // Zürich Airport should be outside
        Point zurichAirport = GeoJsonValidator.parsePoint("[8.5617, 47.4647]");
        assertThat(GeoJsonValidator.isPointInPolygon(zurichAirport, polygon)).isFalse();
    }
}
