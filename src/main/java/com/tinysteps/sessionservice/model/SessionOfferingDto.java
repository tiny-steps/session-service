package com.tinysteps.sessionservice.model;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionOfferingDto {
    private UUID id;
    private UUID doctorId;
    private UUID practiceId;
    private UUID sessionTypeId;
    private String sessionTypeName;
    private BigDecimal price;
    private boolean isActive;
}
