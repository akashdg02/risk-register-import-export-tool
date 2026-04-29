package com.internship.tool.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.internship.tool.dto.RiskRegisterRequest;
import com.internship.tool.dto.RiskRegisterResponse;
import com.internship.tool.entity.RiskRegister;
import com.internship.tool.exception.DuplicateRiskCodeException;
import com.internship.tool.exception.InactiveRiskOperationException;
import com.internship.tool.exception.InvalidRiskDataException;
import com.internship.tool.exception.RiskRegisterNotFoundException;
import com.internship.tool.repository.RiskRegisterRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RiskRegisterServiceImplTests {

    @Mock
    private RiskRegisterRepository riskRegisterRepository;

    private RiskRegisterServiceImpl riskRegisterService;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        riskRegisterService = new RiskRegisterServiceImpl(riskRegisterRepository, validator);
    }

    @Test
    void shouldCreateRiskRegisterWhenRequestIsValid() {
        RiskRegisterRequest request = createRequest();
        RiskRegister savedRisk = createRiskEntity(1L, true);

        when(riskRegisterRepository.findByRiskCode("RISK-101")).thenReturn(Optional.empty());
        when(riskRegisterRepository.save(any(RiskRegister.class))).thenReturn(savedRisk);

        RiskRegisterResponse response = riskRegisterService.createRiskRegister(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRiskCode()).isEqualTo("RISK-101");
        verify(riskRegisterRepository).save(any(RiskRegister.class));
    }

    @Test
    void shouldThrowDuplicateRiskCodeExceptionWhenRiskCodeAlreadyExists() {
        RiskRegisterRequest request = createRequest();
        when(riskRegisterRepository.findByRiskCode("RISK-101")).thenReturn(Optional.of(createRiskEntity(99L, true)));

        assertThatThrownBy(() -> riskRegisterService.createRiskRegister(request))
            .isInstanceOf(DuplicateRiskCodeException.class)
            .hasMessageContaining("Risk code already exists");
    }

    @Test
    void shouldThrowInvalidRiskDataExceptionWhenValidationFails() {
        RiskRegisterRequest request = createRequest();
        request.setOwnerEmail("wrong-email");

        assertThatThrownBy(() -> riskRegisterService.createRiskRegister(request))
            .isInstanceOf(InvalidRiskDataException.class)
            .hasMessageContaining("Owner email must be valid");
    }

    @Test
    void shouldReturnRisksByStatus() {
        when(riskRegisterRepository.findByStatusIgnoreCase("Open"))
            .thenReturn(List.of(createRiskEntity(1L, true)));

        List<RiskRegisterResponse> responses = riskRegisterService.getRiskRegistersByStatus("Open");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo("Open");
    }

    @Test
    void shouldDeactivateActiveRisk() {
        RiskRegister existingRisk = createRiskEntity(1L, true);
        RiskRegister deactivatedRisk = createRiskEntity(1L, false);

        when(riskRegisterRepository.findById(1L)).thenReturn(Optional.of(existingRisk));
        when(riskRegisterRepository.save(any(RiskRegister.class))).thenReturn(deactivatedRisk);

        RiskRegisterResponse response = riskRegisterService.deactivateRiskRegister(1L);

        assertThat(response.getActive()).isFalse();
    }

    @Test
    void shouldThrowInactiveRiskOperationExceptionWhenDeactivatingInactiveRisk() {
        when(riskRegisterRepository.findById(1L)).thenReturn(Optional.of(createRiskEntity(1L, false)));

        assertThatThrownBy(() -> riskRegisterService.deactivateRiskRegister(1L))
            .isInstanceOf(InactiveRiskOperationException.class)
            .hasMessageContaining("inactive");
    }

    @Test
    void shouldThrowRiskRegisterNotFoundExceptionWhenRiskDoesNotExist() {
        when(riskRegisterRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riskRegisterService.getRiskRegisterById(100L))
            .isInstanceOf(RiskRegisterNotFoundException.class)
            .hasMessageContaining("100");
    }

    private RiskRegisterRequest createRequest() {
        RiskRegisterRequest request = new RiskRegisterRequest();
        request.setRiskCode("RISK-101");
        request.setTitle("API timeout");
        request.setDescription("Import API may timeout on large files.");
        request.setCategory("Technical");
        request.setStatus("Open");
        request.setPriority("High");
        request.setImpactLevel("High");
        request.setLikelihoodLevel("Medium");
        request.setOwnerName("Anagha");
        request.setOwnerEmail("anagha@example.com");
        request.setMitigationPlan("Tune timeouts and add retries.");
        request.setTargetResolutionDate(LocalDate.now().plusDays(3));
        request.setSourceSystem("Import Portal");
        request.setActive(true);
        return request;
    }

    private RiskRegister createRiskEntity(Long id, boolean active) {
        RiskRegister riskRegister = new RiskRegister();
        riskRegister.setId(id);
        riskRegister.setRiskCode("RISK-101");
        riskRegister.setTitle("API timeout");
        riskRegister.setDescription("Import API may timeout on large files.");
        riskRegister.setCategory("Technical");
        riskRegister.setStatus("Open");
        riskRegister.setPriority("High");
        riskRegister.setImpactLevel("High");
        riskRegister.setLikelihoodLevel("Medium");
        riskRegister.setOwnerName("Anagha");
        riskRegister.setOwnerEmail("anagha@example.com");
        riskRegister.setMitigationPlan("Tune timeouts and add retries.");
        riskRegister.setTargetResolutionDate(LocalDate.now().plusDays(3));
        riskRegister.setSourceSystem("Import Portal");
        riskRegister.setActive(active);
        riskRegister.setCreatedAt(Instant.now());
        riskRegister.setUpdatedAt(Instant.now());
        return riskRegister;
    }
}
