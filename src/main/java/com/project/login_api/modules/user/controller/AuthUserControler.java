package com.project.login_api.modules.user.controller;

import com.project.login_api.modules.user.UserRepository;
import com.project.login_api.modules.user.dto.AuthRequestDTO;

import com.project.login_api.modules.user.dto.AuthResponseDTO;
import com.project.login_api.modules.user.dto.RegisterDTO;
import com.project.login_api.modules.user.dto.UserDTO;
import com.project.login_api.security.TokenService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.login_api.modules.user.User;

import java.util.UUID;

@RestController
@RequestMapping("/auth/user")
public class AuthUserControler {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthRequestDTO dto) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        User user = (User) auth.getPrincipal();
        String token = tokenService.generatedToken(user);

        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();

        return new ResponseEntity<>(new AuthResponseDTO(userDTO, token), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid RegisterDTO dto) {
        if (this.userRepository.findByEmail(dto.email()) != null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            String encryptedPassword = new BCryptPasswordEncoder().encode(dto.password());
            User user = User.builder()
                    .email(dto.email())
                    .name(dto.name())
                    .role(dto.role())
                    .password(encryptedPassword)
                    .build();

            this.userRepository.save(user);
            return new ResponseEntity<>(true, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/validate")
    public ResponseEntity validateToken(@RequestHeader("Authorization") String token) {
        String userId = tokenService.validateToken(token);
        System.out.println(userId);
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();

        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

}
