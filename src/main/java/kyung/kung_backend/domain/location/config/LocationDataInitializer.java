package kyung.kung_backend.domain.location.config;

import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LocationDataInitializer implements ApplicationRunner {

    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Location root = createRootLocation();

        createRegionGroup(root, 1L, "서울", List.of(
                "서울 전체",
                "강남구",
                "강동구",
                "강북구",
                "강서구",
                "관악구",
                "광진구",
                "구로구",
                "금천구",
                "노원구",
                "도봉구",
                "마포구",
                "서초구",
                "송파구",
                "영등포구",
                "종로구",
                "중구"
        ));

        createRegionGroup(root, 2L, "경기", List.of(
                "경기 전체",
                "고양시",
                "성남시",
                "수원시",
                "용인시",
                "부천시",
                "안양시"
        ));

        createRegionGroup(root, 3L, "인천", List.of(
                "인천 전체",
                "부평구",
                "남동구",
                "연수구"
        ));

        createRegionGroup(root, 4L, "강원", List.of(
                "강원 전체",
                "강릉시",
                "고성군",
                "춘천시",
                "원주시"
        ));

        createRegionGroup(root, 5L, "충북", List.of(
                "충북 전체",
                "청주시",
                "충주시"
        ));

        createRegionGroup(root, 6L, "충남", List.of(
                "충남 전체",
                "천안시",
                "아산시"
        ));

        createRegionGroup(root, 7L, "경북", List.of(
                "경북 전체",
                "포항시",
                "경주시"
        ));

        createRegionGroup(root, 8L, "경남", List.of(
                "경남 전체",
                "창원시",
                "김해시"
        ));

        createRegionGroup(root, 9L, "대전", List.of(
                "대전 전체"
        ));

        createRegionGroup(root, 10L, "대구", List.of(
                "대구 전체"
        ));

        createRegionGroup(root, 11L, "광주", List.of(
                "광주 전체"
        ));

        createRegionGroup(root, 12L, "부산", List.of(
                "부산 전체"
        ));

        createRegionGroup(root, 13L, "울산", List.of(
                "울산 전체"
        ));

        createRegionGroup(root, 14L, "전북", List.of(
                "전북 전체"
        ));

        createRegionGroup(root, 15L, "전남", List.of(
                "전남 전체"
        ));

        createRegionGroup(root, 16L, "세종", List.of(
                "세종 전체"
        ));

        createRegionGroup(root, 17L, "제주", List.of(
                "제주 전체"
        ));
    }

    private Location createRootLocation() {
        return locationRepository
                .findByNameAndDepthAndParentIsNull("전국", 0L)
                .orElseGet(() -> locationRepository.save(
                        Location.createRoot("전국", 1L)
                ));
    }

    private void createRegionGroup(
            Location root,
            Long regionSortOrder,
            String regionName,
            List<String> districtNames
    ) {
        Location region = locationRepository
                .findByNameAndParentAndDepth(regionName, root, 1L)
                .orElseGet(() -> locationRepository.save(
                        Location.createChild(root, regionName, 1L, regionSortOrder)
                ));

        for (int i = 0; i < districtNames.size(); i++) {
            String districtName = districtNames.get(i);
            Long districtSortOrder = (long) i + 1;

            locationRepository
                    .findByNameAndParentAndDepth(districtName, region, 2L)
                    .orElseGet(() -> locationRepository.save(
                            Location.createChild(region, districtName, 2L, districtSortOrder)
                    ));
        }
    }
}