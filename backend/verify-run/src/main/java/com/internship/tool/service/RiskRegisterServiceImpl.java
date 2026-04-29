package com.internship.tool.service;

import com.internship.tool.dto.RiskRegisterRequest;
import com.internship.tool.dto.RiskRegisterResponse;
import com.internship.tool.entity.RiskRegister;
import com.internship.tool.exception.DuplicateRiskCodeException;
import com.internship.tool.exception.InactiveRiskOperationException;
import com.internship.tool.exception.InvalidRiskDataException;
import com.internship.tool.exception.RiskRegisterNotFoundException;
import com.internship.tool.repository.RiskRegisterRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RiskRegisterServiceImpl implements RiskRegisterService {

    private final RiskRegisterRepository riskRegisterRepository;
    private final Validator validator;

    public RiskRegisterServiceImpl(RiskRegisterRepository riskRegisterRepository, Validator validator) {
        this.riskRegisterRepository = riskRegisterRepository;
        this.validator = validator;
    }

    @Override
    public RiskRegisterResponse createRiskRegister(RiskRegisterRequest request) {
        validateRequest(request);
        validateUniqueRiskCode(request.getRiskCode(), null);

        RiskRegister riskRegister = mapToEntity(request, new RiskRegister());
        return mapToResponse(riskRegisterRepository.save(riskRegister));
    }

    @Override
    public RiskRegisterResponse updateRiskRegister(Long id, RiskRegisterRequest request) {
        validateRequest(request);

        RiskRegister existingRisk = getActiveRiskOrThrow(id);
        validateUniqueRiskCode(request.getRiskCode(), id);

        RiskRegister updatedRisk = mapToEntity(request, existingRisk);
        return mapToResponse(riskRegisterRepository.save(updatedRisk));
    }

    @Override
    @Transactional(readOnly = true)
    public RiskRegisterResponse getRiskRegisterById(Long id) {
        return mapToResponse(getRiskOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiskRegisterResponse> getAllRiskRegisters() {
        return riskRegisterRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(RiskRegister::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .map(this::mapToResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RiskRegisterResponse> getAllRiskRegisters(Pageable pageable) {
        return riskRegisterRepository.findAll(pageable)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiskRegisterResponse> getRiskRegistersByStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new InvalidRiskDataException("Status is required to filter risk registers");
        }

        return riskRegisterRepository.findByStatusIgnoreCase(status.trim())
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiskRegisterResponse> searchRiskRegisters(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new InvalidRiskDataException("Keyword is required to search risk registers");
        }

        return riskRegisterRepository.searchByKeyword(keyword.trim())
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Override
    public RiskRegisterResponse deactivateRiskRegister(Long id) {
        RiskRegister riskRegister = getActiveRiskOrThrow(id);
        riskRegister.setActive(Boolean.FALSE);
        return mapToResponse(riskRegisterRepository.save(riskRegister));
    }

    private void validateRequest(RiskRegisterRequest request) {
        if (request == null) {
            throw new InvalidRiskDataException("Risk register request cannot be null");
        }

        Set<ConstraintViolation<RiskRegisterRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining(", "));
            throw new InvalidRiskDataException(message);
        }

        if (request.getTargetResolutionDate() != null
            && request.getTargetResolutionDate().isBefore(LocalDate.now())) {
            throw new InvalidRiskDataException("Target resolution date must be today or later");
        }
    }

    private void validateUniqueRiskCode(String riskCode, Long currentRiskId) {
        riskRegisterRepository.findByRiskCode(riskCode.trim())
            .filter(existingRisk -> currentRiskId == null || !existingRisk.getId().equals(currentRiskId))
            .ifPresent(existingRisk -> {
                throw new DuplicateRiskCodeException("Risk code already exists: " + riskCode);
            });
    }

    private RiskRegister getRiskOrThrow(Long id) {
        return riskRegisterRepository.findById(id)
            .orElseThrow(() -> new RiskRegisterNotFoundException("Risk register not found for id: " + id));
    }

    private RiskRegister getActiveRiskOrThrow(Long id) {
        RiskRegister riskRegister = getRiskOrThrow(id);
        if (Boolean.FALSE.equals(riskRegister.getActive())) {
            throw new InactiveRiskOperationException("Risk register is inactive for id: " + id);
        }
        return riskRegister;
    }

    private RiskRegister mapToEntity(RiskRegisterRequest request, RiskRegister riskRegister) {
        riskRegister.setRiskCode(request.getRiskCode().trim());
        riskRegister.setTitle(request.getTitle().trim());
        riskRegister.setDescription(request.getDescription().trim());
        riskRegister.setCategory(request.getCategory().trim());
        riskRegister.setStatus(request.getStatus().trim());
        riskRegister.setPriority(request.getPriority().trim());
        riskRegister.setImpactLevel(request.getImpactLevel().trim());
        riskRegister.setLikelihoodLevel(request.getLikelihoodLevel().trim());
        riskRegister.setOwnerName(request.getOwnerName().trim());
        riskRegister.setOwnerEmail(request.getOwnerEmail().trim().toLowerCase());
        riskRegister.setMitigationPlan(trimToNull(request.getMitigationPlan()));
        riskRegister.setTargetResolutionDate(request.getTargetResolutionDate());
        riskRegister.setSourceSystem(trimToNull(request.getSourceSystem()));
        riskRegister.setActive(request.getActive());
        return riskRegister;
    }

    private RiskRegisterResponse mapToResponse(RiskRegister riskRegister) {
        RiskRegisterResponse response = new RiskRegisterResponse();
        response.setId(riskRegister.getId());
        response.setRiskCode(riskRegister.getRiskCode());
        response.setTitle(riskRegister.getTitle());
        response.setDescription(riskRegister.getDescription());
        response.setCategory(riskRegister.getCategory());
        response.setStatus(riskRegister.getStatus());
        response.setPriority(riskRegister.getPriority());
        response.setImpactLevel(riskRegister.getImpactLevel());
        response.setLikelihoodLevel(riskRegister.getLikelihoodLevel());
        response.setOwnerName(riskRegister.getOwnerName());
        response.setOwnerEmail(riskRegister.getOwnerEmail());
        response.setMitigationPlan(riskRegister.getMitigationPlan());
        response.setTargetResolutionDate(riskRegister.getTargetResolutionDate());
        response.setSourceSystem(riskRegister.getSourceSystem());
        response.setActive(riskRegister.getActive());
        response.setCreatedAt(riskRegister.getCreatedAt());
        response.setUpdatedAt(riskRegister.getUpdatedAt());
        return response;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
