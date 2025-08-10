package com.tinysteps.sessionservice.model;

import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionTypeDto {
    private UUID id;
    private String name;
    private String description;
    private int defaultDurationMinutes;
    private boolean isTelemedicineAvailable;
    private boolean isActive;
}
