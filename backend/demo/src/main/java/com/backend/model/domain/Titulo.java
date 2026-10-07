package com.backend.model.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "titulos")
public class Titulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nome;
    private String nomeOriginal;
    private Integer ano;
    private String categoria;
    private String classe;
    private String diretor;
    private String nacionalidade;
    private String distribuidor;
    private String sinopse;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "titulo_atores", joinColumns = @JoinColumn(name = "titulo_id"))
    @jakarta.persistence.OrderColumn(name = "ordem")
    private List<String> atores = new ArrayList<>();

    public Titulo() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getNomeOriginal() { return nomeOriginal; }
    public void setNomeOriginal(String nomeOriginal) { this.nomeOriginal = nomeOriginal; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }
    public String getDiretor() { return diretor; }
    public void setDiretor(String diretor) { this.diretor = diretor; }
    public List<String> getAtores() { return atores; }
    public void setAtores(List<String> atores) { this.atores = atores == null ? new ArrayList<>() : new ArrayList<>(atores); }
    public String getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(String nacionalidade) { this.nacionalidade = nacionalidade; }
    public String getDistribuidor() { return distribuidor; }
    public void setDistribuidor(String distribuidor) { this.distribuidor = distribuidor; }
    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }
}
