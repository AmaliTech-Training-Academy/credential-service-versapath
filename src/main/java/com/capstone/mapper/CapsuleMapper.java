package com.capstone.mapper;

import com.capstone.dto.response.AtomSummaryDto;
import com.capstone.dto.response.CapsuleResponseDto;
import com.capstone.model.CapsuleAtomMapping;
import com.capstone.model.CapsuleSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CapsuleMapper {

    @Mapping(target = "totalAtoms", expression = "java(entity.getCapsuleAtomMappings().size())")
    @Mapping(target = "atoms", ignore = true)
    CapsuleResponseDto toBasicResponseDto(CapsuleSnapshot entity);

    @Mapping(target = "totalAtoms", expression = "java(entity.getCapsuleAtomMappings().size())")
    @Mapping(target = "atoms", source = "capsuleAtomMappings")
    CapsuleResponseDto toResponseDtoWithAtoms(CapsuleSnapshot entity);

    @Mapping(source = "skillAtom.id", target = "id")
    @Mapping(source = "skillAtom.atomId", target = "atomId")
    @Mapping(source = "skillAtom.name", target = "atomName")
    @Mapping(source = "skillAtom.description", target = "description")
    AtomSummaryDto toSkillAtomSummaryDto(CapsuleAtomMapping mapping);

    List<AtomSummaryDto> toSkillAtomSummaryDtoList(List<CapsuleAtomMapping> mappings);
}
