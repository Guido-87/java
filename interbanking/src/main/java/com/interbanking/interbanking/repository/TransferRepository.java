package com.interbanking.interbanking.repository;

import com.interbanking.interbanking.model.Transfer;
import org.springframework.data.repository.CrudRepository;

public interface TransferRepository extends CrudRepository<Transfer, Integer> {
}
