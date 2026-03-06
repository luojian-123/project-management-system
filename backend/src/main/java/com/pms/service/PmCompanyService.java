package com.pms.service;

import com.pms.entity.PmCompany;
import com.pms.mapper.PmCompanyMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmCompanyService {

    private final PmCompanyMapper companyMapper;

    public PmCompanyService(PmCompanyMapper companyMapper) {
        this.companyMapper = companyMapper;
    }

    public List<PmCompany> list() {
        return companyMapper.selectList();
    }

    public PmCompany getById(Long id) {
        return companyMapper.selectById(id);
    }

    public void save(PmCompany company) {
        if (company.getId() == null) companyMapper.insert(company);
        else companyMapper.updateById(company);
    }

    public void deleteById(Long id) {
        companyMapper.deleteById(id);
    }
}

