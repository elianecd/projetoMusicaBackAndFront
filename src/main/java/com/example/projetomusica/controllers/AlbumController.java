package com.example.projetomusica.controllers;

import com.example.projetomusica.dtos.AlbumDTO;
import com.example.projetomusica.exceptions.AlbumAlreadyExistsException;
import com.example.projetomusica.models.Album;
import com.example.projetomusica.models.AvaliacaoAlbum;
import com.example.projetomusica.models.AvaliacaoRequest;
import com.example.projetomusica.models.Banda;
import com.example.projetomusica.repositories.AvaliacaoAlbumRepository;
import com.example.projetomusica.services.AlbumService;
import com.example.projetomusica.services.BandaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/album")
@RequiredArgsConstructor
//@CrossOrigin("http://localhost:4200") //configurado na webconfig
public class AlbumController {

    Locale enUS = new Locale("en", "US");

    @Autowired
    private AlbumService albumService;

    @Autowired
    private BandaService bandaService;

    @Autowired
    private AvaliacaoAlbumRepository avaliacaoAlbumRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Album> getAlbumById(@PathVariable Long id) {
        Optional<Album> albumOptional = albumService.findById(id);
        if (albumOptional.isPresent()) {
            return new ResponseEntity<>(albumOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/pesquisar-banda")
    public ResponseEntity<List<Banda>> pesquisarBanda(@RequestParam String nome) {
        List<Banda> bandas = bandaService.findByNome(nome);
        return new ResponseEntity<>(bandas, HttpStatus.OK);
    }

    @PostMapping("/novo-registro/{bandaId}")
    public ResponseEntity<Album> createAlbum(@PathVariable Long bandaId, @Valid @RequestBody AlbumDTO albumDTO) {

        Album album = new Album();
        album.setNome(albumDTO.nome());
        album.setResumo(albumDTO.resumo());

        try {
            Optional<Banda> bandaOptional = bandaService.findById(bandaId);

            if (bandaOptional.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Banda banda = bandaOptional.get();
            album.setBanda(banda);

            Album savedAlbum = albumService.createAlbum(album);
            return new ResponseEntity<>(savedAlbum, HttpStatus.CREATED);
        } catch (AlbumAlreadyExistsException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PostMapping("/{id}/avaliar-album")
    public ResponseEntity<String> avaliarAlbum(@PathVariable Long id, @RequestBody AvaliacaoRequest request) {

        Integer nota = request.getNota();

        if (nota < 0 || nota > 10) {
            return new ResponseEntity<>("Valor inválido.", HttpStatus.BAD_REQUEST);
        }

        Optional<Album> albumOptional = albumService.findById(id);

        if (albumOptional.isEmpty()) {
            return new ResponseEntity<>("Álbum não encontrado.", HttpStatus.BAD_REQUEST);
        }

        Album album = albumOptional.get();

        AvaliacaoAlbum avaliacao = new AvaliacaoAlbum();
        avaliacao.setAlbumId(id);
        avaliacao.setNota(nota);
        avaliacaoAlbumRepository.save(avaliacao);

        List<AvaliacaoAlbum> avaliacoes = avaliacaoAlbumRepository.findAllByAlbumId(id);
        albumService.updateMedia(avaliacoes);

        // Atualizar a média do álbum
        album.setMedia(albumService.getMedia());
        albumService.save(album);

        String mediaFormatada = String.format(enUS, "%.2f", album.getMedia());

        return new ResponseEntity<>("Avaliação adicionada com sucesso. Média: " + mediaFormatada, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public Album atualizarNomeOuResumo(@PathVariable Long id, @RequestBody Album album) {
        return albumService.atualizarNomeOuResumo(id, album);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarAlbum(@PathVariable Long id) {
        albumService.deletarAlbum(id);
    }
}