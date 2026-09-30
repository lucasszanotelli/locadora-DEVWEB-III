package com.backend.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.model.domain.Ator;

@Repository
public interface RepoAtor extends JpaRepository<Ator, Integer> {
}
