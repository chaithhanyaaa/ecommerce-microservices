package com.ecommerce.userservice.repository;

import com.ecommerce.userservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository
        extends JpaRepository<Address, Long>
{
}
