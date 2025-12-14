package com.sb.movie.services;

import com.sb.movie.request.PasswordChangeRequest;
import com.sb.movie.request.UserRequest;
import com.sb.movie.response.UserProfileResponse;

public interface UserService {

    String addUser(UserRequest userRequest);

    UserProfileResponse getCurrentUserProfile(String userEmail);

    UserProfileResponse updateUserProfile(String userEmail, UserRequest userRequest);

    String changePassword(String userEmail, PasswordChangeRequest passwordChangeRequest);
}
