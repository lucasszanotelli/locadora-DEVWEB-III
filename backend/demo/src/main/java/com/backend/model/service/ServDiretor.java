package com.backend.model.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.backend.model.domain.Diretor;
import com.backend.model.repository.RepoDiretor;

@Service
public class ServDiretor {
    private final RepoDiretor repoDiretor;

    public ServDiretor(RepoDiretor repoDiretor) {
        this.repoDiretor = repoDiretor;
    }

    public List<Diretor> listarTodos() {
        return repoDiretor.findAll();
    }

    public Optional<Diretor> buscarPorId(Integer id) {
        return repoDiretor.findById(id);
    }

    public Diretor salvar(Diretor diretor) {
        validar(diretor);
        return repoDiretor.save(diretor);
    }

    public Optional<Diretor> atualizar(Integer id, Diretor diretorAtualizado) {
        return repoDiretor.findById(id).map(diretor -> {
            validar(diretorAtualizado);
            diretor.setNome(diretorAtualizado.getNome());
            diretor.setNacionalidade(diretorAtualizado.getNacionalidade());
            diretor.setDataNascimento(diretorAtualizado.getDataNascimento());
            return repoDiretor.save(diretor);
        });
    }

    private void validar(Diretor diretor) {
        if (diretor.getNome() == null || diretor.getNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome do diretor é obrigatório.");
        }
        diretor.setNome(diretor.getNome().trim());

        if (diretor.getDataNascimento() != null && !diretor.getDataNascimento().isBlank()) {
            try {
                LocalDate.parse(diretor.getDataNascimento());
            } catch (DateTimeParseException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data de nascimento inválida.");
            }
        }
    }

    public boolean excluir(Integer id) {
        if (!repoDiretor.existsById(id)) {
            return false;
        }

        repoDiretor.deleteById(id);
        return true;
    }
}
