package com.example.projetomusica.repositories;

import com.example.projetomusica.models.Album;
import com.example.projetomusica.models.Banda;
import com.example.projetomusica.models.Musica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusicaRepository extends JpaRepository<Musica, Long> {
    Musica findByNomeAndAlbumAndBanda(String nome, Album album, Banda banda);
    List<Musica> findByAlbumId(Long albumId);
    void deleteByAlbumId(Long albumId);
    List<Musica> findByBandaIdAndAlbumId(Long bandaId, Long albumId);
    List<Musica> findByBandaId(Long bandaId);
}
