package com.example.projetomusica.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record MusicaDTO(
        @NotEmpty(message = "{campo.nome.obrigatorio}") String nome,
        @NotEmpty(message = "{campo.resumo.obrigatorio}") String resumo,
        @NotEmpty(message = "{campo.duracao.obrigatorio}") String duracao) {
//        @NotNull Long albumId) {
}