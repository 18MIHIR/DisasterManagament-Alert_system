package com.disastermgmt.backend.external;

import com.disastermgmt.backend.disaster.Disaster;
import com.disastermgmt.backend.disaster.DisasterSeverity;
import com.disastermgmt.backend.disaster.DisasterType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExternalApiService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // India bounding box: lat 6.7-35.5, lon 68.1-97.4
    private static final double INDIA_MIN_LAT = 6.7;
    private static final double INDIA_MAX_LAT = 35.5;
    private static final double INDIA_MIN_LON = 68.1;
    private static final double INDIA_MAX_LON = 97.4;

    private static final String USGS_INDIA_EARTHQUAKES = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson"
            + "&minlatitude=" + INDIA_MIN_LAT + "&maxlatitude=" + INDIA_MAX_LAT
            + "&minlongitude=" + INDIA_MIN_LON + "&maxlongitude=" + INDIA_MAX_LON
            + "&minmagnitude=4.0&starttime=";

    public ExternalApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<Disaster> fetchEarthquakeData() {
        List<Disaster> disasters = new ArrayList<>();
        String startTime = LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String url = USGS_INDIA_EARTHQUAKES + startTime;

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode features = root.get("features");

            if (features != null && features.isArray()) {
                for (JsonNode feature : features) {
                    try {
                        Disaster disaster = parseEarthquake(feature);
                        if (disaster != null) {
                            disasters.add(disaster);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to parse earthquake feature: {}", e.getMessage());
                    }
                }
            }
            logger.info("Fetched {} earthquakes from USGS (India only)", disasters.size());
        } catch (Exception e) {
            logger.error("Failed to fetch earthquake data from USGS: {}", e.getMessage());
        }

        return disasters;
    }

    private Disaster parseEarthquake(JsonNode feature) {
        JsonNode properties = feature.get("properties");
        JsonNode geometry = feature.get("geometry");

        if (properties == null || geometry == null) return null;

        Disaster disaster = new Disaster();
        
        String id = feature.has("id") ? feature.get("id").asText() : null;
        disaster.setExternalId(id);
        disaster.setSource("USGS");
        disaster.setType(DisasterType.EARTHQUAKE);

        double magnitude = properties.has("mag") && !properties.get("mag").isNull() 
                ? properties.get("mag").asDouble() : 0;
        String place = properties.has("place") && !properties.get("place").isNull() 
                ? properties.get("place").asText() : "Unknown Location";
        long time = properties.has("time") ? properties.get("time").asLong() : System.currentTimeMillis();
        String alert = properties.has("alert") && !properties.get("alert").isNull() 
                ? properties.get("alert").asText() : null;

        disaster.setTitle(String.format("M%.1f Earthquake - %s", magnitude, place));
        disaster.setDescription(String.format("Magnitude %.1f earthquake detected. %s", 
                magnitude, 
                properties.has("tsunami") && properties.get("tsunami").asInt() == 1 
                        ? "Tsunami warning issued." : "No tsunami warning."));
        disaster.setLocation(place);
        disaster.setCountry("India");
        disaster.setSeverity(mapMagnitudeToSeverity(magnitude, alert));
        disaster.setEventTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));

        JsonNode coordinates = geometry.get("coordinates");
        if (coordinates != null && coordinates.isArray() && coordinates.size() >= 2) {
            disaster.setLongitude(coordinates.get(0).asDouble());
            disaster.setLatitude(coordinates.get(1).asDouble());
        }

        if (properties.has("url") && !properties.get("url").isNull()) {
            disaster.setAdvisoryMessage("More info: " + properties.get("url").asText());
        }

        return disaster;
    }

    private DisasterSeverity mapMagnitudeToSeverity(double magnitude, String alertLevel) {
        if (alertLevel != null) {
            switch (alertLevel.toLowerCase()) {
                case "red": return DisasterSeverity.EXTREME;
                case "orange": return DisasterSeverity.CRITICAL;
                case "yellow": return DisasterSeverity.HIGH;
                case "green": return DisasterSeverity.MODERATE;
            }
        }
        
        if (magnitude >= 8.0) return DisasterSeverity.EXTREME;
        if (magnitude >= 7.0) return DisasterSeverity.CRITICAL;
        if (magnitude >= 6.0) return DisasterSeverity.HIGH;
        if (magnitude >= 5.0) return DisasterSeverity.MODERATE;
        return DisasterSeverity.LOW;
    }

    public List<Disaster> fetchWeatherAlerts(double lat, double lon) {
        List<Disaster> disasters = new ArrayList<>();
        
        try {
            String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,wind_speed_10m,precipitation&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max&timezone=auto",
                lat, lon
            );
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            JsonNode current = root.get("current");
            if (current != null) {
                double windSpeed = current.has("wind_speed_10m") ? current.get("wind_speed_10m").asDouble() : 0;
                double precipitation = current.has("precipitation") ? current.get("precipitation").asDouble() : 0;
                double temperature = current.has("temperature_2m") ? current.get("temperature_2m").asDouble() : 20;

                if (windSpeed > 100) {
                    Disaster storm = new Disaster();
                    storm.setType(DisasterType.SEVERE_STORM);
                    storm.setTitle(String.format("Severe Storm Warning - Wind %.0f km/h", windSpeed));
                    storm.setDescription(String.format("Dangerous wind conditions detected. Wind speed: %.0f km/h", windSpeed));
                    storm.setSeverity(windSpeed > 150 ? DisasterSeverity.EXTREME : 
                                     windSpeed > 120 ? DisasterSeverity.CRITICAL : DisasterSeverity.HIGH);
                    storm.setLatitude(lat);
                    storm.setLongitude(lon);
                    storm.setEventTime(LocalDateTime.now());
                    storm.setSource("OpenMeteo");
                    storm.setCountry("India");
                    storm.setExternalId("storm-" + lat + "-" + lon + "-" + System.currentTimeMillis());
                    disasters.add(storm);
                }

                if (precipitation > 50) {
                    Disaster flood = new Disaster();
                    flood.setType(DisasterType.FLOOD);
                    flood.setTitle(String.format("Flood Risk Alert - %.0fmm precipitation", precipitation));
                    flood.setDescription(String.format("Heavy precipitation detected. Total: %.0fmm. Risk of flooding.", precipitation));
                    flood.setSeverity(precipitation > 100 ? DisasterSeverity.CRITICAL : 
                                     precipitation > 75 ? DisasterSeverity.HIGH : DisasterSeverity.MODERATE);
                    flood.setLatitude(lat);
                    flood.setLongitude(lon);
                    flood.setEventTime(LocalDateTime.now());
                    flood.setSource("OpenMeteo");
                    flood.setCountry("India");
                    flood.setExternalId("flood-" + lat + "-" + lon + "-" + System.currentTimeMillis());
                    disasters.add(flood);
                }

                if (temperature > 45) {
                    Disaster heatwave = new Disaster();
                    heatwave.setType(DisasterType.HEATWAVE);
                    heatwave.setTitle(String.format("Extreme Heat Warning - %.0f°C", temperature));
                    heatwave.setDescription(String.format("Dangerous heat conditions. Temperature: %.0f°C", temperature));
                    heatwave.setSeverity(temperature > 50 ? DisasterSeverity.EXTREME : DisasterSeverity.CRITICAL);
                    heatwave.setLatitude(lat);
                    heatwave.setLongitude(lon);
                    heatwave.setEventTime(LocalDateTime.now());
                    heatwave.setSource("OpenMeteo");
                    heatwave.setCountry("India");
                    heatwave.setExternalId("heat-" + lat + "-" + lon + "-" + System.currentTimeMillis());
                    disasters.add(heatwave);
                }

                if (temperature < -20) {
                    Disaster coldwave = new Disaster();
                    coldwave.setType(DisasterType.COLDWAVE);
                    coldwave.setTitle(String.format("Extreme Cold Warning - %.0f°C", temperature));
                    coldwave.setDescription(String.format("Dangerous cold conditions. Temperature: %.0f°C", temperature));
                    coldwave.setSeverity(temperature < -30 ? DisasterSeverity.EXTREME : DisasterSeverity.CRITICAL);
                    coldwave.setLatitude(lat);
                    coldwave.setLongitude(lon);
                    coldwave.setEventTime(LocalDateTime.now());
                    coldwave.setSource("OpenMeteo");
                    coldwave.setCountry("India");
                    coldwave.setExternalId("cold-" + lat + "-" + lon + "-" + System.currentTimeMillis());
                    disasters.add(coldwave);
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to fetch weather data: {}", e.getMessage());
        }

        return disasters;
    }

    public List<Disaster> fetchNasaEonetEvents() {
        List<Disaster> disasters = new ArrayList<>();
        
        try {
            String url = "https://eonet.gsfc.nasa.gov/api/v3/events?status=open&limit=50";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode events = root.get("events");

            if (events != null && events.isArray()) {
                for (JsonNode event : events) {
                    try {
                        Disaster disaster = parseEonetEvent(event);
                        if (disaster != null) {
                            disasters.add(disaster);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to parse EONET event: {}", e.getMessage());
                    }
                }
            }
            logger.info("Fetched {} events from NASA EONET", disasters.size());
        } catch (Exception e) {
            logger.error("Failed to fetch NASA EONET data: {}", e.getMessage());
        }

        return disasters;
    }

    private Disaster parseEonetEvent(JsonNode event) {
        Disaster disaster = new Disaster();
        
        String id = event.has("id") ? event.get("id").asText() : null;
        String title = event.has("title") ? event.get("title").asText() : "Unknown Event";
        
        disaster.setExternalId(id);
        disaster.setSource("NASA_EONET");
        disaster.setTitle(title);

        JsonNode categories = event.get("categories");
        if (categories != null && categories.isArray() && categories.size() > 0) {
            String categoryId = categories.get(0).has("id") ? categories.get(0).get("id").asText() : "";
            disaster.setType(mapEonetCategory(categoryId));
        } else {
            disaster.setType(DisasterType.OTHER);
        }

        JsonNode geometry = event.get("geometry");
        if (geometry != null && geometry.isArray() && geometry.size() > 0) {
            JsonNode latestGeom = geometry.get(geometry.size() - 1);
            JsonNode coordinates = latestGeom.get("coordinates");
            if (coordinates != null && coordinates.isArray() && coordinates.size() >= 2) {
                disaster.setLongitude(coordinates.get(0).asDouble());
                disaster.setLatitude(coordinates.get(1).asDouble());
            }
            
            String dateStr = latestGeom.has("date") ? latestGeom.get("date").asText() : null;
            if (dateStr != null) {
                try {
                    disaster.setEventTime(LocalDateTime.parse(dateStr.replace("Z", "")));
                } catch (Exception e) {
                    disaster.setEventTime(LocalDateTime.now());
                }
            }
        }

        disaster.setLocation(title);
        disaster.setSeverity(DisasterSeverity.MODERATE);
        disaster.setDescription("Event detected by NASA Earth Observatory Natural Event Tracker (EONET)");

        // Only include events within India bounding box
        if (disaster.getLatitude() != null && disaster.getLongitude() != null) {
            if (!isWithinIndia(disaster.getLatitude(), disaster.getLongitude())) {
                return null;
            }
        } else {
            return null; // Skip events without coordinates
        }

        return disaster;
    }

    private boolean isWithinIndia(double lat, double lon) {
        return lat >= INDIA_MIN_LAT && lat <= INDIA_MAX_LAT
                && lon >= INDIA_MIN_LON && lon <= INDIA_MAX_LON;
    }

    private DisasterType mapEonetCategory(String categoryId) {
        switch (categoryId.toLowerCase()) {
            case "wildfires": return DisasterType.WILDFIRE;
            case "volcanoes": return DisasterType.VOLCANIC_ERUPTION;
            case "severeStorms": return DisasterType.SEVERE_STORM;
            case "floods": return DisasterType.FLOOD;
            case "earthquakes": return DisasterType.EARTHQUAKE;
            case "drought": return DisasterType.DROUGHT;
            case "landslides": return DisasterType.LANDSLIDE;
            default: return DisasterType.OTHER;
        }
    }
}
