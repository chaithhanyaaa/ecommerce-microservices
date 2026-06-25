package com.ecommerce.userservice.entity;

import com.ecommerce.userservice.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Setter
@Getter
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String name;


    @Enumerated(EnumType.STRING)
    private Role role;
}
