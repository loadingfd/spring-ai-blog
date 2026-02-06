package com.ldfd.ragdoc.infrastructure.converter;

import com.ldfd.ragdoc.application.vo.VectorStoreVo;
import com.ldfd.ragdoc.domain.bo.VectorStore;
import com.ldfd.ragdoc.infrastructure.mapper.po.VectorStorePo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VectorStoreConverter {

    VectorStore poToBo(VectorStorePo po);

    VectorStorePo boToPo(VectorStore bo);

    VectorStoreVo boToVo(VectorStore bo);

    List<VectorStoreVo> boListToVo(List<VectorStore> boList);

    List<VectorStoreVo> poListToVo(List<VectorStorePo> poList);
}
