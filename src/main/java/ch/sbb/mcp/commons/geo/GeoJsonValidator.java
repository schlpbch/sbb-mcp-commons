package ch.sbb.mcp.commons.geo;

import ch.sbb.mcp.commons.validation.Validators;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for validating and parsing GeoJSON geometries.
 * 
 * <p>Supports GeoJSON Point and Polygon formats according to RFC 7946.</p>
 * 
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7946">RFC 7946: GeoJSON</a>
 */
public class GeoJsonValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(GeoJsonValidator.class);
    private static final GeometryFactory geometryFactory = new GeometryFactory();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Parse a GeoJSON Polygon coordinate array.
     * 
     * <p>Expected format: [[lon1, lat1], [lon2, lat2], ..., [lon1, lat1]]</p>
     * <p>First and last coordinates must be identical (closed ring).</p>
     * 
     * @param polygonCoordinates GeoJSON Polygon coordinates as string
     * @return JTS Polygon geometry
     * @throws IllegalArgumentException if coordinates are invalid
     */
    public static Polygon parsePolygon(String polygonCoordinates) {
        Validators.requireNonEmpty(polygonCoordinates, "polygonCoordinates");
        
        try {
            JsonNode rootNode = objectMapper.readTree(polygonCoordinates);
            
            if (!rootNode.isArray()) {
                throw new IllegalArgumentException("Polygon coordinates must be an array");
            }
            
            // GeoJSON Polygon: array of linear rings (first is exterior, rest are holes)
            if (rootNode.size() == 0) {
                throw new IllegalArgumentException("Polygon must have at least one ring");
            }
            
            JsonNode exteriorRing = rootNode.get(0);
            if (!exteriorRing.isArray()) {
                throw new IllegalArgumentException("Polygon ring must be an array of coordinates");
            }
            
            List<Coordinate> coordinates = parseCoordinateArray(exteriorRing);
            
            // Validate closed ring
            if (coordinates.size() < 4) {
                throw new IllegalArgumentException(
                    "Polygon ring must have at least 4 coordinates (3 unique points + closing point). Got: " + coordinates.size()
                );
            }
            
            Coordinate first = coordinates.get(0);
            Coordinate last = coordinates.get(coordinates.size() - 1);
            if (!first.equals2D(last)) {
                throw new IllegalArgumentException(
                    "Polygon ring must be closed (first and last coordinates must match). " +
                    "First: [" + first.x + ", " + first.y + "], Last: [" + last.x + ", " + last.y + "]"
                );
            }
            
            // Create polygon (ignore holes for now - can be added later if needed)
            Coordinate[] coordArray = coordinates.toArray(new Coordinate[0]);
            LinearRing shell = geometryFactory.createLinearRing(coordArray);
            
            return geometryFactory.createPolygon(shell);
            
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON format for polygon coordinates: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse a GeoJSON Point coordinate.
     * 
     * <p>Expected format: [lon, lat]</p>
     * 
     * @param pointCoordinates GeoJSON Point coordinates as string
     * @return JTS Point geometry
     * @throws IllegalArgumentException if coordinates are invalid
     */
    public static Point parsePoint(String pointCoordinates) {
        Validators.requireNonEmpty(pointCoordinates, "pointCoordinates");
        
        try {
            JsonNode rootNode = objectMapper.readTree(pointCoordinates);
            
            if (!rootNode.isArray() || rootNode.size() != 2) {
                throw new IllegalArgumentException("Point coordinates must be an array of [longitude, latitude]");
            }
            
            double lon = rootNode.get(0).asDouble();
            double lat = rootNode.get(1).asDouble();
            
            validateLongitude(lon);
            validateLatitude(lat);
            
            return geometryFactory.createPoint(new Coordinate(lon, lat));
            
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON format for point coordinates: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculate bounding box from a polygon.
     * 
     * @param polygon JTS Polygon
     * @return Bounding box as [minLon, minLat, maxLon, maxLat]
     */
    public static double[] calculateBoundingBox(Polygon polygon) {
        Envelope envelope = polygon.getEnvelopeInternal();
        return new double[] {
            envelope.getMinX(), // minLon
            envelope.getMinY(), // minLat
            envelope.getMaxX(), // maxLon
            envelope.getMaxY()  // maxLat
        };
    }
    
    /**
     * Calculate center point of a bounding box.
     * 
     * @param boundingBox [minLon, minLat, maxLon, maxLat]
     * @return Center point as [longitude, latitude]
     */
    public static double[] calculateCenter(double[] boundingBox) {
        double centerLon = (boundingBox[0] + boundingBox[2]) / 2.0;
        double centerLat = (boundingBox[1] + boundingBox[3]) / 2.0;
        return new double[] { centerLon, centerLat };
    }
    
    /**
     * Calculate radius in meters to cover the bounding box.
     * Uses Haversine formula for accurate distance calculation.
     * 
     * @param boundingBox [minLon, minLat, maxLon, maxLat]
     * @return Radius in meters
     */
    public static int calculateRadius(double[] boundingBox) {
        double[] center = calculateCenter(boundingBox);
        
        // Calculate distance from center to corner (diagonal)
        double cornerLon = boundingBox[2]; // maxLon
        double cornerLat = boundingBox[3]; // maxLat
        
        double distanceMeters = haversineDistance(
            center[1], center[0],  // center lat, lon
            cornerLat, cornerLon   // corner lat, lon
        );
        
        // Add 10% buffer to ensure coverage
        return (int) Math.ceil(distanceMeters * 1.1);
    }
    
    /**
     * Check if a point is inside a polygon.
     * 
     * @param point JTS Point
     * @param polygon JTS Polygon
     * @return true if point is inside polygon
     */
    public static boolean isPointInPolygon(Point point, Polygon polygon) {
        return polygon.contains(point);
    }
    
    // Private helper methods
    
    private static List<Coordinate> parseCoordinateArray(JsonNode coordArray) {
        List<Coordinate> coordinates = new ArrayList<>();
        
        for (JsonNode coordNode : coordArray) {
            if (!coordNode.isArray() || coordNode.size() < 2) {
                throw new IllegalArgumentException("Each coordinate must be an array of [longitude, latitude]");
            }
            
            double lon = coordNode.get(0).asDouble();
            double lat = coordNode.get(1).asDouble();
            
            validateLongitude(lon);
            validateLatitude(lat);
            
            coordinates.add(new Coordinate(lon, lat));
        }
        
        return coordinates;
    }
    
    private static void validateLongitude(double lon) {
        Validators.requireValidLongitude(lon);
    }
    
    private static void validateLatitude(double lat) {
        Validators.requireValidLatitude(lat);
    }
    
    /**
     * Calculate distance between two points using Haversine formula.
     * 
     * @param lat1 Latitude of point 1 (degrees)
     * @param lon1 Longitude of point 1 (degrees)
     * @param lat2 Latitude of point 2 (degrees)
     * @param lon2 Longitude of point 2 (degrees)
     * @return Distance in meters
     */
    private static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Earth radius in meters
        
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
