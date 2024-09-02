package com.example.projetomusica.dtos;

import jakarta.validation.constraints.NotEmpty;

public record BandaDTO(
        @NotEmpty(message = "{campo.nome.obrigatorio}") String nome,
        @NotEmpty(message = "{campo.resumo.obrigatorio}") String resumo) {
}
