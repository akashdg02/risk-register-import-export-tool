package com.internship.tool.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.dto.RiskRegisterRequest;
import com.internship.tool.dto.RiskRegisterResponse;
import com.internship.tool.exception.DuplicateRiskCodeException;
import com.internship.tool.exception.GlobalExceptionHandler;
import com.internship.tool.exception.RiskRegisterNotFoundException;
import com.internship.tool.service.RiskRegisterService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RiskRegisterController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RiskRegisterControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RiskRegisterService riskRegisterService;

    @Test
    void shouldReturnPaginatedRiskRegisters() throws Exception {
        when(riskRegisterService.getAllRiskRegisters(any()))
            .thenReturn(new PageImpl<>(List.of(createResponse())));

        mockMvc.perform(get("/risk-registers/all?page=0&size=5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].riskCode").value("RISK-201"));
    }

    @Test
    void shouldReturnRiskRegisterById() throws Exception {
        when(riskRegisterService.getRiskRegisterById(1L)).thenReturn(createResponse());

        mockMvc.perform(get("/risk-registers/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("File corruption"));
    }

    @Test
    void shouldReturnNotFoundWhenRiskRegisterDoesNotExist() throws Exception {
        when(riskRegisterService.getRiskRegisterById(99L))
            .thenThrow(new RiskRegisterNotFoundException("Risk register not found for id: 99"));

        mockMvc.perform(get("/risk-registers/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldCreateRiskRegisterWhenRequestIsValid() throws Exception {
        RiskRegisterRequest request = createRequest();
        when(riskRegisterService.createRiskRegister(any(RiskRegisterRequest.class))).thenReturn(createResponse());

        mockMvc.perform(post("/risk-registers/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.riskCode").value("RISK-201"));
    }

    @Test
    void shouldReturnBadRequestWhenPostBodyIsInvalid() throws Exception {
        RiskRegisterRequest request = createRequest();
        request.setOwnerEmail("wrong-email");

        mockMvc.perform(post("/risk-registers/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnConflictWhenRiskCodeAlreadyExists() throws Exception {
        when(riskRegisterService.createRiskRegister(any(RiskRegisterRequest.class)))
            .thenThrow(new DuplicateRiskCodeException("Risk code already exists: RISK-201"));

        mockMvc.perform(post("/risk-registers/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
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
