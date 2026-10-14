package com.backend.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.model.domain.Titulo;

public interface RepoTitulo extends JpaRepository<Titulo, Integer> {
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByClasse(String classe);
    java.util.List<Titulo> findByClasse(String classe);
}

// Esses métodos são consultas que o Spring Data JPA gera automaticamente 
// pelo nome. Você declara no repositório, e ele cria a implementação para 
// consultar o banco.
// - existsByNomeIgnoreCase(String nome): retorna true se existir um título 
// com esse nome, ignorando maiúsculas e minúsculas. Por exemplo, "Matrix" e 
// "matrix" são considerados iguais.
// - existsByClasse(String classe): retorna true se algum título estiver 
// associado à classe informada. Usamos para impedir a exclusão de uma classe
// que possui títulos.
// - findByClasse(String classe): retorna uma lista de todos os títulos 
// associados à classe. Usamos para atualizar os títulos quando o nome da 
// classe muda.