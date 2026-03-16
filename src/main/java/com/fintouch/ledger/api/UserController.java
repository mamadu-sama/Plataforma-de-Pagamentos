package com.fintouch.ledger.api;

import com.fintouch.ledger.api.dto.CreateUserRequest;
import com.fintouch.ledger.api.dto.CreateUserResponse;
import com.fintouch.ledger.domain.User;
import com.fintouch.ledger.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse create(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request.fullName(), request.email(), request.document(), request.type());
        return new CreateUserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getDocument(), user.getType());
    }
}

