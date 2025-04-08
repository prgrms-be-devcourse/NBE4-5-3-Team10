package com.tripfriend.domain.event.service;

import com.tripfriend.domain.event.dto.EventRequest;
import com.tripfriend.domain.event.dto.EventResponse;
import com.tripfriend.domain.event.entity.Event;
import com.tripfriend.domain.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public List<EventResponse> findAll() {
        return eventRepository.findAll().stream()
                .map(event -> EventResponse.builder()
                        .id(event.getId())
                        .title(event.getTitle())
                        .description(event.getDescription())
                        .eventDate(event.getEventDate())
                        .createdAt(event.getCreatedAt())
                        .build()
                ).collect(Collectors.toList());
    }

    public EventResponse create(EventRequest request) {
        Event event = new Event(
                null,
                request.getTitle(),
                request.getDescription(),
                request.getEventDate(),
                null,
                null
        );


        Event saved = eventRepository.save(event);

        return EventResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .eventDate(saved.getEventDate())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public void delete(Long id) {
        eventRepository.deleteById(id);
    }

    public EventResponse update(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("이벤트를 찾을 수 없습니다."));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        // 수정 시간 자동 업데이트

        Event updated = eventRepository.save(event);

        return EventResponse.builder()
                .id(updated.getId())
                .title(updated.getTitle())
                .description(updated.getDescription())
                .eventDate(updated.getEventDate())
                .createdAt(updated.getCreatedAt())
                .build();
    }

}
