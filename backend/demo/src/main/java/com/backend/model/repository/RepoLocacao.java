package com.backend.model.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.backend.model.domain.Locacao;

public interface RepoLocacao extends JpaRepository<Locacao, Integer> {
    List<Locacao> findAllByOrderByIdDesc();
    List<Locacao> findBySerieIgnoreCaseAndDevolucaoIsNull(String serie);
    boolean existsByItemIdAndDevolucaoIsNullAndIdNot(Integer itemId, Integer id);
    boolean existsByItemId(Integer itemId);
    boolean existsByClienteId(Integer clienteId);
}
