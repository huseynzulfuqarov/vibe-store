package com.example.vibe_store.mapper;

import com.example.vibe_store.dto.grade.*;
import com.example.vibe_store.entity.grade.Grade;
import com.example.vibe_store.entity.grade.GradeRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface GradeMapper {

    @Mapping(source = "grade.id", target = "gradeId")
    @Mapping(source = "grade.isActive", target = "isActive")
    @Mapping(source = "grade.createdAt", target = "createdAt")
    @Mapping(source = "rules", target = "rules")
    GradeResponseDTO toResponse(Grade grade, List<GradeRule> rules);

    @Mapping(source = "grade.id", target = "gradeId")
    @Mapping(source = "position.positionName", target = "positionName")
    GradeRuleRespondDTO toResponse(GradeRule rule);

    List<GradeRuleRespondDTO> toResponseList(List<GradeRule> rules);
}