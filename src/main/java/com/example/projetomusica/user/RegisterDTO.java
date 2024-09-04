package com.example.projetomusica.user;

//public record RegisterDTO(String username, String password) {
//}

import jakarta.validation.constraints.NotBlank;

public record RegisterDTO(
        @NotBlank String username,
        @NotBlank String password
) {}