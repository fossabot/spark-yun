package com.isxcode.star.modules.datasource.repository;

import com.isxcode.star.modules.datasource.entity.DatasourceEntity;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@CacheConfig(cacheNames = {"SY_DATASOURCE"})
public interface DatasourceRepository extends JpaRepository<DatasourceEntity, String> {

	@Query("SELECT D FROM DatasourceEntity D " + "WHERE D.name LIKE %:keyword% " + "OR D.remark LIKE %:keyword% "
			+ "OR D.dbType LIKE %:keyword% " + "OR D.username LIKE %:keyword% "
			+ "OR D.jdbcUrl LIKE %:keyword% order by D.createDateTime desc ")
	Page<DatasourceEntity> searchAll(@Param("keyword") String searchKeyWord, Pageable pageable);

	List<DatasourceEntity> findAllByDriverId(String driverId);
}
