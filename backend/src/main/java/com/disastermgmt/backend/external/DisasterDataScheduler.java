package com.disastermgmt.backend.external;

import com.disastermgmt.backend.disaster.Disaster;
import com.disastermgmt.backend.disaster.DisasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DisasterDataScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DisasterDataScheduler.class);

    private final ExternalApiService externalApiService;
    private final DisasterService disasterService;

    public DisasterDataScheduler(ExternalApiService externalApiService, DisasterService disasterService) {
        this.externalApiService = externalApiService;
        this.disasterService = disasterService;
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void fetchEarthquakeData() {
        logger.info("Fetching earthquake data from USGS...");
        try {
            List<Disaster> earthquakes = externalApiService.fetchEarthquakeData();
            int saved = 0;
            for (Disaster earthquake : earthquakes) {
                try {
                    disasterService.createFromExternalSource(earthquake);
                    saved++;
                } catch (Exception e) {
                    logger.warn("Failed to save earthquake: {}", e.getMessage());
                }
            }
            logger.info("Saved {} earthquakes to database", saved);
        } catch (Exception e) {
            logger.error("Error in earthquake fetch job: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 600000) // Every 10 minutes
    public void fetchNasaEonetData() {
        logger.info("Fetching disaster events from NASA EONET...");
        try {
            List<Disaster> events = externalApiService.fetchNasaEonetEvents();
            int saved = 0;
            for (Disaster event : events) {
                try {
                    disasterService.createFromExternalSource(event);
                    saved++;
                } catch (Exception e) {
                    logger.warn("Failed to save EONET event: {}", e.getMessage());
                }
            }
            logger.info("Saved {} NASA EONET events to database", saved);
        } catch (Exception e) {
            logger.error("Error in NASA EONET fetch job: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 900000) // Every 15 minutes
    public void fetchWeatherAlertsForMajorCities() {
        logger.info("Fetching weather alerts for major cities...");
        
        // Indian cities only - lat, lon
        double[][] majorCities = {
            {28.6139, 77.2090},  // Delhi
            {19.0760, 72.8777},  // Mumbai
            {13.0827, 80.2707},  // Chennai
            {22.5726, 88.3639},  // Kolkata
            {12.9716, 77.5946},  // Bangalore
            {23.0225, 72.5714},  // Ahmedabad
            {17.3850, 78.4867},  // Hyderabad
            {26.9124, 75.7873},  // Jaipur
            {26.8467, 80.9462},  // Lucknow
            {21.1702, 72.8311},  // Surat
            {19.9975, 73.7898},  // Nashik
            {15.2993, 74.1240},  // Goa
            {11.0168, 76.9558},  // Coimbatore
        };

        int totalAlerts = 0;
        for (double[] city : majorCities) {
            try {
                List<Disaster> alerts = externalApiService.fetchWeatherAlerts(city[0], city[1]);
                for (Disaster alert : alerts) {
                    try {
                        disasterService.createFromExternalSource(alert);
                        totalAlerts++;
                    } catch (Exception e) {
                        logger.warn("Failed to save weather alert: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to fetch weather for city [{}, {}]: {}", city[0], city[1], e.getMessage());
            }
        }
        logger.info("Processed {} weather alerts", totalAlerts);
    }

    public void triggerManualFetch() {
        logger.info("Manual fetch triggered (India data only)...");
        fetchEarthquakeData();
        fetchNasaEonetData();
        fetchWeatherAlertsForMajorCities();
    }
}
