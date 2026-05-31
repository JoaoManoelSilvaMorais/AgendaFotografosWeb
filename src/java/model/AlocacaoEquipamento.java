package model;

public class AlocacaoEquipamento {
    private Integer id;
    private EscalaEvento escala; 
    private Equipamento equipamento;
    private Integer quantidade;

    public AlocacaoEquipamento() {
    }

    public AlocacaoEquipamento(Integer id, EscalaEvento escala, Equipamento equipamento, Integer quantidade) {
        this.id = id;
        this.escala = escala;
        this.equipamento = equipamento;
        this.quantidade = quantidade;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EscalaEvento getEscala() {
        return escala;
    }

    public void setEscala(EscalaEvento escala) {
        this.escala = escala;
    }

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public void setEquipamento(Equipamento equipamento) {
        this.equipamento = equipamento;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
    
    
}