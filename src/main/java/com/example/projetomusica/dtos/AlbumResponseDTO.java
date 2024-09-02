package com.example.projetomusica.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlbumResponseDTO {
    private Long id;
    private String nome;
    private List<MusicaResponseDTO> musicas;
    private String resumo;
    private Double media;
    private Integer duracaoTotal;
    private String mensagem;
}
