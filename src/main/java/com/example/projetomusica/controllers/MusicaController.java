package com.example.projetomusica.controllers;

import com.example.projetomusica.dtos.MusicaDTO;
import com.example.projetomusica.dtos.MusicaResponseDTO;
import com.example.projetomusica.exceptions.MusicaAlreadyExistsException;
import com.example.projetomusica.models.*;
import com.example.projetomusica.repositories.AvaliacaoMusicaRepository;
import com.example.projetomusica.repositories.MusicaRepository;
import com.example.projetomusica.services.AlbumService;
import com.example.projetomusica.services.BandaService;
import com.example.projetomusica.services.MusicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/musicas")
@RequiredArgsConstructor
//@CrossOrigin("http://localhost:4200") //configurado na webconfig
public class MusicaController {

    Locale enUS = new Locale("en", "US");

    @Autowired
    private AlbumService albumService;

    @Autowired
    private BandaService bandaService;

    @Autowired
    private MusicaService musicaService;

    @Autowired
    private MusicaRepository musicaRepository;

    @Autowired
    private AvaliacaoMusicaRepository avaliacaoMusicaRepository;

    @PostMapping("/novo-registro/{bandaId}/{albumId}")
    public ResponseEntity<?> createMusica(@PathVariable Long bandaId, @PathVariable Long albumId, @Valid @RequestBody MusicaDTO musicaDTO) {

        Musica musica = new Musica();
        musica.setNome(musicaDTO.nome());
        musica.setResumo(musicaDTO.resumo());
        musica.setDuracao(Integer.parseInt(musicaDTO.duracao()));

        try {
            Optional<Banda> bandaOptional = bandaService.findById(bandaId);
            if (bandaOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Banda não encontrada.");
            }

            Optional<Album> albumOptional = albumService.findById(albumId);
            if (albumOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Álbum não encontrado.");
            }

            Banda banda = bandaOptional.get();
            Album album = albumOptional.get();
            musica.setBanda(banda);
            musica.setAlbum(album);

            Musica savedMusica = musicaService.createMusica(musica);
            albumService.updateDuracaoTotal(album, String.valueOf(musica.getDuracao())); // Atualizar a duração total do álbum associado à música.

            return new ResponseEntity<>(savedMusica, HttpStatus.CREATED);
        } catch (MusicaAlreadyExistsException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (RuntimeException exception) {
            throw new RuntimeException("Não foi possível cadastrar a música.", exception);
        }
    }

    @PostMapping("/{id}/avaliar-musica")
    public ResponseEntity<String> avaliarMusica(@PathVariable Long id, @RequestBody AvaliacaoRequest avaliacaoRequest) {

        Integer nota = avaliacaoRequest.getNota();

        if (nota == null || nota < 0 || nota > 10) {
            return new ResponseEntity<>("Nota inválida, a nota deve ser um valor inteiro de 1 a 10.", HttpStatus.BAD_REQUEST);
        }

        try {
            Optional<Musica> musicaOptional = musicaService.findById(id);
            if (!musicaOptional.isPresent()) {
                return new ResponseEntity<>("Música não encontrada.", HttpStatus.NOT_FOUND);
            }

            Musica musica = musicaOptional.get();

            AvaliacaoMusica avaliacaoMusica = new AvaliacaoMusica();
            avaliacaoMusica.setMusicaId(id);
            avaliacaoMusica.setNota(nota);
            avaliacaoMusicaRepository.save(avaliacaoMusica);

            musicaService.updateMedia(avaliacaoMusicaRepository.findByMusicaId(id));

            // Atualizar a média da música
            musica.setMedia(musicaService.getMedia());
            musicaRepository.save(musica);

            String mediaFormatada = String.format(enUS, "%.2f", musica.getMedia());

            return new ResponseEntity<>("Música " + musica.getNome() + " avaliada com sucesso com nota " + nota + ". Média atual: " + mediaFormatada, HttpStatus.CREATED);

        } catch (RuntimeException exception) {
            throw new RuntimeException("Não foi possível avaliar a música.", exception);
        }
    }

    @GetMapping("/bandas/{bandaId}/album/{albumId}")
    public ResponseEntity<List<Musica>> getMusicasByBandaAndAlbum(@PathVariable Long bandaId, @PathVariable Long albumId) {
        System.out.println("Recebido bandaId: " + bandaId + ", albumId: " + albumId);
        List<Musica> musicas = musicaService.findByBandaIdAndAlbumId(bandaId, albumId);
        return new ResponseEntity<>(musicas, HttpStatus.OK);
    }

    @GetMapping("/bandas/{bandaId}")
    public ResponseEntity<List<Musica>> getMusicasByBanda(@PathVariable Long bandaId) {
        List<Musica> musicas = musicaService.findByBandaId(bandaId);
        return new ResponseEntity<>(musicas, HttpStatus.OK);
    }

    @GetMapping("/album/{albumId}")
    public ResponseEntity<List<Musica>> getMusicasByAlbum(@PathVariable Long albumId) {
        List<Musica> musicas = musicaService.findByAlbumId(albumId);
        return new ResponseEntity<>(musicas, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMusica(@PathVariable Long id, @Valid @RequestBody MusicaDTO musicaDTO) {
        try {
            Musica updatedMusica = musicaService.updateMusica(id, musicaDTO);
            return new ResponseEntity<>(updatedMusica, HttpStatus.OK);
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Não foi possível atualizar a música.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Musica> getMusicaById(@PathVariable Long id) {
        Optional<Musica> musicaOptional = musicaService.findById(id);
        if (musicaOptional.isPresent()) {
            return new ResponseEntity<>(musicaOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<Musica>> listarMusicas(
            @RequestParam(required = false) String banda,
            @RequestParam(required = false) String album) {

        List<Musica> musicasFiltradas = musicaService.listarMusicas(banda, album);
        return ResponseEntity.ok(musicasFiltradas);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMusica(@PathVariable Long id) {
        //System.out.println("ID da música recebida para deleção: " + id);
        musicaService.deleteMusica(id);
        return ResponseEntity.noContent().build();
    }
}