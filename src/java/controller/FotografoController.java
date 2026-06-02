package controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext; 

import model.Fotografo;
import persistencia.FotografoDAO;

@ManagedBean
@ViewScoped
public class FotografoController implements Serializable {

    private Fotografo fotografo;
    private List<Fotografo> listaFotografos;
    private FotografoDAO dao;

    public FotografoController() {
        this.dao = new FotografoDAO();
        this.fotografo = new Fotografo(); 
        this.fotografo.setDataContratacao(LocalDate.now()); 
    }

    public void salvar() {
        boolean sucesso;
        
        // Verifica se é uma inserção (ID nulo) ou uma atualização (ID existente)
        if (this.fotografo.getId() == null || this.fotografo.getId() == 0) {
            sucesso = dao.salvar(fotografo);
        } else {
            sucesso = dao.atualizar(fotografo);
        }
        
        FacesContext context = FacesContext.getCurrentInstance();
        
        if (sucesso) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Sucesso", "Dados do fotógrafo gravados com sucesso!"));
            
            // Limpa o formulário
            this.fotografo = new Fotografo();
            this.fotografo.setDataContratacao(LocalDate.now());
            
            // Força a recarga da lista
            this.listaFotografos = null; 
            
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Erro", "Ocorreu um problema ao salvar. Verifique os dados fornecidos."));
        }
    }
    
    // NOVO MÉTODO: Carrega os dados da linha clicada para o formulário
    public void prepararEdicao(Fotografo f) {
        // Criamos uma cópia para que alterações não salvas não reflitam na tabela instantaneamente
        this.fotografo = new Fotografo(
            f.getId(), 
            f.getNomeCompleto(), 
            f.getCpf(), 
            f.getTelefone(), 
            f.getDataContratacao(), 
            f.getAtivo()
        );
    }
    
    public void excluir(Fotografo f) {
        boolean sucesso = dao.excluir(f.getId());
        FacesContext context = FacesContext.getCurrentInstance();
        
        if (sucesso) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Sucesso", "Fotógrafo removido com sucesso!"));
            this.listaFotografos = null; 
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Erro ao excluir", "Não é possível remover este fotógrafo pois ele está vinculado a um evento."));
        }
    }

    public Fotografo getFotografo() {
        return fotografo;
    }

    public void setFotografo(Fotografo fotografo) {
        this.fotografo = fotografo;
    }

    public List<Fotografo> getListaFotografos() {
        if (listaFotografos == null) {
            listaFotografos = dao.listarTodos();
        }
        return listaFotografos;
    }
}