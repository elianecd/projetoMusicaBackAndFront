package com.example.projetomusica.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AlbumDTO(
        @NotEmpty(message = "{campo.nome.obrigatorio}") String nome,
        @NotEmpty(message = "{campo.resumo.obrigatorio}") String resumo) {
//        @NotNull Long bandaId) {
}
