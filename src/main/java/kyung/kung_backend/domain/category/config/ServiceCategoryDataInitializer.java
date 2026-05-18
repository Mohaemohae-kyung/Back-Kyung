package kyung.kung_backend.domain.category.config;

import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.category.repository.ServiceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ServiceCategoryDataInitializer implements ApplicationRunner {

    private final ServiceCategoryRepository serviceCategoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createCategoryGroup(1L, "취업/직무", List.of(
                "취업 준비",
                "창업 준비",
                "시험/자격증",
                "기타 실무"
        ));

        createCategoryGroup(2L, "취미/자기계발", List.of(
                "음악이론/보컬",
                "미술/드로잉",
                "연기/마술",
                "기타 취미/자기계발"
        ));

        createCategoryGroup(3L, "과외", List.of(
                "국내 입시",
                "유학 준비",
                "체육",
                "무용/댄스"
        ));

        createCategoryGroup(4L, "외주", List.of(
                "디자인 외주",
                "개발 외주",
                "번역 외주",
                "마케팅"
        ));

        createCategoryGroup(5L, "기타", List.of(
                "심리",
                "번역 작업",
                "심부름"
        ));
    }

    private void createCategoryGroup(
            Long rootSortOrder,
            String rootName,
            List<String> childNames
    ) {
        ServiceCategory rootCategory = serviceCategoryRepository
                .findByNameAndDepthAndParentIsNull(rootName, 1L)
                .orElseGet(() -> serviceCategoryRepository.save(
                        ServiceCategory.createRoot(rootName, rootSortOrder)
                ));

        for (int i = 0; i < childNames.size(); i++) {
            String childName = childNames.get(i);
            Long childSortOrder = (long) i + 1;

            serviceCategoryRepository
                    .findByNameAndParentAndDepth(childName, rootCategory, 2L)
                    .orElseGet(() -> serviceCategoryRepository.save(
                            ServiceCategory.createChild(rootCategory, childName, childSortOrder)
                    ));
        }
    }
}