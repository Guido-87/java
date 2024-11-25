package com.interbanking.interbanking.service;

import com.interbanking.interbanking.model.Company;
import com.interbanking.interbanking.model.Transfer;
import com.interbanking.interbanking.repository.TransferRepository;
import com.interbanking.interbanking.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransferService {
    @Autowired
    TransferRepository transferRepository;

    public List<Company> getCompaniesWithTransfers() {
        List<Company> companies = new ArrayList<Company>();
        transferRepository.findAll().forEach(t -> {
            if (t.getFecha().after(DateUtils.getDateAMonthBefore())) {
                companies.add(t.getEmpresa());
            }
        });
        return companies;
    }
}
