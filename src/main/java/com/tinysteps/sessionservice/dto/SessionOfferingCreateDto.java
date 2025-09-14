package com.tinysteps.sessionservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionOfferingCreateDto {

    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    @NotNull(message = "Branch ID is required")
    private UUID branchId;

    @NotNull(message = "Session Type ID is required")
    private UUID sessionTypeId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;

    @Builder.Default
    private boolean isActive = true;
}
