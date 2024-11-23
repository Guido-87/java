package com.interbanking.interbanking.controller;

import com.interbanking.interbanking.model.Company;
import com.interbanking.interbanking.model.Transfer;
import com.interbanking.interbanking.service.CompanyService;
import com.interbanking.interbanking.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CompanyController {
    @Autowired
    CompanyService companyService;

    @Autowired
    TransferService transferService;

    @GetMapping("/companies")
    private List<Company> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @GetMapping("/companies/new")
    private List<Company> getNewCompanies() {
        return companyService.getNewCompanies();
    }

    @GetMapping("/companies/transfers")
    private List<Company> getCompaniesWithTransfers () {
        return transferService.getCompaniesWithTransfers();
    }

    @PostMapping("/company")
    private int saveCompany(@RequestBody Company company) {
        companyService.saveCompany(company);
        return company.getId();
    }
}
