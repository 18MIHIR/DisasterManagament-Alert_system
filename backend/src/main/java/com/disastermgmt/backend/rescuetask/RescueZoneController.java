package com.disastermgmt.backend.rescuetask;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rescue-zones")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class RescueZoneController {

    private final RescueZoneRepository rescueZoneRepository;

    public RescueZoneController(RescueZoneRepository rescueZoneRepository) {
        this.rescueZoneRepository = rescueZoneRepository;
    }

    @GetMapping
    public ResponseEntity<List<RescueZoneDTO>> listZones() {
        List<RescueZoneDTO> list = rescueZoneRepository.findAllByOrderByRegionAscNameAsc().stream()
                .map(RescueZoneDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
