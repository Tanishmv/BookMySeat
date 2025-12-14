package com.sb.movie.services;

import com.sb.movie.converter.UserConvertor;
import com.sb.movie.entities.User;
import com.sb.movie.exceptions.UserDoesNotExists;
import com.sb.movie.exceptions.UserExist;
import com.sb.movie.repositories.UserRepository;
import com.sb.movie.request.PasswordChangeRequest;
import com.sb.movie.request.UserRequest;
import com.sb.movie.response.UserProfileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

      
    @Override
    public String addUser(UserRequest userRequest) {
        Optional<User> users = userRepository.findByEmailId(userRequest.getEmailId());

        if (users.isPresent()) {
            throw new UserExist();
        }

        User user = UserConvertor.userDtoToUser(userRequest, passwordEncoder.encode(userRequest.getPassword()));

        userRepository.save(user);
        return "User Saved Successfully";
    }

    @Override
    public UserProfileResponse getCurrentUserProfile(String userEmail) {
        User user = userRepository.findByEmailId(userEmail)
                .orElseThrow(() -> new UserDoesNotExists());

        return convertToProfileResponse(user);
    }

    @Override
    public UserProfileResponse updateUserProfile(String userEmail, UserRequest userRequest) {
        User user = userRepository.findByEmailId(userEmail)
                .orElseThrow(() -> new UserDoesNotExists());

        if (userRequest.getName() != null) {
            user.setName(userRequest.getName());
        }
        if (userRequest.getAge() != null) {
            user.setAge(userRequest.getAge());
        }
        if (userRequest.getAddress() != null) {
            user.setAddress(userRequest.getAddress());
        }
        if (userRequest.getGender() != null) {
            user.setGender(userRequest.getGender());
        }
        if (userRequest.getMobileNo() != null) {
            user.setMobileNo(userRequest.getMobileNo());
        }

        userRepository.save(user);
        return convertToProfileResponse(user);
    }

    @Override
    public String changePassword(String userEmail, PasswordChangeRequest passwordChangeRequest) {
        User user = userRepository.findByEmailId(userEmail)
                .orElseThrow(() -> new UserDoesNotExists());

        if (!passwordEncoder.matches(passwordChangeRequest.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        userRepository.save(user);

        return "Password changed successfully";
    }

    private UserProfileResponse convertToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .age(user.getAge())
                .address(user.getAddress())
                .gender(user.getGender())
                .mobileNo(user.getMobileNo())
                .emailId(user.getEmailId())
                .roles(user.getRoles())
                .build();
    }

}