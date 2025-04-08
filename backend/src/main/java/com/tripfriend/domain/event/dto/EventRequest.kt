package com.tripfriend.domain.event.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EventRequest {
    private String title;
    private String description;
    private LocalDate eventDate;
}
