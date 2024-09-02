package com.example.projetomusica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity //Indica que a classe é uma entidade JPA e será mapeada para uma tabela no banco de dados.
@Getter
@Setter
@AllArgsConstructor //gera automaticamente um construtor com todos os campos da classe como argumentos.
@NoArgsConstructor //gera automaticamente um construtor padrão sem argumentos.
@Table(name = "Bandas")
public class Banda {

    @Id //marca o campo id como a chave primária da entidade.
    @GeneratedValue(strategy = GenerationType.IDENTITY) //especifica que o valor do campo id será gerado automaticamente pelo banco de dados usando uma estratégia de auto-incremento.
    private Long id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "resumo")
    private String resumo;

    @Column(name = "media")
    private Double media = 0.0;
}
