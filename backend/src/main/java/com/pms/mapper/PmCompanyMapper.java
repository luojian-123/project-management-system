package com.pms.mapper;

import com.pms.entity.PmCompany;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmCompanyMapper {
    List<PmCompany> selectList();
    PmCompany selectById(@Param("id") Long id);
    int insert(PmCompany company);
    int updateById(PmCompany company);
    int deleteById(@Param("id") Long id);
}

