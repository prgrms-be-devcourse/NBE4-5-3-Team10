package com.tripfriend.domain.trip.information.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitedReqDto {
    @NotNull
    private Long tripInformationId;
    @NotNull
    private Boolean isVisited;
}
