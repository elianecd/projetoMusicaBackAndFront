package com.example.projetomusica.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Musicas")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Musica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne //muitas músicas podem estar associados a uma única banda.
    @JoinColumn(name = "banda_id", nullable = false)
    private Banda banda;

    @JsonIgnore
    @ManyToOne //muitas músicas podem estar associados a um unico album.
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(name = "nome")
    private String nome;

    @Column(name = "resumo")
    private String resumo;

    @Column(name = "media")
    private Double media = 0.0;

    @Column(name = "duracao")
    private int duracao = 0; //em segundos
}

