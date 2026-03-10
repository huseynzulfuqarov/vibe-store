package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.grade.*;
import com.example.vibe_store.entity.Store;
import com.example.vibe_store.entity.employee.Employee;
import com.example.vibe_store.entity.employee.Position;
import com.example.vibe_store.entity.grade.Grade;
import com.example.vibe_store.entity.grade.GradeAssignment;
import com.example.vibe_store.entity.grade.GradeRule;
import com.example.vibe_store.entity.grade.GradedEmployee;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.*;
import com.example.vibe_store.service.GradeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
    public GradeRespondDTO createGrade(CreateGradeRequestDTO requestDTO) {
        Grade newGrade = new Grade();

        newGrade.setGradeName(requestDTO.getGradeName());
        newGrade.setGradeType(requestDTO.getGradeType());
        newGrade.setIsActive(true);

        Grade savedGrade = gradeRepository.save(newGrade);

        if (requestDTO.getRules() != null && !requestDTO.getRules().isEmpty()) {
            for (CreateGradeRuleRequestDto ruleDto : requestDTO.getRules()) {
                GradeRule newRule = new GradeRule();
                newRule.setGrade(savedGrade);
                newRule.setTargetType(ruleDto.getTargetType());
                newRule.setFixedAmount(ruleDto.getFixedAmount());
                newRule.setMinThreshold(ruleDto.getMinThreshold());
                newRule.setMaxThreshold(ruleDto.getMaxThreshold());
                newRule.setPercentage(ruleDto.getPercentage());
                newRule.setSharePercentage(ruleDto.getSharePercentage());

                if (ruleDto.getPositionId() != null) {
                    Position position = positionRepository.findById(ruleDto.getPositionId())
                            .orElseThrow(() -> new ResourceNotFoundException("Vəzifə tapılmadı: " + ruleDto.getPositionId()));
                    newRule.setPosition(position);
                }
                gradeRuleRepository.save(newRule);
            }
        }
        return getGradeById(newGrade.getId());
    }

    @Override
    @Transactional
    public GradeRuleRespondDTO createGradeRule(Integer gradeId, CreateGradeRuleRequestDto requestDTO){

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade tapılmadı: " + gradeId));

        GradeRule newRule = new GradeRule();

        newRule.setGrade(grade);
        newRule.setTargetType(requestDTO.getTargetType());
        newRule.setFixedAmount(requestDTO.getFixedAmount());
        newRule.setMinThreshold(requestDTO.getMinThreshold());
        newRule.setMaxThreshold(requestDTO.getMaxThreshold());
        newRule.setPercentage(requestDTO.getPercentage());
        newRule.setSharePercentage(requestDTO.getSharePercentage());


        if (requestDTO.getPositionId() != null) {
            Position position = positionRepository.findById(requestDTO.getPositionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vəzifə tapılmadı: " + requestDTO.getPositionId()));
            newRule.setPosition(position);
        }
        GradeRule savedRule = gradeRuleRepository.save(newRule);

        return getGradeRuleRespondDTO(savedRule);
    }

    @Override
    @Transactional
    public void assignGradeRule(AssignGradeRequestDto requestDTO) {

        Grade grade = gradeRepository.findById(requestDTO.getGradeId())
                .orElseThrow(() -> new ResourceNotFoundException("grade tapilmadi"));

        GradeAssignment newGradeAssignment = new GradeAssignment();
        newGradeAssignment.setGrade(grade);
        newGradeAssignment.setStartDate(requestDTO.getStartDate());
        newGradeAssignment.setEndDate(requestDTO.getEndDate());
        newGradeAssignment.setIsActive(true);

        if(requestDTO.getStoreId() != null) {
            Store store = storeRepository.findById(requestDTO.getStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("store tapilmadi"));

            newGradeAssignment.setStore(store);

            gradeAssignmentRepository.findByStoreIdAndIsActiveTrue(store.getId())
                    .ifPresent(oldAssignment -> {
                        oldAssignment.setIsActive(false);
                        oldAssignment.setEndDate(LocalDateTime.now());
                        gradeAssignmentRepository.save(oldAssignment);
                    });
        }

        GradeAssignment savedAssignment = gradeAssignmentRepository.save(newGradeAssignment);


        if(requestDTO.getEmployeeIds() != null && !requestDTO.getEmployeeIds().isEmpty()) {
            for(Integer employeeId : requestDTO.getEmployeeIds()) {
                Employee employee = employeeRepository.findById(employeeId)
                        .orElseThrow(() -> new ResourceNotFoundException("employee tapilmadi"));

                GradedEmployee gradedEmployee = new GradedEmployee();
                gradedEmployee.setEmployee(employee);
                gradedEmployee.setGradeAssignment(savedAssignment);
                gradedEmployeeRepository.save(gradedEmployee);
            }
        }
    }

    @Override
    public GradeRespondDTO getGradeById(Integer id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with given id"));
        return modelMapper.map(grade, GradeRespondDTO.class);
    }

    @Override
    public GradeRuleRespondDTO getGradeRuleById(Integer id) {
        GradeRule gradeRule = gradeRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GradeRule not found with given id"));

        return modelMapper.map(gradeRule, GradeRuleRespondDTO.class);
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
}
