package com.internship.tool.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.internship.tool.entity.RiskRegister;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.data.redis.repositories.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RiskRegisterRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RiskRegisterRepository riskRegisterRepository;

    @Test
    void shouldFindRiskByCustomFilters() {
        RiskRegister riskRegister = new RiskRegister();
        riskRegister.setRiskCode("RISK-001");
        riskRegister.setTitle("Vendor delay");
        riskRegister.setDescription("Key vendor may delay the import file delivery.");
        riskRegister.setCategory("Operational");
        riskRegister.setStatus("Open");
        riskRegister.setPriority("High");
        riskRegister.setImpactLevel("High");
        riskRegister.setLikelihoodLevel("Medium");
        riskRegister.setOwnerName("Anagha");
        riskRegister.setOwnerEmail("anagha@example.com");
        riskRegister.setMitigationPlan("Follow up with vendor and prepare manual fallback.");
        riskRegister.setTargetResolutionDate(LocalDate.now().minusDays(1));
        riskRegister.setSourceSystem("Portal");
        riskRegister.setActive(true);
        riskRegister.setCreatedAt(Instant.now());
        riskRegister.setUpdatedAt(Instant.now());

        entityManager.persistAndFlush(riskRegister);

        assertThat(riskRegisterRepository.findByRiskCode("RISK-001")).isPresent();
        assertThat(riskRegisterRepository.findByStatusIgnoreCase("open")).hasSize(1);
        assertThat(riskRegisterRepository.findByCategoryIgnoreCaseAndActiveTrue("operational")).hasSize(1);
        assertThat(riskRegisterRepository.findByOwnerEmailIgnoreCase("ANAGHA@example.com")).hasSize(1);
        assertThat(riskRegisterRepository.findByTargetResolutionDateBeforeAndActiveTrue(LocalDate.now())).hasSize(1);

        List<RiskRegister> searchResults = riskRegisterRepository.searchByKeyword("vendor");
        assertThat(searchResults).hasSize(1);
        assertThat(riskRegisterRepository.countByStatusCustom("OPEN")).isEqualTo(1);
    }
}
