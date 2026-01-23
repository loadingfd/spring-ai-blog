package com.ldfd.ragdoc.application.mapper;

import com.ldfd.ragdoc.adapter.controller.dto.MDocDTO;
import com.ldfd.ragdoc.application.vo.MDocVo;
import com.ldfd.ragdoc.domain.bo.MDoc;
import com.ldfd.ragdoc.infrastructure.mapper.po.MDocPo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MDocMapper {

    MDoc poToBo(MDocPo po);

    MDocPo boToPo(MDoc bo);

    MDocVo boToVo(MDoc bo);

    List<MDocVo> boListToVo(List<MDoc> boList);

    MDoc dtoToBo(MDocDTO request);
}
