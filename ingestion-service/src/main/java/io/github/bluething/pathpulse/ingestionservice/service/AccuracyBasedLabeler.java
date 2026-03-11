package io.github.bluething.pathpulse.ingestionservice.service;

import io.github.bluething.pathpulse.ingestionservice.model.LocationData;
import org.springframework.stereotype.Service;

@Service
class AccuracyBasedLabeler implements LocationLabeler {
    @Override
    public String label(LocationData data) {
        return "";
    }
}
