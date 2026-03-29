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
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final GradeRuleRepository gradeRuleRepository;
    private final PositionRepository positionRepository;
    private final GradeAssignmentRepository gradeAssignmentRepository;
    private final GradedEmployeeRepository gradedEmployeeRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public GradeResponseDTO createGrade(CreateGradeRequestDTO requestDTO) {
        Grade newGrade = new Grade();

        newGrade.setGradeName(requestDTO.getGradeName());
        newGrade.setGradeType(requestDTO.getGradeType());
        newGrade.setIsActive(true);

        gradeRepository.save(newGrade);

        if (requestDTO.getRules() != null && !requestDTO.getRules().isEmpty()) {
            for (CreateGradeRuleRequestDTO ruleDto : requestDTO.getRules()) {

                validateGradeRuleDependencies(requestDTO.getGradeType(), ruleDto);

               buildAndSaveGradeRule(newGrade, ruleDto);
            }
        }
        return getGradeById(newGrade.getId());
    }

    @Override
    @Transactional
    public GradeRuleRespondDTO createGradeRule(Integer gradeId, CreateGradeRuleRequestDTO requestDTO) {

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + gradeId));

        validateGradeRuleDependencies(grade.getGradeType(), requestDTO);

        GradeRule newRule = buildAndSaveGradeRule(grade, requestDTO);

        return getGradeRuleRespondDTO(newRule);
    }

    @Override
    @Transactional
    public void assignGradeRule(AssignGradeRequestDTO requestDTO) {

        if (requestDTO.getStartDate() != null && requestDTO.getEndDate() != null
                && !requestDTO.getStartDate().isBefore(requestDTO.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        Grade grade = gradeRepository.findById(requestDTO.getGradeId())
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found"));

        GradeAssignment newGradeAssignment = new GradeAssignment();
        newGradeAssignment.setGrade(grade);
        newGradeAssignment.setStartDate(requestDTO.getStartDate());
        newGradeAssignment.setEndDate(requestDTO.getEndDate());
        newGradeAssignment.setIsActive(true);

        if (requestDTO.getStoreId() != null) {
            Store store = storeRepository.findById(requestDTO.getStoreId())
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

        if (requestDTO.getEmployeeIds() != null && !requestDTO.getEmployeeIds().isEmpty()) {
            for (Integer employeeId : requestDTO.getEmployeeIds()) {
                Employee employee = employeeRepository.findById(employeeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

                GradedEmployee gradedEmployee = new GradedEmployee();
                gradedEmployee.setEmployee(employee);
                gradedEmployee.setGradeAssignment(savedAssignment);
                gradedEmployeeRepository.save(gradedEmployee);
            }
        }
    }

    @Override
    public GradeResponseDTO getGradeById(Integer id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with given id"));

        GradeResponseDTO responseDTO = modelMapper.map(grade, GradeResponseDTO.class);
        responseDTO.setGradeId(grade.getId());

        List<GradeRule> rules = gradeRuleRepository.findAllByGradeId(id);

        List<GradeRuleRespondDTO> ruleDtos = rules.stream()
                .map(this::getGradeRuleRespondDTO)
                .toList();

        responseDTO.setRules(ruleDtos);

        return responseDTO;
    }

    @Override
    public GradeRuleRespondDTO getGradeRuleById(Integer id) {
        GradeRule gradeRule = gradeRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GradeRule not found with given id"));

        return modelMapper.map(gradeRule, GradeRuleRespondDTO.class);
    }

    // ================= HELPER =================

    private GradeRule buildAndSaveGradeRule(Grade grade, CreateGradeRuleRequestDTO ruleDto){

        GradeRule newRule = new GradeRule();

        newRule.setGrade(grade);
        newRule.setTargetType(ruleDto.getTargetType());
        newRule.setFixedAmount(ruleDto.getFixedAmount());
        newRule.setMinThreshold(ruleDto.getMinThreshold());
        newRule.setMaxThreshold(ruleDto.getMaxThreshold());
        newRule.setPercentage(ruleDto.getPercentage());
        newRule.setSharePercentage(ruleDto.getSharePercentage());

        if (ruleDto.getPositionId() != null) {
            Position position = positionRepository.findById(ruleDto.getPositionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Position not found: " + ruleDto.getPositionId()));
            newRule.setPosition(position);
        }

        gradeRuleRepository.save(newRule);

        return newRule;
    }
    private GradeRuleRespondDTO getGradeRuleRespondDTO(GradeRule savedRule) {
        GradeRuleRespondDTO respondDTO = new GradeRuleRespondDTO();

        respondDTO.setGradeId(savedRule.getGrade().getId());
        respondDTO.setMinThreshold(savedRule.getMinThreshold());
        respondDTO.setMaxThreshold(savedRule.getMaxThreshold());
        respondDTO.setFixedAmount(savedRule.getFixedAmount());
        respondDTO.setPercentage(savedRule.getPercentage());
        respondDTO.setSharePercentage(savedRule.getSharePercentage());
        respondDTO.setTargetType(savedRule.getTargetType());

        respondDTO.setPositionName(savedRule.getPosition() != null
                ? savedRule.getPosition().getPositionName() : null);
        return respondDTO;
    }

    private void validateGradeRuleDependencies(GradeType gradeType, CreateGradeRuleRequestDTO ruleDto) {

        if (gradeType == GradeType.FIXED_GRADE && (ruleDto.getFixedAmount() == null || ruleDto.getFixedAmount().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("For FIXED_GRADE, 'fixedAmount' must be provided and greater than 0.");
        }

        if (gradeType == GradeType.PERCENT_GRADE || gradeType == GradeType.GRADE_THRESHOLD) {
            if (ruleDto.getPercentage() == null || ruleDto.getPercentage().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(gradeType + " requires a valid 'percentage' value.");
            }

            if (ruleDto.getTargetType() == TargetType.STORE_TOTAL_SALES &&
                    (ruleDto.getSharePercentage() == null || ruleDto.getSharePercentage().compareTo(BigDecimal.ZERO) <= 0)) {
                throw new IllegalArgumentException("When bonus is based on store total sales, 'sharePercentage' must be provided.");
            }
        }

        if (gradeType == GradeType.GRADE_THRESHOLD) {
            if (ruleDto.getMinThreshold() == null && ruleDto.getMaxThreshold() == null) {
                throw new IllegalArgumentException("GRADE_THRESHOLD requires at least one limit (min or max threshold).");
            }

            if (ruleDto.getMinThreshold() != null && ruleDto.getMaxThreshold() != null &&
                    ruleDto.getMinThreshold().compareTo(ruleDto.getMaxThreshold()) >= 0) {
                throw new IllegalArgumentException("'minThreshold' must be less than 'maxThreshold'.");
            }
        }
    }
}