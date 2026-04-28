package com.internship.tool.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.dto.RiskRegisterRequest;
import com.internship.tool.dto.RiskRegisterResponse;
import com.internship.tool.service.RiskRegisterService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class RiskRegisterSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RiskRegisterService riskRegisterService;

    @Test
    @WithMockUser(roles = "USER")
    void shouldAllowUserToReadRiskRegisters() throws Exception {
        when(riskRegisterService.getAllRiskRegisters(any()))
            .thenReturn(new PageImpl<>(List.of(createResponse())));

        mockMvc.perform(get("/risk-registers/all?page=0&size=5"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldForbidCreateWhenUserDoesNotHaveAdminRole() throws Exception {
        mockMvc.perform(post("/risk-registers/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowCreateWhenUserHasAdminRole() throws Exception {
        when(riskRegisterService.createRiskRegister(any(RiskRegisterRequest.class))).thenReturn(createResponse());

        mockMvc.perform(post("/risk-registers/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())))
            .andExpect(status().isCreated());
    }

    private RiskRegisterRequest createRequest() {
        RiskRegisterRequest request = new RiskRegisterRequest();
        request.setRiskCode("RISK-201");
        request.setTitle("File corruption");
        request.setDescription("Import file may contain corrupted rows.");
        request.setCategory("Data");
        request.setStatus("Open");
        request.setPriority("High");
        request.setImpactLevel("High");
        request.setLikelihoodLevel("Medium");
        request.setOwnerName("Anagha");
        request.setOwnerEmail("anagha@example.com");
        request.setMitigationPlan("Validate file before processing.");
        request.setTargetResolutionDate(LocalDate.now().plusDays(2));
        request.setSourceSystem("Bulk Import");
        request.setActive(true);
        return request;
    }

    private RiskRegisterResponse createResponse() {
        RiskRegisterResponse response = new RiskRegisterResponse();
        response.setId(1L);
        response.setRiskCode("RISK-201");
        response.setTitle("File corruption");
        response.setDescription("Import file may contain corrupted rows.");
        response.setCategory("Data");
        response.setStatus("Open");
        response.setPriority("High");
        response.setImpactLevel("High");
        response.setLikelihoodLevel("Medium");
        response.setOwnerName("Anagha");
        response.setOwnerEmail("anagha@example.com");
        response.setMitigationPlan("Validate file before processing.");
        response.setTargetResolutionDate(LocalDate.now().plusDays(2));
        response.setSourceSystem("Bulk Import");
        response.setActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());
        return response;
    }
}
