package com.ecommerce.userservice.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.*;
// this is not a table it represents just the composite key for the table UserAddress
import java.io.Serializable;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UserAddressId implements Serializable {

    private Long userId;
    private Long addressId;
}

