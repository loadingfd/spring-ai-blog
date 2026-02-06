package com.ldfd.ragdoc.infrastructure.converter;

import com.ldfd.ragdoc.adapter.controller.dto.UserDTO;
import com.ldfd.ragdoc.application.vo.UserVo;
import com.ldfd.ragdoc.domain.bo.User;
import com.ldfd.ragdoc.infrastructure.mapper.po.UserPo;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserConverter {

    User poToBo(UserPo po);

    UserPo boToPo(User bo);

    UserVo boToVo(User bo);

    List<UserVo> boListToVo(List<User> boList);

    User dtoToBo(UserDTO request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserDTO dto, @MappingTarget User user);
}
