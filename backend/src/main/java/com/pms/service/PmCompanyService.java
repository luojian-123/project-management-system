package com.pms.service;

import com.pms.entity.PmCompany;
import com.pms.repository.PmCompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmCompanyService {

    private final PmCompanyRepository companyRepository;
    private final OrgBootstrapService orgBootstrapService;

    public PmCompanyService(PmCompanyRepository companyRepository, OrgBootstrapService orgBootstrapService) {
        this.companyRepository = companyRepository;
        this.orgBootstrapService = orgBootstrapService;
    }

    public List<PmCompany> list() {
        orgBootstrapService.ensureSystemCompanyExists();
        return companyRepository.selectList();
    }

    public PmCompany getById(Long id) {
        return companyRepository.selectById(id);
    }

    public void save(PmCompany company) {
        if (company.getId() == null) {
            companyRepository.insert(company);
            return;
        }
        PmCompany existing = companyRepository.selectById(company.getId());
        if (existing != null && Boolean.TRUE.equals(existing.getIsSystem())) {
            company.setCompanyCode(PmCompanyRepository.SYS_COMPANY_CODE);
            company.setIsSystem(true);
        }
        companyRepository.updateById(company);
    }

    public void deleteById(Long id) {
        PmCompany c = companyRepository.selectById(id);
        if (c != null && Boolean.TRUE.equals(c.getIsSystem())) {
            throw new IllegalStateException("系统公司不能删除");
        }
        companyRepository.deleteById(id);
    }
}

