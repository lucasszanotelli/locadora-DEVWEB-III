package com.backend.model.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.backend.model.domain.Titulo;
import com.backend.model.repository.RepoTitulo;

@Service
public class ServTitulo {
    private final RepoTitulo repoTitulo;

    public ServTitulo(RepoTitulo repoTitulo) { this.repoTitulo = repoTitulo; }

    public List<Titulo> listarTodos() { return repoTitulo.findAll(); }
    public Optional<Titulo> buscarPorId(Integer id) { return repoTitulo.findById(id); }

    public Titulo salvar(Titulo titulo) {
        validar(titulo);
        titulo.setId(null);
        return repoTitulo.save(titulo);
    }

    public Optional<Titulo> atualizar(Integer id, Titulo atualizado) {
        return repoTitulo.findById(id).map(titulo -> {
            validar(atualizado);
            titulo.setNome(atualizado.getNome());
            titulo.setNomeOriginal(atualizado.getNomeOriginal());
            titulo.setAno(atualizado.getAno());
            titulo.setCategoria(atualizado.getCategoria());
            titulo.setClasse(atualizado.getClasse());
            titulo.setDiretor(atualizado.getDiretor());
            titulo.setAtores(atualizado.getAtores());
            titulo.setNacionalidade(atualizado.getNacionalidade());
            titulo.setDistribuidor(atualizado.getDistribuidor());
            titulo.setSinopse(atualizado.getSinopse());
            return repoTitulo.save(titulo);
        });
    }

    private void validar(Titulo titulo) {
        if (titulo.getNome() == null || titulo.getNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome do título é obrigatório.");
        }
        if (titulo.getCategoria() == null || titulo.getCategoria().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A categoria do título é obrigatória.");
        }
        if (titulo.getAno() != null && (titulo.getAno() < 1888 || titulo.getAno() > 9999)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ano do título é inválido.");
        }
        titulo.setNome(titulo.getNome().trim());
        if (titulo.getAtores() == null) titulo.setAtores(List.of());
    }

    public boolean excluir(Integer id) {
        if (!repoTitulo.existsById(id)) return false;
        repoTitulo.deleteById(id);
        return true;
    }
}
