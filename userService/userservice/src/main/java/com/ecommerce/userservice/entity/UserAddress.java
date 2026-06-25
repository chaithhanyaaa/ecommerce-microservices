package com.ecommerce.userservice.entity;


import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_addresses")
@Setter
@Getter
public class UserAddress {

    @EmbeddedId
    private UserAddressId id;
}
