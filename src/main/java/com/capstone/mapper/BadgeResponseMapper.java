package com.capstone.mapper;

import com.capstone.dto.response.AtomNameDto;
import com.capstone.dto.response.LearnerBadgeResponseDto;
import com.capstone.model.CapsuleAtomMapping;
import com.capstone.model.IssuedBadge;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface BadgeResponseMapper {

    @Mapping(source = "issuedId", target = "badgeId")
    @Mapping(source = "issuer.name", target = "issuerName")
    @Mapping(source = "issuer.websiteUrl", target = "issuerWebsite")
    @Mapping(source = "userSnapshot.userId", target = "learnerId")
    @Mapping(source = "userSnapshot.firstName", target = "learnerFirstName")
    @Mapping(source = "userSnapshot.lastName", target = "learnerLastName")
    @Mapping(source = "userSnapshot.email", target = "learnerEmail")
    @Mapping(source = "capsuleSnapshot.capsuleAtomMappings", target = "atoms")
    LearnerBadgeResponseDto toDto(IssuedBadge badge);

    @Mapping(source = "skillAtom.name", target = "name")
    AtomNameDto toAtomDto(CapsuleAtomMapping mapping);

    List<LearnerBadgeResponseDto> toDtoList(List<IssuedBadge> badges);

    List<AtomNameDto> toAtomDtoList(List<CapsuleAtomMapping> mappings);
}
