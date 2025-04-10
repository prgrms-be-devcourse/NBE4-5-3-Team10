package com.tripfriend.domain.place.place.controller;

import com.tripfriend.domain.place.place.dto.PlaceCreateReqDto;
import com.tripfriend.domain.place.place.dto.PlaceResDto;
import com.tripfriend.domain.place.place.entity.Place;
import com.tripfriend.domain.place.place.service.PlaceService;
import com.tripfriend.global.annotation.CheckPermission;
import com.tripfriend.global.dto.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/place")
@Tag(name = "Place API", description = "여행지 관련 기능을 제공합니다.")
public class PlaceController {

    private final PlaceService placeService;

    // 여행지 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CheckPermission("ADMIN") //관리자
    @Operation(summary = "여행지 등록", description = "새로운 여행지를 등록합니다.")
    public RsData<PlaceResDto> createPlace(@ModelAttribute PlaceCreateReqDto req) {

        Place savePlace = placeService.createPlace(req);
        PlaceResDto placeResDto = new PlaceResDto(savePlace);
        return new RsData<>(
                "200-1",
                "여행지가 성공적으로 등록되었습니다.",
                placeResDto
        );
    }

    // 전체 여행지 조회
    @GetMapping
    @Operation(summary = "전체 여행지 조회", description = "모든 여행지를 조회합니다.")
    public RsData<List<PlaceResDto>> getAllPlaces(@RequestParam(required = false) String cityName) {
        List<Place> places;
        if (cityName != null && !cityName.isEmpty()) {
            places = placeService.getPlacesByCity(cityName);
        } else {
            places = placeService.getAllPlaces();
        }
        List<PlaceResDto> placeResDtos = places.stream().map(PlaceResDto::new).toList();
        return new RsData<>(
                "200-2",
                "전체 여행지가 성공적으로 조회되었습니다.",
                placeResDtos
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "특정 여행지 조회", description = "특정 여행지를 조회합니다.")
    public RsData<PlaceResDto> getPlace(@PathVariable Long id) {
        Place place = placeService.getPlace(id);
        PlaceResDto placeResDto = new PlaceResDto(place);

        return new RsData<>(
                "200-3",
                "특정 여행지가 성공적으로 조회되었습니다.",
                placeResDto
        );
    }

    // 등록된 도시 목록 조회
    @GetMapping("/cities")
    @Operation(summary = "도시 목록 조회", description = "현재 등록된 도시 목록을 반환합니다.")
    public RsData<List<String>> getDistinctCities() {
        List<String> cities = placeService.getDistinctCities();
        return new RsData<>(
                "200-7",
                "도시 목록 조회 성공",
                cities
        );
    }

    // 여행지 검색
    @GetMapping("/search")
    @Operation(summary = "여행지 검색", description = "장소명 또는 도시명으로 검색하여 일치하는 여행지 정보를 반환합니다.")
    public RsData<List<PlaceResDto>> searchPlace(
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) String placeName) {
        List<Place> places = placeService.searchPlace(placeName, cityName);
        List<PlaceResDto> placeResDtos = places.stream().map(PlaceResDto::new).toList();
        return new RsData<>(
                "200-6",
                "여행지 검색 성공",
                placeResDtos
        );
    }

    // 특정 여행지 삭제
    @DeleteMapping("/{id}")
    @CheckPermission("ADMIN") //관리자
    @Operation(summary = "여행지 삭제", description = "특정 여행지를 삭제합니다.")
    public RsData<Void> deletePlace(@Parameter(description = "여행지 ID", required = true, example = "1")
                                    @PathVariable Long id) {
        Place place = placeService.getPlace(id);
        placeService.deletePlace(place);
        return new RsData<>(
                "200-4",
                "여행지가 성공적으로 삭제되었습니다."
        );
    }

    // 특정 여행지 정보 수정(사용안함)
//    @PutMapping("/{id}")
//    @CheckPermission("ADMIN") //관리자
//    @Operation(summary = "여행지 정보 수정", description = "특정 여행지의 정보를 수정할 수 있습니다.")
//    public RsData<PlaceResDto> updatePlace(@Parameter(description = "여행지 ID", required = true, example = "1")
//                                           @PathVariable Long id,
//                                           @RequestBody PlaceUpdateReqDto placeUpdateReqDto) {
//        Place place = placeService.getPlace(id);
//        Place updatePlace = placeService.updatePlace(place, placeUpdateReqDto);
//        PlaceResDto placeResDto = new PlaceResDto(updatePlace);
//        return new RsData<>(
//                "200-5",
//                "여행지 정보가 성공적으로 수정되었습니다.",
//                placeResDto
//        );
//    }
}
