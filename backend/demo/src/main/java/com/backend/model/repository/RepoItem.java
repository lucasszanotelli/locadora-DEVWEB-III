package com.backend.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.model.domain.Item;


public interface RepoItem extends JpaRepository<Item, Integer>{
} 