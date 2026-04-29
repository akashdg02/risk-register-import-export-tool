package com.internship.tool.service;

import com.internship.tool.dto.RiskRegisterRequest;
import com.internship.tool.dto.RiskRegisterResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RiskRegisterService {

    RiskRegisterResponse createRiskRegister(RiskRegisterRequest request);

    RiskRegisterResponse updateRiskRegister(Long id, RiskRegisterRequest request);

    RiskRegisterResponse getRiskRegisterById(Long id);

    List<RiskRegisterResponse> getAllRiskRegisters();

    Page<RiskRegisterResponse> getAllRiskRegisters(Pageable pageable);

    List<RiskRegisterResponse> getRiskRegistersByStatus(String status);

    List<RiskRegisterResponse> searchRiskRegisters(String keyword);

    RiskRegisterResponse deactivateRiskRegister(Long id);
}
