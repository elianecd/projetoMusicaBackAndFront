package com.example.projetomusica.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Albuns")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne //muitos álbuns podem estar associados a uma única banda.
    @JoinColumn(name = "banda_id", nullable = false)
    private Banda banda;

    @Column(name = "nome")
    private String nome;

    @Column(name = "resumo")
    private String resumo;

    @Column(name = "duracao_total")
    private int duracaoTotal = 0;

    @Column(name = "media")
    private Double media = 0.0;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Musica> musicas;

    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", resumo='" + resumo + '\'' +
                ", duracaoTotal=" + duracaoTotal +
                ", media=" + media +
                ", banda=" + banda +
                ", musicas=" + musicas +
                '}';
    }
}
