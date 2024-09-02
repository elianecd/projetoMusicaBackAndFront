package com.example.projetomusica.services;

import com.example.projetomusica.exceptions.BandaAlreadyExistsException;
import com.example.projetomusica.models.AvaliacaoBanda;
import com.example.projetomusica.models.Banda;
import com.example.projetomusica.repositories.AvaliacaoBandaRepository;
import com.example.projetomusica.repositories.BandaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class BandaService { //é a classe de serviço que contém a lógica de negócios para criar uma banda.

    @Autowired //cria uma instância do BandaRepository e a fornece ao BandaService quando o BandaService é criado.
    private BandaRepository bandaRepository;

    @Autowired
    private AvaliacaoBandaRepository avaliacaoBandaRepository;

    public Banda createBanda(Banda banda) { //salva a banda no banco de dados.
        Banda existingBanda = bandaRepository.findByNome(banda.getNome());

        if (existingBanda != null) {
            throw new BandaAlreadyExistsException("A banda com o nome " + banda.getNome() + " já existe.");
        }
        return bandaRepository.save(banda);
    }

    public Optional<Banda> findById(Long idBanda) {
        return bandaRepository.findById(idBanda);
    }

    public void updateMedia(Banda banda) {
        List<AvaliacaoBanda> avaliacoes = avaliacaoBandaRepository.findAllByIdBanda(banda.getId());
        double soma = 0.0;
        for (AvaliacaoBanda avaliacao : avaliacoes) {
            soma += avaliacao.getNota();
        }
        double media = soma / avaliacoes.size();
        banda.setMedia(media);
        bandaRepository.save(banda);
    }

    public Page<Banda> findAll(Pageable pageable) {
        return bandaRepository.findAll(pageable);
    }


    public void deleteBanda(Long id) {
        Banda banda = bandaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Banda não encontrada"));
        bandaRepository.delete(banda);
    }

    public List<Banda> findByNome(String nome) {
        return bandaRepository.findByNomeContainingIgnoreCase(nome);
    }

    public void updateBanda(Banda banda) {
        bandaRepository.save(banda);
    }

    public List<Banda> findAll() {
        return bandaRepository.findAll();
    }
}

