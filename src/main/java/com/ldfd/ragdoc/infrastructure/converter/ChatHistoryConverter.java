package com.ldfd.ragdoc.infrastructure.converter;

import com.ldfd.ragdoc.adapter.controller.dto.ChatHistoryDTO;
import com.ldfd.ragdoc.application.vo.ChatHistoryVo;
import com.ldfd.ragdoc.domain.bo.ChatHistory;
import com.ldfd.ragdoc.infrastructure.mapper.po.ChatHistoryPo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatHistoryConverter {

    ChatHistory poToBo(ChatHistoryPo po);

    ChatHistoryPo boToPo(ChatHistory bo);

    ChatHistoryVo boToVo(ChatHistory bo);

    List<ChatHistoryVo> boListToVo(List<ChatHistory> boList);

    ChatHistory dtoToBo(ChatHistoryDTO request);
}
