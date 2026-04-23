package com.internship.tool.service;

import com.internship.tool.dto.RiskRegisterRequest;
import com.internship.tool.dto.RiskRegisterResponse;
import java.util.List;

public interface RiskRegisterService {

    RiskRegisterResponse createRiskRegister(RiskRegisterRequest request);

    RiskRegisterResponse updateRiskRegister(Long id, RiskRegisterRequest request);

    RiskRegisterResponse getRiskRegisterById(Long id);

    List<RiskRegisterResponse> getAllRiskRegisters();

    List<RiskRegisterResponse> getRiskRegistersByStatus(String status);

    List<RiskRegisterResponse> searchRiskRegisters(String keyword);

    RiskRegisterResponse deactivateRiskRegister(Long id);
}
