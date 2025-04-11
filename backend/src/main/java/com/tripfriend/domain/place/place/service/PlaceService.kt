package com.tripfriend.domain.place.place.service

import com.tripfriend.domain.place.place.dto.PlaceCreateReqDto
import com.tripfriend.domain.place.place.entity.Place
import com.tripfriend.domain.place.place.repository.PlaceRepository
import com.tripfriend.global.annotation.CheckPermission
import com.tripfriend.global.exception.ServiceException
import com.tripfriend.global.util.ImageUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@Service
class PlaceService(
    private val placeRepository: PlaceRepository,
    private val imageUtil: ImageUtil
) {

    // 여행 장소 등록
    @Transactional
    fun createPlace(req: PlaceCreateReqDto): Place {
        var imageUrl: String? = null
        val imageFile: MultipartFile? = req.imageUrl

        if (imageFile != null && !imageFile.isEmpty) {
            imageUrl = try {
                uploadPlaceImage(imageFile)
            } catch (e: IOException) {
                throw ServiceException("404-2", "이미지 업로드에 실패했습니다.")
            }
        }

        val place = Place().apply {
            cityName = req.cityName
            placeName = req.placeName
            description = req.description
            category = req.category
            this.imageUrl = imageUrl
        }

        placeRepository.save(place)
        return place
    }

    // 여행 장소 전체 리스트 조회
    fun getAllPlaces(): List<Place> = placeRepository.findAll()

    // 특정 도시의 여행 장소 리스트 조회
    fun getPlacesByCity(cityName: String): List<Place> = placeRepository.findByCityName(cityName)

    // 도시 목록 중복 제거
    fun getDistinctCities(): List<String> = placeRepository.findDistinctCityNames()

    // 여행 장소 단건 조회
    fun getPlace(id: Long): Place =
        placeRepository.findById(id)
            .orElseThrow { ServiceException("404-1", "해당 장소가 존재하지 않습니다.") }

    // 여행 장소 삭제
    @CheckPermission("ADMIN")
    @Transactional
    fun deletePlace(place: Place) {
        placeRepository.delete(place)
    }

    // 여행 장소 수정 (사용 안함)
    /*
    @Transactional
    fun updatePlace(place: Place, req: PlaceUpdateReqDto): Place {
        place.apply {
            cityName = req.cityName
            placeName = req.placeName
            description = req.description
            category = req.category
        }
        return placeRepository.save(place)
    }
    */

    // 여행지 이미지 등록
    @Throws(IOException::class)
    fun uploadPlaceImage(imageFile: MultipartFile): String? =
        imageUtil.saveImage(imageFile)

    // 여행 장소 검색
    fun searchPlace(name: String?, city: String?): List<Place> {
        return when {
            !name.isNullOrEmpty() && !city.isNullOrEmpty() ->
                placeRepository.findByPlaceNameContainingIgnoreCaseAndCityNameContainingIgnoreCase(name, city)
            !name.isNullOrEmpty() ->
                placeRepository.findByPlaceNameContainingIgnoreCase(name)
            !city.isNullOrEmpty() ->
                placeRepository.findByCityNameContainingIgnoreCase(city)
            else -> emptyList()
        }
    }
}