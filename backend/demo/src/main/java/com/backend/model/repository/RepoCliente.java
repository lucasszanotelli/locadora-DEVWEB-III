package com.backend.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.model.domain.Cliente;

public interface RepoCliente extends JpaRepository<Cliente, Integer> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Integer id);

    long countBySocioIdAndAtivoTrueAndIdNot(Integer socioId, Integer id);

    long countBySocioIdAndAtivoTrue(Integer socioId);

    List<Cliente> findBySocioId(Integer socioId);
}
