package com.example.projetomusica.services;

import com.example.projetomusica.dtos.MusicaDTO;
import com.example.projetomusica.exceptions.MusicaAlreadyExistsException;
import com.example.projetomusica.models.Album;
import com.example.projetomusica.models.AvaliacaoMusica;
import com.example.projetomusica.models.Banda;
import com.example.projetomusica.models.Musica;
import com.example.projetomusica.repositories.AlbumRepository;
import com.example.projetomusica.repositories.BandaRepository;
import com.example.projetomusica.repositories.MusicaRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Getter
@Service
public class MusicaService {
    private double media;

    @Autowired
    private MusicaRepository musicaRepository;

    @Autowired
    private BandaRepository bandaRepository;

    @Autowired
    private AlbumRepository albumRepository;

    public Musica createMusica(Musica musica){
        Musica existingMusica = musicaRepository.findByNomeAndAlbumAndBanda(musica.getNome(), musica.getAlbum(), musica.getBanda());
        if (existingMusica != null) {
            throw new MusicaAlreadyExistsException("A musica " + musica.getNome() + " já existe no album " + musica.getAlbum().getNome() + " da banda " + musica.getBanda().getNome() + ".");
        }
        return musicaRepository.save(musica);
    }

    public void updateMedia(List<AvaliacaoMusica> avaliacoes) {
        double soma = 0.0;
        for (AvaliacaoMusica avaliacao : avaliacoes) {
            soma += avaliacao.getNota();
        }
        this.media = soma / avaliacoes.size();
    }

    public List<Musica> findByBandaIdAndAlbumId(Long bandaId, Long albumId) {
        return musicaRepository.findByBandaIdAndAlbumId(bandaId, albumId);
    }

    public List<Musica> findByBandaId(Long bandaId) {
        return musicaRepository.findByBandaId(bandaId);
    }

    public List<Musica> findByAlbumId(Long albumId) {
        return musicaRepository.findByAlbumId(albumId);
    }

    public Optional<Musica> findById(Long id) {
        return musicaRepository.findById(id);
    }

    public Musica updateMusica(Long id, MusicaDTO musicaDTO) {
        Musica musica = musicaRepository.findById(id).orElseThrow(() -> new RuntimeException("Música não encontrada"));
        musica.setNome(musicaDTO.nome());
        musica.setResumo(musicaDTO.resumo());
        musica.setDuracao(Integer.parseInt(musicaDTO.duracao()));
        return musicaRepository.save(musica);
    }

    public void deleteMusica(Long id) {
        musicaRepository.deleteById(id);
    }

    public List<Musica> listarMusicas(String bandaNome, String albumNome) {
        Banda banda = (bandaNome != null) ? bandaRepository.findByNome(bandaNome) : null;
        Album album = (albumNome != null) ? albumRepository.findByNome(albumNome) : null;

        if (banda != null && album != null) {
            return musicaRepository.findByBandaIdAndAlbumId(banda.getId(), album.getId());
        } else if (banda != null) {
            return musicaRepository.findByBandaId(banda.getId());
        } else if (album != null) {
            return musicaRepository.findByAlbumId(album.getId());
        } else {
            return musicaRepository.findAll();
        }
    }
}