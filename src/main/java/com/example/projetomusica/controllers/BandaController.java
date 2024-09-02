package com.example.projetomusica.controllers;

import com.example.projetomusica.dtos.AlbumResponseDTO;
import com.example.projetomusica.dtos.BandaDTO;
import com.example.projetomusica.dtos.BandaResponseDTO;
import com.example.projetomusica.dtos.MusicaResponseDTO;
import com.example.projetomusica.exceptions.BandaAlreadyExistsException;
import com.example.projetomusica.models.Album;
import com.example.projetomusica.models.AvaliacaoBanda;
import com.example.projetomusica.models.AvaliacaoRequest;
import com.example.projetomusica.models.Banda;
import com.example.projetomusica.repositories.AvaliacaoBandaRepository;
import com.example.projetomusica.services.AlbumService;
import com.example.projetomusica.services.BandaService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bandas")
//@CrossOrigin("http://localhost:4200") //está configurado na WebConfig
public class BandaController {

    Locale enUS = new Locale("en", "US");

    @Autowired
    private BandaService bandaService;

    @Autowired
    private AvaliacaoBandaRepository avaliacaoBandaRepository;

    @Autowired
    private AlbumService albumService;

    @PostMapping("/novo-registro")
    public ResponseEntity<Banda> createBanda(@Valid @RequestBody BandaDTO bandaDTO) {

        Banda banda = new Banda();
        banda.setNome(bandaDTO.nome());
        banda.setResumo(bandaDTO.resumo());

        try {
            Banda savedBanda = bandaService.createBanda(banda);
            return new ResponseEntity<>(savedBanda, HttpStatus.CREATED);
        } catch (BandaAlreadyExistsException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PostMapping("/{id}/avaliar-banda")
    public ResponseEntity<String> avaliarBanda(@PathVariable(value = "id") Long idBanda, @RequestBody @Valid AvaliacaoRequest avaliacaoRequest) {

        Integer nota = avaliacaoRequest.getNota();

        if (nota == null || nota < 0 || nota > 10) {
            return new ResponseEntity<>("Valor inválido.", HttpStatus.BAD_REQUEST);
        }

        try {
            Optional<Banda> bandaOptional = bandaService.findById(idBanda);
            if (!bandaOptional.isPresent()) {
                return new ResponseEntity<>("Banda não encontrada.", HttpStatus.BAD_REQUEST);
            }

            Banda banda = bandaOptional.get();

            AvaliacaoBanda avaliacaoBanda = new AvaliacaoBanda();
            avaliacaoBanda.setIdBanda(idBanda);
            avaliacaoBanda.setNota(nota);
            avaliacaoBandaRepository.save(avaliacaoBanda);

            bandaService.updateMedia(banda);

            String mediaFormatada = String.format(enUS, "%.2f", banda.getMedia());

            return new ResponseEntity<>("Banda " + banda.getNome() + " avaliada com sucesso com nota " + nota + ". Média atual: " + mediaFormatada, HttpStatus.CREATED);
        } catch (RuntimeException exception) {
            throw new RuntimeException("Não foi possível avaliar a banda.", exception);
        }
    }

    @GetMapping
    public ResponseEntity<?> listarBandas(Pageable pageable) {

        Page<Banda> bandas = bandaService.findAll(pageable);

        if (bandas.isEmpty()) {
            return new ResponseEntity<>("Nenhuma banda registrada.", HttpStatus.OK);
        }

        List<BandaResponseDTO> response = bandas.stream()
                .map(banda -> new BandaResponseDTO(banda.getId(), banda.getNome(), banda.getResumo(), banda.getMedia()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Transactional
    @GetMapping("/{id}/albuns")
    public ResponseEntity<?> listarAlbunsDaBanda(@PathVariable Long id, Pageable pageable) {

        Optional<Banda> bandaOptional = bandaService.findById(id);

        if (!bandaOptional.isPresent()) {
            return new ResponseEntity<>("Nenhuma banda registrada.", HttpStatus.OK);
        }

        Banda banda = bandaOptional.get();
        Pageable pageableWithTenItems = PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());

        Page<Album> albunsPage = albumService.findAllByBanda(banda, pageableWithTenItems);

        if (albunsPage.isEmpty()) {
            return new ResponseEntity<>("Nenhum álbum registrado", HttpStatus.OK);
        }

        // Convert the Page<Album> to List<AlbumResponseDTO>
        List<AlbumResponseDTO> albuns = albunsPage.getContent().stream()
                .map(album -> {
                    AlbumResponseDTO albumDTO = new AlbumResponseDTO();
                    albumDTO.setId(album.getId());
                    albumDTO.setNome(album.getNome());
                    albumDTO.setMedia(album.getMedia());
                    albumDTO.setDuracaoTotal(album.getDuracaoTotal());
                    albumDTO.setResumo(album.getResumo()); // Populando o campo resumo
                    if (album.getMusicas().isEmpty()) {
                        albumDTO.setMensagem("Não há musicas cadastradas");
                    } else {
                        albumDTO.setMusicas(album.getMusicas().stream().map(musica -> {
                            MusicaResponseDTO musicaDTO = new MusicaResponseDTO();
                            musicaDTO.setId(musica.getId());
                            musicaDTO.setNome(musica.getNome());
                            musicaDTO.setResumo(musica.getResumo());
                            musicaDTO.setMedia(musica.getMedia());
                            musicaDTO.setDuracao(musica.getDuracao());
                            return musicaDTO;
                        }).collect(Collectors.toList()));
                    }
                    return albumDTO;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(albuns, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBandaById(@PathVariable Long id) {
        Optional<Banda> banda = bandaService.findById(id);
        if (banda.isPresent()) {
            BandaResponseDTO response = new BandaResponseDTO(banda.get().getId(), banda.get().getNome(), banda.get().getResumo(), banda.get().getMedia());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Banda não encontrada.", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateBanda(@PathVariable Long id, @Valid @RequestBody Banda banda) {
        Optional<Banda> bandaOptional = bandaService.findById(id);
        if (bandaOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Banda existingBanda = bandaOptional.get();
        existingBanda.setNome(banda.getNome());
        existingBanda.setResumo(banda.getResumo());

        bandaService.updateBanda(existingBanda);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        bandaService.deleteBanda(id);
    }

    @GetMapping("/pesquisar-banda")
    public ResponseEntity<List<Banda>> getAllBandas() {
        List<Banda> bandas = bandaService.findAll();
        return ResponseEntity.ok(bandas);
    }
}