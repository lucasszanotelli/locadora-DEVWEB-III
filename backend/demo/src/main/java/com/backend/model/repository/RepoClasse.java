package com.backend.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.backend.model.domain.Classe;

public interface RepoClasse extends JpaRepository<Classe, Integer> {
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Integer id);
}
