package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.AddressRequest;
import com.ecommerce.userservice.dto.LoginRequest;
import com.ecommerce.userservice.dto.LoginResponse;
import com.ecommerce.userservice.dto.RegisterRequest;
import com.ecommerce.userservice.entity.Address;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.entity.UserAddress;
import com.ecommerce.userservice.entity.UserAddressId;
import com.ecommerce.userservice.enums.Role;
import com.ecommerce.userservice.exception.EmailAlreadyExistsException;
import com.ecommerce.userservice.exception.InvalidCredentialsException;
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
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       AddressRepository addressRepository,
                       UserAddressRepository userAddressRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService)
    {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.userAddressRepository = userAddressRepository;
        this.passwordEncoder=passwordEncoder;
        this.jwtService=jwtService;
    }


    public LoginResponse login(LoginRequest request)
    {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password"));

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()))
        {
            throw new InvalidCredentialsException(
                    "Invalid email or password");
        }

        String token = jwtService.generateToken(user);



        return new LoginResponse(token,"Bearer");
    }



    @Transactional
    public void register(RegisterRequest request, Role role)
    {
        if (userRepository.findByEmail(request.email()).isPresent())
        {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(role);
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