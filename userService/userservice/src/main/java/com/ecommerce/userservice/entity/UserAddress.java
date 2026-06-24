package com.ecommerce.userservice.entity;


import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
@Entity
@Table(name = "user_addresses")
public class UserAddress {

    @EmbeddedId
    private UserAddressId id;
}
