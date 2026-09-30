package com.backend.model.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

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
        return repoAtor.save(ator);
    }

    public Optional<Ator> atualizar(Integer id, Ator atorAtualizado) {
        return repoAtor.findById(id).map(ator -> {
            ator.setNome(atorAtualizado.getNome());
            ator.setNacionalidade(atorAtualizado.getNacionalidade());
            ator.setDataNascimento(atorAtualizado.getDataNascimento());
            return repoAtor.save(ator);
        });
    }

    public boolean excluir(Integer id) {
        if (!repoAtor.existsById(id)) {
            return false;
        }

        repoAtor.deleteById(id);
        return true;
    }
}
