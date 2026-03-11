package io.github.bluething.pathpulse.ingestionservice.service;

import io.github.bluething.pathpulse.ingestionservice.model.LocationData;

public interface LocationLabeler {
    String label(LocationData data);
}
