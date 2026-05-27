package model;

public class Equipamento {
    private Integer id;
    private String descricao;
    private String tipo;
    private Integer quantidadeTotal;
    private Integer quantidadeEmUso;

    public Equipamento() {
    }

    public Equipamento(Integer id, String descricao, String tipo, Integer quantidadeTotal, Integer quantidadeEmUso) {
        this.id = id;
        this.descricao = descricao;
        this.tipo = tipo;
        this.quantidadeTotal = quantidadeTotal;
        this.quantidadeEmUso = quantidadeEmUso;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public void setQuantidadeTotal(Integer quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    public Integer getQuantidadeEmUso() {
        return quantidadeEmUso;
    }

    public void setQuantidadeEmUso(Integer quantidadeEmUso) {
        this.quantidadeEmUso = quantidadeEmUso;
    }
    
    
}