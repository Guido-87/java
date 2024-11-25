package com.interbanking.interbanking.service;

import com.interbanking.interbanking.model.Company;
import com.interbanking.interbanking.repository.CompanyRepository;
import com.interbanking.interbanking.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompanyService {
    @Autowired
    CompanyRepository companyRepository;

    public List<Company> getAllCompanies() {
        List<Company> allCompanies = new ArrayList<Company>();
        companyRepository.findAll().forEach(allCompanies::add);
        return allCompanies;
    }

    public List<Company> getNewCompanies() {
        List<Company> newCompanies = new ArrayList<Company>();
        companyRepository.findAll().forEach(c -> {
            if (c.getFechaAdhesion().after(DateUtils.getDateAMonthBefore())) {
                newCompanies.add(c);
            }
        });
        return newCompanies;
    }

    public void saveCompany(Company company) {
        companyRepository.save(company);
    }
}
