package com.example.projetomusica.repositories;

import com.example.projetomusica.models.Album;
import com.example.projetomusica.models.Banda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    Page<Album> findAllByBanda(Banda banda, Pageable pageable);
}
