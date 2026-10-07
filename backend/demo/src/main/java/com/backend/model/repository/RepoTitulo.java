package com.backend.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.backend.model.domain.Titulo;

public interface RepoTitulo extends JpaRepository<Titulo, Integer> {
    boolean existsByNomeIgnoreCase(String nome);
}
