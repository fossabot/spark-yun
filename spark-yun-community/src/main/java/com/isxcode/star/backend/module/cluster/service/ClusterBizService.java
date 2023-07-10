package com.isxcode.star.backend.module.cluster.service;

import com.isxcode.star.api.constants.CalculateEngineStatus;
import com.isxcode.star.api.constants.EngineNodeStatus;
import com.isxcode.star.api.pojos.calculate.engine.req.CaeAddEngineReq;
import com.isxcode.star.api.pojos.calculate.engine.req.CaeQueryEngineReq;
import com.isxcode.star.api.pojos.calculate.engine.req.CaeUpdateEngineReq;
import com.isxcode.star.api.pojos.calculate.engine.res.CaeQueryEngineRes;
import com.isxcode.star.api.pojos.engine.node.dto.ScpFileEngineNodeDto;
import com.isxcode.star.api.properties.SparkYunProperties;
import com.isxcode.star.api.utils.AesUtils;
import com.isxcode.star.backend.module.cluster.entity.ClusterEntity;
import com.isxcode.star.backend.module.cluster.node.entity.ClusterNodeEntity;
import com.isxcode.star.backend.module.cluster.node.mapper.ClusterNodeMapper;
import com.isxcode.star.backend.module.cluster.node.repository.ClusterNodeRepository;
import com.isxcode.star.backend.module.cluster.mapper.ClusterMapper;
import com.isxcode.star.backend.module.cluster.node.service.RunAgentCheckService;
import com.isxcode.star.backend.module.cluster.repository.ClusterRepository;
import javax.transaction.Transactional;

import com.isxcode.star.api.exception.SparkYunException;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** 计算引擎模块. */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ClusterBizService {

  private final ClusterRepository engineRepository;

  private final ClusterNodeRepository engineNodeRepository;

  private final ClusterMapper engineMapper;

  private final RunAgentCheckService runAgentCheckService;

  private final ClusterNodeMapper clusterNodeMapper;

  private final SparkYunProperties sparkYunProperties;

  public void addEngine(CaeAddEngineReq caeAddEngineReq) {

    ClusterEntity engine = engineMapper.addEngineReqToEngineEntity(caeAddEngineReq);

    engine.setStatus(CalculateEngineStatus.NEW);

    engineRepository.save(engine);
  }

  public void updateEngine(CaeUpdateEngineReq caeUpdateEngineReq) {

    Optional<ClusterEntity> calculateEngineEntityOptional = engineRepository.findById(caeUpdateEngineReq.getCalculateEngineId());
    if (!calculateEngineEntityOptional.isPresent()) {
      throw new SparkYunException("计算引擎不存在");
    }

    ClusterEntity engine = engineMapper.updateEngineReqToEngineEntity(caeUpdateEngineReq,calculateEngineEntityOptional.get());

    engineRepository.save(engine);
  }

  public Page<CaeQueryEngineRes> queryEngines(CaeQueryEngineReq caeQueryEngineReq) {

    Page<ClusterEntity> engineEntities =
      engineRepository.searchAll(caeQueryEngineReq.getSearchKeyWord(),
        PageRequest.of(caeQueryEngineReq.getPage(), caeQueryEngineReq.getPageSize()));

    return engineMapper.engineEntityPageToCaeQueryEngineResPage(engineEntities);
  }

  public void delEngine(String engineId) {

    engineRepository.deleteById(engineId);
  }

  public void checkEngine(String engineId) {

    Optional<ClusterEntity> calculateEngineEntityOptional = engineRepository.findById(engineId);
    if (!calculateEngineEntityOptional.isPresent()) {
      throw new SparkYunException("计算引擎不存在");
    }
    ClusterEntity calculateEngineEntity = calculateEngineEntityOptional.get();

    List<ClusterNodeEntity> engineNodes = engineNodeRepository.findAllByClusterId(engineId);

    // 同步检测按钮
    engineNodes.forEach(e -> {

      ScpFileEngineNodeDto scpFileEngineNodeDto = clusterNodeMapper.engineNodeEntityToScpFileEngineNodeDto(e);
      scpFileEngineNodeDto.setPasswd(AesUtils.decrypt(sparkYunProperties.getAesSlat(), scpFileEngineNodeDto.getPasswd()));

      try {
        runAgentCheckService.checkAgent(scpFileEngineNodeDto, e);
      } catch (JSchException | IOException | InterruptedException | SftpException ex) {
        log.error(ex.getMessage());
        e.setCheckDateTime(LocalDateTime.now());
        e.setAgentLog(ex.getMessage());
        e.setStatus(EngineNodeStatus.CHECK_ERROR);
        engineNodeRepository.saveAndFlush(e);
      }
    });

    // 激活节点
    List<ClusterNodeEntity> activeNodes = engineNodes.stream().filter(e -> EngineNodeStatus.RUNNING.equals(e.getStatus())).collect(Collectors.toList());
    calculateEngineEntity.setActiveNodeNum(activeNodes.size());
    calculateEngineEntity.setAllNodeNum(engineNodes.size());

    // 内存
    double allMemory = activeNodes.stream().mapToDouble(ClusterNodeEntity::getAllMemory).sum();
    calculateEngineEntity.setAllMemoryNum(allMemory);
    double usedMemory = activeNodes.stream().mapToDouble(ClusterNodeEntity::getUsedMemory).sum();
    calculateEngineEntity.setUsedMemoryNum(usedMemory);

    // 存储
    double allStorage = activeNodes.stream().mapToDouble(ClusterNodeEntity::getAllStorage).sum();
    calculateEngineEntity.setAllStorageNum(allStorage);
    double usedStorage = activeNodes.stream().mapToDouble(ClusterNodeEntity::getUsedStorage).sum();
    calculateEngineEntity.setUsedStorageNum(usedStorage);

    if (!activeNodes.isEmpty()) {
      calculateEngineEntity.setStatus(CalculateEngineStatus.ACTIVE);
    } else {
      calculateEngineEntity.setStatus(CalculateEngineStatus.NO_ACTIVE);
    }

    calculateEngineEntity.setCheckDateTime(LocalDateTime.now());
    engineRepository.saveAndFlush(calculateEngineEntity);
  }
}
