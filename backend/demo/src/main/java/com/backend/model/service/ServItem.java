package com.backend.model.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.backend.model.domain.Item;
import com.backend.model.repository.RepoItem;
import com.backend.model.repository.RepoTitulo;

@Service
public class ServItem {
    private final RepoItem repoItem;
    private final RepoTitulo repoTitulo;

    public ServItem(RepoItem repoItem, RepoTitulo repoTitulo) {
        this.repoItem = repoItem;
        this.repoTitulo = repoTitulo;
    }

    public List<Item> listarTodos() {
        return repoItem.findAll();
    }

    public Optional<Item> buscarPorId(Integer serial) {
        return repoItem.findById(serial);
    }

    public Item salvar(Item item) {
        validar(item);
        item.setSerial(null);
        return repoItem.save(item);
    }

    public Optional<Item> atualizar(Integer serial, Item itemAtualizado) {
        return repoItem.findById(serial).map(item -> {
            validar(itemAtualizado);
            item.setDataAquisicao(itemAtualizado.getDataAquisicao());
            item.setTipoItem(itemAtualizado.getTipoItem());
            item.setNumeroSerie(itemAtualizado.getNumeroSerie());
            item.setTitulo(itemAtualizado.getTitulo());
            item.setStatus(itemAtualizado.getStatus());
            return repoItem.save(item);
        });
    }

    private void validar(Item item) {
        if (item.getDataAquisicao() == null || item.getDataAquisicao().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de aquisição é obrigatória.");
        }
        try {
            java.time.LocalDate.parse(item.getDataAquisicao());
        } catch (java.time.format.DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data de aquisição inválida. Use o formato AAAA-MM-DD.");
        }
        if (item.getTipoItem() == null || item.getTipoItem().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O tipo do item é obrigatório.");
        }
        item.setTipoItem(item.getTipoItem().trim());
        if (item.getTitulo() == null || item.getTitulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O título do item é obrigatório.");
        }
        if (item.getNumeroSerie() == null || item.getNumeroSerie().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O número de série é obrigatório.");
        }
        item.setNumeroSerie(item.getNumeroSerie().trim());
        item.setTitulo(item.getTitulo().trim());
        if (!repoTitulo.existsByNomeIgnoreCase(item.getTitulo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um título cadastrado.");
        }
        if (item.getStatus() == null || item.getStatus().isBlank()) item.setStatus("Disponível");
    }

    public boolean excluir(Integer serial) {
        if (!repoItem.existsById(serial)) {
            return false;
        }

        repoItem.deleteById(serial);
        return true;
    }
}
