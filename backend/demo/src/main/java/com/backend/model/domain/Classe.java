package com.backend.model.domain;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "classe")
public class Classe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nome;
    private BigDecimal valor;
    private Integer prazo;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id; 
    }
    public String getNome() { 
        return nome; 
    }
    public void setNome(String nome) { 
        this.nome = nome; 
    }
    public BigDecimal getValor() { 
        return valor; 

    }
    public void setValor(BigDecimal valor) { 
        this.valor = valor; 

    }
    public Integer getPrazo() { 
        return prazo; 
    }
    public void setPrazo(Integer prazo) { 
        this.prazo = prazo; 
    }
}
