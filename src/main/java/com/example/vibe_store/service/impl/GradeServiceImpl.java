package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.grade.*;
import com.example.vibe_store.entity.Store;
import com.example.vibe_store.entity.employee.Employee;
import com.example.vibe_store.entity.employee.Position;
import com.example.vibe_store.entity.grade.Grade;
import com.example.vibe_store.entity.grade.GradeAssignment;
import com.example.vibe_store.entity.grade.GradeRule;
import com.example.vibe_store.entity.grade.GradedEmployee;
import com.example.vibe_store.enums.GradeType;
import com.example.vibe_store.enums.TargetType;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.*;
import com.example.vibe_store.service.GradeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.example.vibe_store.mapper.GradeMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final GradeRuleRepository gradeRuleRepository;
    private final PositionRepository positionRepository;
    private final GradeAssignmentRepository gradeAssignmentRepository;
    private final GradedEmployeeRepository gradedEmployeeRepository;
    private final GradeMapper gradeMapper;

    @Override
    @Transactional
    public GradeResponseDTO createGrade(CreateGradeRequestDTO requestDTO) {
        Grade newGrade = new Grade();

        newGrade.setGradeName(requestDTO.gradeName());
        newGrade.setGradeType(requestDTO.gradeType());
        newGrade.setIsActive(true);

        gradeRepository.save(newGrade);

        if (requestDTO.rules() != null && !requestDTO.rules().isEmpty()) {
            for (CreateGradeRuleRequestDTO ruleDto : requestDTO.rules()) {

               validateGradeRuleDependencies(requestDTO.gradeType(), ruleDto);

               buildAndSaveGradeRule(newGrade, ruleDto);
            }
        }
        log.info("Grade {} created successfully",  requestDTO.gradeName());
        return getGradeById(newGrade.getId());
    }

    @Override
    @Transactional
    public GradeRuleRespondDTO createGradeRule(Integer gradeId, CreateGradeRuleRequestDTO requestDTO) {

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + gradeId));

        validateGradeRuleDependencies(grade.getGradeType(), requestDTO);

        GradeRule newRule = buildAndSaveGradeRule(grade, requestDTO);

        log.info("GradeRule for Grade {} created successfully",  grade.getGradeName());
        return gradeMapper.toResponse(newRule);
    }

    @Override
    @Transactional
    public void assignGradeRule(AssignGradeRequestDTO requestDTO) {

        if (requestDTO.startDate() != null && requestDTO.endDate() != null
                && !requestDTO.startDate().isBefore(requestDTO.endDate())) {
            log.warn("Start date {} must be before end date {}", requestDTO.startDate(), requestDTO.endDate());
            throw new IllegalArgumentException("Start date must be before end date");
        }

        Grade grade = gradeRepository.findById(requestDTO.gradeId())
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found"));

        GradeAssignment newGradeAssignment = new GradeAssignment();
        newGradeAssignment.setGrade(grade);
        newGradeAssignment.setStartDate(requestDTO.startDate());
        newGradeAssignment.setEndDate(requestDTO.endDate());
        newGradeAssignment.setIsActive(true);

        if (requestDTO.storeId() != null) {
            Store store = storeRepository.findById(requestDTO.storeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

            newGradeAssignment.setStore(store);

            gradeAssignmentRepository.findByStoreIdAndIsActiveTrue(store.getId())
                    .ifPresent(oldAssignment -> {
                        oldAssignment.setIsActive(false);
                        oldAssignment.setEndDate(LocalDateTime.now());
                        gradeAssignmentRepository.save(oldAssignment);
                    });
        }

        GradeAssignment savedAssignment = gradeAssignmentRepository.save(newGradeAssignment);
        log.info("GradeAssignment for Grade {} created successfully", grade.getGradeName());

        if (requestDTO.employeeIds() != null && !requestDTO.employeeIds().isEmpty()) {
            for (Integer employeeId : requestDTO.employeeIds()) {
                Employee employee = employeeRepository.findById(employeeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

                GradedEmployee gradedEmployee = new GradedEmployee();
                gradedEmployee.setEmployee(employee);
                gradedEmployee.setGradeAssignment(savedAssignment);

                log.info("Employee {} added to GradeAssignment {}", employeeId, savedAssignment.getId());
                gradedEmployeeRepository.save(gradedEmployee);
            }
        }
    }

    @Override
    public GradeResponseDTO getGradeById(Integer id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with given id"));

        return mapGradeToResponse(grade);
    }

    @Override
    public Page<GradeResponseDTO> getAllGrades(Pageable pageable) {
        return gradeRepository.findAll(pageable)
                .map(this::mapGradeToResponse);
    }

    private GradeResponseDTO mapGradeToResponse(Grade grade) {
        List<GradeRule> rules = gradeRuleRepository.findAllByGradeIdWithPosition(grade.getId());
        return gradeMapper.toResponse(grade, rules);
    }

    @Override
    public GradeRuleRespondDTO getGradeRuleById(Integer id) {
        GradeRule gradeRule = gradeRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GradeRule not found with given id"));

        return gradeMapper.toResponse(gradeRule);
    }

    // ================= HELPER =================

    private GradeRule buildAndSaveGradeRule(Grade grade, CreateGradeRuleRequestDTO ruleDto){

        GradeRule newRule = new GradeRule();

        newRule.setGrade(grade);
        newRule.setTargetType(ruleDto.targetType());
        newRule.setFixedAmount(ruleDto.fixedAmount());
        newRule.setMinThreshold(ruleDto.minThreshold());
        newRule.setMaxThreshold(ruleDto.maxThreshold());
        newRule.setPercentage(ruleDto.percentage());
        newRule.setSharePercentage(ruleDto.sharePercentage());

        if (ruleDto.positionId() != null) {
            Position position = positionRepository.findById(ruleDto.positionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Position not found: " + ruleDto.positionId()));
            newRule.setPosition(position);
        }

        gradeRuleRepository.save(newRule);

        return newRule;
    }

    private void validateGradeRuleDependencies(GradeType gradeType, CreateGradeRuleRequestDTO ruleDto) {

        if (gradeType == GradeType.FIXED_GRADE && (ruleDto.fixedAmount() == null || ruleDto.fixedAmount().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("For FIXED_GRADE, 'fixedAmount' must be provided and greater than 0.");
        }

        if (gradeType == GradeType.PERCENT_GRADE || gradeType == GradeType.GRADE_THRESHOLD) {
            if (ruleDto.percentage() == null || ruleDto.percentage().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(gradeType + " requires a valid 'percentage' value.");
            }

            if (ruleDto.targetType() == TargetType.STORE_TOTAL_SALES &&
                    (ruleDto.sharePercentage() == null || ruleDto.sharePercentage().compareTo(BigDecimal.ZERO) <= 0)) {
                throw new IllegalArgumentException("When bonus is based on store total sales, 'sharePercentage' must be provided.");
            }
        }

        if (gradeType == GradeType.GRADE_THRESHOLD) {
            if (ruleDto.minThreshold() == null && ruleDto.maxThreshold() == null) {
                throw new IllegalArgumentException("GRADE_THRESHOLD requires at least one limit (min or max threshold).");
            }
        }

        // for all grade types: if both min and max are set, min must be less than max
        if (ruleDto.minThreshold() != null && ruleDto.maxThreshold() != null &&
                ruleDto.minThreshold().compareTo(ruleDto.maxThreshold()) >= 0) {
            throw new IllegalArgumentException("'minThreshold' must be less than 'maxThreshold'.");
        }
    }
}