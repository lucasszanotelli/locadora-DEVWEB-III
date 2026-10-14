package com.backend.model.service;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.backend.model.domain.Classe;
import com.backend.model.repository.RepoClasse;
import com.backend.model.repository.RepoTitulo;

@Service
public class ServClasse {
    private final RepoClasse classes;
    private final RepoTitulo titulos;

    public ServClasse(RepoClasse classes, RepoTitulo titulos) {
        this.classes = classes;
        this.titulos = titulos;
    }

    public List<Classe> listarTodos() { return classes.findAll(); }
    public Optional<Classe> buscarPorId(Integer id) { return classes.findById(id); }

    @Transactional
    public Classe salvar(Classe classe) {
        classe.setId(null);
        validar(classe, null);
        return classes.save(classe);
    }

    @Transactional
    public Optional<Classe> atualizar(Integer id, Classe dados) {
        return classes.findById(id).map(classe -> {
            validar(dados, id);
            if (!classe.getNome().equals(dados.getNome())) {
                titulos.findByClasse(classe.getNome()).forEach(titulo -> titulo.setClasse(dados.getNome()));
            }
            classe.setNome(dados.getNome());
            classe.setValor(dados.getValor());
            classe.setPrazo(dados.getPrazo());
            return classes.save(classe);
        });
    }

    private void validar(Classe classe, Integer id) {
        if (classe.getNome() == null || classe.getNome().isBlank()
                || classe.getValor() == null || classe.getValor().signum() <= 0
                || classe.getPrazo() == null || classe.getPrazo() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe nome, valor positivo e prazo de pelo menos um dia.");
        }
        classe.setNome(classe.getNome().trim());
        if (id == null ? classes.existsByNomeIgnoreCase(classe.getNome())
                : classes.existsByNomeIgnoreCaseAndIdNot(classe.getNome(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma classe com esse nome.");
        }
    }

    @Transactional
    public boolean excluir(Integer id) {
        var classe = classes.findById(id);
        if (classe.isEmpty()) return false;
        if (titulos.existsByClasse(classe.get().getNome())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Classe associada a títulos. A exclusão não é permitida.");
        }
        classes.delete(classe.get());
        return true;
    }
}
