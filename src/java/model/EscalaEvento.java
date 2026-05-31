package model;

public class EscalaEvento {
    private Integer id;
    private Fotografo fotografo; 
    private Evento evento;

    public EscalaEvento() {
    }

    public EscalaEvento(Integer id, Fotografo fotografo, Evento evento) {
        this.id = id;
        this.fotografo = fotografo;
        this.evento = evento;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Fotografo getFotografo() {
        return fotografo;
    }

    public void setFotografo(Fotografo fotografo) {
        this.fotografo = fotografo;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }
    
    
}