package com.backend.model.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name = "itens")
public class Item {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serial;

    private String dataAquisicao;
    private String tipoItem;
    private String numeroSerie;
    private String titulo;
    private String status = "Disponível";

    public Item(){
    }

    public Item( String dataAquisicao, String tipoItem){
        this.dataAquisicao = dataAquisicao;
        this.tipoItem = tipoItem;
    }

    public Item(Integer serial, String dataAquisicao, String tipoItem) {
        this(dataAquisicao, tipoItem);
        this.serial = serial;
    }

    public Item(Integer serial, String dataAquisicao, String tipoItem, String numeroSerie, String titulo, String status) {
        this(serial, dataAquisicao, tipoItem);
        this.numeroSerie = numeroSerie;
        this.titulo = titulo;
        this.status = status;
    }

    public Integer getSerial() {
        return serial;
    }

    public void setSerial(Integer serial) {
        this.serial = serial;
    }

    public String getDataAquisicao() {
        return dataAquisicao;
    }

    public void setDataAquisicao(String dataAquisicao) {
        this.dataAquisicao = dataAquisicao;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
