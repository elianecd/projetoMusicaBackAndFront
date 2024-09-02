package com.example.projetomusica.services;

import com.example.projetomusica.dtos.AlbumResponseDTO;
import com.example.projetomusica.dtos.MusicaResponseDTO;
import com.example.projetomusica.exceptions.AlbumAlreadyExistsException;
import com.example.projetomusica.models.Album;
import com.example.projetomusica.models.AvaliacaoAlbum;
import com.example.projetomusica.models.Banda;
import com.example.projetomusica.models.Musica;
import com.example.projetomusica.repositories.AlbumRepository;
import com.example.projetomusica.repositories.MusicaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Service
public class AlbumService {

    private double media;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private MusicaRepository musicaRepository;

    @Transactional
    public Album createAlbum(Album album) {
        Album existingAlbum = albumRepository.findByNomeAndBanda(album.getNome(), album.getBanda());
        if (existingAlbum != null) {
            throw new AlbumAlreadyExistsException("Um álbum com o nome " + album.getNome() + " já existe para esta banda.");
        }
        return albumRepository.save(album);
    }

    public Optional<Album> findById(Long albumId) {
        return albumRepository.findById(albumId);
    }

    public Album save(Album album) {
        return albumRepository.save(album);
    }

    public Page<Album> findAllByBanda(Banda banda, Pageable pageable) {
        return albumRepository.findAllByBanda(banda, pageable);
    }

    public void updateMedia(List<AvaliacaoAlbum> avaliacoes) {
        double soma = 0.0;
        for (AvaliacaoAlbum avaliacao : avaliacoes) {
            soma += avaliacao.getNota();
        }
        this.media = soma / avaliacoes.size();
    }

    public Album updateDuracaoTotal(Album album, String duracaoMusica) {
        int duracaoTotal = album.getDuracaoTotal();
        int duracaoMusicaInt = Integer.parseInt(duracaoMusica); // Converter String para int
        duracaoTotal += duracaoMusicaInt; // Somar a duração da música à duração total
        album.setDuracaoTotal(duracaoTotal);
        return albumRepository.save(album);
    }

    @Transactional
    public Album atualizarNomeOuResumo(Long id, Album album) {
        Album existingAlbum = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Album not found"));
        existingAlbum.setNome(album.getNome());
        existingAlbum.setResumo(album.getResumo());
        return albumRepository.save(existingAlbum);
    }

    @Transactional
    public void deletarAlbum(Long albumId) {
        Album album = albumRepository.findById(albumId).orElseThrow(() -> new EntityNotFoundException("Album not found"));
        musicaRepository.deleteByAlbumId(albumId); // Deletar músicas associadas
        albumRepository.delete(album); // Deletar álbum
    }
}
