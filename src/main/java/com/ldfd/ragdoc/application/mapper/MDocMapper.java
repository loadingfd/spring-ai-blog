package com.ldfd.ragdoc.application.mapper;

import com.ldfd.ragdoc.domain.bo.MDoc;
import com.ldfd.ragdoc.infrastructure.mapper.po.MDocPo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MDocMapper {

    MDoc poToBo(MDocPo po);

    MDocPo boToPo(MDoc bo);
}
