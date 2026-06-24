package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.AddressRequest;
import com.ecommerce.userservice.dto.RegisterRequest;
import com.ecommerce.userservice.entity.Address;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.entity.UserAddress;
import com.ecommerce.userservice.entity.UserAddressId;
import com.ecommerce.userservice.repository.AddressRepository;
import com.ecommerce.userservice.repository.UserAddressRepository;
import com.ecommerce.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService
{
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAddressRepository userAddressRepository;

    public UserService(UserRepository userRepository,
                       AddressRepository addressRepository,
                       UserAddressRepository userAddressRepository,
                       PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.userAddressRepository = userAddressRepository;
        this.passwordEncoder=passwordEncoder;
    }

    @Transactional
    public void register(RegisterRequest request)
    {
        if (userRepository.findByEmail(request.email()).isPresent())
        {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        user = userRepository.save(user);

        for (AddressRequest addressRequest : request.addresses())
        {
            Address address = new Address();

            address.setHouseNo(addressRequest.houseNo());
            address.setStreet(addressRequest.street());
            address.setCity(addressRequest.city());
            address.setState(addressRequest.state());
            address.setCountry(addressRequest.country());
            address.setPincode(addressRequest.pincode());

            address = addressRepository.save(address);

            UserAddressId userAddressId =
                    new UserAddressId(
                            user.getId(),
                            address.getAid()
                    );

            UserAddress userAddress = new UserAddress();
            userAddress.setId(userAddressId);

            userAddressRepository.save(userAddress);
        }
    }
}