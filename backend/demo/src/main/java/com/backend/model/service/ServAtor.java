package com.backend.model.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.backend.model.domain.Ator;
import com.backend.model.repository.RepoAtor;

@Service
public class ServAtor {

    private final RepoAtor repoAtor;

    public ServAtor(RepoAtor repoAtor) {
        this.repoAtor = repoAtor;
    }

    public List<Ator> listarTodos() {
        return repoAtor.findAll();
    }

    public Optional<Ator> buscarPorId(Integer id) {
        return repoAtor.findById(id);
    }

    public Ator salvar(Ator ator) {
        validar(ator);
        return repoAtor.save(ator);
    }

    public Optional<Ator> atualizar(Integer id, Ator atorAtualizado) {
        return repoAtor.findById(id).map(ator -> {
            validar(atorAtualizado);
            ator.setNome(atorAtualizado.getNome());
            ator.setNacionalidade(atorAtualizado.getNacionalidade());
            ator.setDataNascimento(atorAtualizado.getDataNascimento());
            return repoAtor.save(ator);
        });
    }

    private void validar(Ator ator) {
        if (ator.getNome() == null || ator.getNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome do ator é obrigatório.");
        }
        ator.setNome(ator.getNome().trim());
        if (ator.getDataNascimento() != null && !ator.getDataNascimento().isBlank()) {
            try {
                java.time.LocalDate.parse(ator.getDataNascimento());
            } catch (java.time.format.DateTimeParseException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data de nascimento inválida.");
            }
        }
    }

    public boolean excluir(Integer id) {
        if (!repoAtor.existsById(id)) {
            return false;
        }

        repoAtor.deleteById(id);
        return true;
    }
}
