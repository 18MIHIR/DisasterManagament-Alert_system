package com.disastermgmt.backend.rescuetask;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RescueZoneDataLoader implements ApplicationRunner {

    private final RescueZoneRepository rescueZoneRepository;

    public RescueZoneDataLoader(RescueZoneRepository rescueZoneRepository) {
        this.rescueZoneRepository = rescueZoneRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (rescueZoneRepository.count() > 0) {
            return;
        }
        seed("Downtown Core", "Maharashtra", 19.0760, 72.8777, "Urban response grid — Mumbai");
        seed("Harbor District", "Maharashtra", 18.9400, 72.8350, "Coastal and port-adjacent zone");
        seed("North Bay", "Karnataka", 12.9716, 77.5946, "Bengaluru metro north");
        seed("South Corridor", "Karnataka", 12.8500, 77.6000, "Outer southern corridor");
        seed("Capital Central", "Delhi", 28.6139, 77.2090, "NDMC central operations");
        seed("Bay Area North", "California", 37.7749, -122.4194, "San Francisco — demo coordinates");
    }

    private void seed(String name, String region, double lat, double lon, String description) {
        RescueZone z = new RescueZone();
        z.setName(name);
        z.setRegion(region);
        z.setCenterLatitude(lat);
        z.setCenterLongitude(lon);
        z.setDescription(description);
        rescueZoneRepository.save(z);
    }
}
