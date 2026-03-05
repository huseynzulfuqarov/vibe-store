package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.grade.*;
import com.example.vibe_store.entity.employee.Position;
import com.example.vibe_store.entity.grade.Grade;
import com.example.vibe_store.entity.grade.GradeRule;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.EmployeeRepository;
import com.example.vibe_store.repository.GradeRepository;
import com.example.vibe_store.repository.PositionRepository;
import com.example.vibe_store.repository.StoreRepository;
import com.example.vibe_store.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final PositionRepository positionRepository;
    private final ModelMapper modelMapper;


    @Override
    public GradeRespondDTO createGrade(CreateGradeRequestDTO requestDTO) {
        Grade newGrade = new Grade();

        newGrade.setGradeName(requestDTO.getGradeName());
        newGrade.setGradeType(requestDTO.getGradeType());
        newGrade.setIsActive(true);
        return getGradeById(newGrade.getId());
    }

    @Override
    public GradeRuleRespondDTO createGradeRule(CreateGradeRuleRequestDto requestDTO) {
        GradeRule newGradeRule = new GradeRule();

        Position position = positionRepository.findById(requestDTO.getPositionId())
                .orElseThrow(() -> new ResourceNotFoundException("Given position not found"));

        newGradeRule.setPosition(position);
        newGradeRule.setGrade(requestDTO.);
        return null;
    }

    @Override
    public void assignGradeRule(AssignGradeRequestDto requestDTO) {

    }

    @Override
    public GradeRespondDTO getGradeById(Integer id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with given id"));
        return modelMapper.map(grade, GradeRespondDTO.class);
    }

    @Override
    public GradeRuleRespondDTO getGradeRuleById(Integer id) {
        return null;
    }
}
