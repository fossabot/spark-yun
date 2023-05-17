package com.isxcode.star.backend.module.cluster.node.mapper;

import com.isxcode.star.api.pojos.engine.node.dto.ScpFileEngineNodeDto;
import com.isxcode.star.api.pojos.engine.node.req.EnoAddNodeReq;
import com.isxcode.star.api.pojos.engine.node.req.EnoUpdateNodeReq;
import com.isxcode.star.api.pojos.engine.node.res.EnoQueryNodeRes;
import com.isxcode.star.backend.module.cluster.node.entity.ClusterNodeEntity;

import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/** mapstruct映射. */
@Mapper(componentModel = "spring")
public interface ClusterNodeMapper {

  @Mapping(target = "usedMemory", expression = "java(0.0)")
  @Mapping(target = "allMemory", expression = "java(0.0)")
  @Mapping(target = "usedStorage", expression = "java(0.0)")
  @Mapping(target = "allStorage", expression = "java(0.0)")
  @Mapping(target = "cpuPercent", expression = "java(0.0)")
  @Mapping(target = "checkDateTime", expression = "java(java.time.LocalDateTime.now())")
  ClusterNodeEntity addNodeReqToNodeEntity(EnoAddNodeReq enoAddNodeReq);

  @Mapping(target = "usedMemory", expression = "java(0.0)")
  @Mapping(target = "allMemory", expression = "java(0.0)")
  @Mapping(target = "usedStorage", expression = "java(0.0)")
  @Mapping(target = "allStorage", expression = "java(0.0)")
  @Mapping(target = "cpuPercent", expression = "java(0.0)")
  @Mapping(target = "checkDateTime", expression = "java(java.time.LocalDateTime.now())")
  ClusterNodeEntity updateNodeReqToNodeEntity(EnoUpdateNodeReq enoUpdateNodeReq);

  @Mapping(
      target = "memory",
      expression = "java( nodeEntity.getUsedMemory()+ \"G/\" +nodeEntity.getAllMemory()+\"G\")")
  @Mapping(
      target = "storage",
      expression = "java( nodeEntity.getUsedStorage()+ \"G/\" +nodeEntity.getAllStorage()+\"G\")")
  @Mapping(target = "cpu", source = "cpuPercent")
  @Mapping(target = "checkDateTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
  EnoQueryNodeRes nodeEntityToQueryNodeRes(ClusterNodeEntity nodeEntity);

  default List<EnoQueryNodeRes> nodeEntityListToQueryNodeResList(
      List<ClusterNodeEntity> nodeEntities) {

    return nodeEntities.stream().map(this::nodeEntityToQueryNodeRes).collect(Collectors.toList());
  }

  default Page<EnoQueryNodeRes> datasourceEntityPageToQueryDatasourceResPage(
      Page<ClusterNodeEntity> engineNodeEntities) {
    List<EnoQueryNodeRes> dtoList =
        nodeEntityListToQueryNodeResList(engineNodeEntities.getContent());
    return new PageImpl<>(
        dtoList, engineNodeEntities.getPageable(), engineNodeEntities.getTotalElements());
  }

  ScpFileEngineNodeDto engineNodeEntityToScpFileEngineNodeDto(ClusterNodeEntity engineNode);
}
