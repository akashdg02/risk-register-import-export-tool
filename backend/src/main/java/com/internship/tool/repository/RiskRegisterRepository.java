package com.internship.tool.repository;

import com.internship.tool.entity.RiskRegister;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RiskRegisterRepository extends JpaRepository<RiskRegister, Long> {

    Optional<RiskRegister> findByRiskCode(String riskCode);

    List<RiskRegister> findByStatusIgnoreCase(String status);

    List<RiskRegister> findByCategoryIgnoreCaseAndActiveTrue(String category);

    List<RiskRegister> findByOwnerEmailIgnoreCase(String ownerEmail);

    List<RiskRegister> findByTargetResolutionDateBeforeAndActiveTrue(LocalDate targetResolutionDate);

    List<RiskRegister> findByTargetResolutionDateBetweenAndActiveTrue(LocalDate startDate, LocalDate endDate);

    List<RiskRegister> findByTargetResolutionDateAndActiveTrue(LocalDate targetResolutionDate);

    @Query("""
        select r
        from RiskRegister r
        where lower(r.title) like lower(concat('%', :keyword, '%'))
           or lower(r.description) like lower(concat('%', :keyword, '%'))
           or lower(r.riskCode) like lower(concat('%', :keyword, '%'))
        order by r.updatedAt desc
        """)
    List<RiskRegister> searchByKeyword(@Param("keyword") String keyword);

    @Query("""
        select count(r)
        from RiskRegister r
        where lower(r.status) = lower(:status)
        """)
    long countByStatusCustom(@Param("status") String status);
}
