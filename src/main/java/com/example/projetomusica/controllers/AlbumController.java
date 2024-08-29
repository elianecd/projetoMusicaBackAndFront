package com.example.projetomusica.controllers;

import com.example.projetomusica.dtos.AlbumDTO;
import com.example.projetomusica.models.Album;
import com.example.projetomusica.models.AvaliacaoAlbum;
import com.example.projetomusica.models.AvaliacaoRequest;
import com.example.projetomusica.models.Banda;
import com.example.projetomusica.repositories.AvaliacaoAlbumRepository;
import com.example.projetomusica.services.AlbumService;
import com.example.projetomusica.services.BandaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/album")
public class AlbumController {
    Locale enUS = new Locale("en", "US");

    @Autowired
    private AlbumService albumService;

    @Autowired
    private BandaService bandaService;

    @Autowired
    private AvaliacaoAlbumRepository avaliacaoAlbumRepository;

    @PostMapping("/novo-registro")
    public ResponseEntity<String> createAlbum(@Valid @RequestBody AlbumDTO albumDTO) {

        if (albumDTO.getNome() == null || albumDTO.getResumo() == null || albumDTO.getBandaId() == null) {
            return new ResponseEntity<>("Os campos nome, resumo e banda_id são obrigatórios", HttpStatus.BAD_REQUEST);
        }

        Album album = new Album();
        album.setNome(albumDTO.getNome());
        album.setResumo(albumDTO.getResumo());

        Banda banda = bandaService.findById(albumDTO.getBandaId());

        if (banda == null) {
            return new ResponseEntity<>("Banda não encontrada", HttpStatus.BAD_REQUEST);
        }
        album.setBanda(banda);

        albumService.createAlbum(album);
        return new ResponseEntity<>("Álbum " + album.getNome() + ", " + album.getResumo() + " da banda " + album.getBanda().getNome() + " criado com sucesso.", HttpStatus.CREATED);
    }

    @PostMapping("/{id}/avaliar-album")
    public ResponseEntity<String> avaliarAlbum(@PathVariable Long id, @RequestBody AvaliacaoRequest request) {

        Integer nota = request.getNota();

        if (nota < 0 || nota > 10) {
            return new ResponseEntity<>("Valor inválido", HttpStatus.BAD_REQUEST);
        }

        Album album = albumService.findById(id);

        if (album == null) {
            return new ResponseEntity<>("Álbum não encontrado", HttpStatus.BAD_REQUEST);
        }

        AvaliacaoAlbum avaliacao = new AvaliacaoAlbum();
        avaliacao.setAlbumId(id);
        avaliacao.setNota(nota);
        avaliacaoAlbumRepository.save(avaliacao);

        List<AvaliacaoAlbum> avaliacoes = avaliacaoAlbumRepository.findAllByAlbumId(id);
        albumService.updateMedia(avaliacoes);

        // Atualizar a média do álbum
        album.setMedia(albumService.getMedia());
        albumService.save(album);

        // Calcular a média das avaliações
        //double media = album.getMedia();

        String mediaFormatada = String.format(enUS,"%.2f", album.getMedia());

        return new ResponseEntity<>("Avaliação adicionada com sucesso. Média: " + mediaFormatada, HttpStatus.CREATED);
    }

}
