package com.ecommerce.userservice.repository;

import com.ecommerce.userservice.entity.UserAddress;
import com.ecommerce.userservice.entity.UserAddressId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository
        extends JpaRepository<UserAddress, UserAddressId>
{
}
