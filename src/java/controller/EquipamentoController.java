package controller;

import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import model.Equipamento;
import persistencia.EquipamentoDAO;

/**
 * Controller responsável pela interface de cadastro e listagem de equipamentos do estoque.
 */
@ManagedBean
@ViewScoped
public class EquipamentoController implements Serializable {

    private Equipamento equipamento;
    private List<Equipamento> listaEquipamentos;
    private EquipamentoDAO dao;

    /**
     * Construtor: Inicializa os objetos para evitar NullPointerException na tela (View).
     */
    public EquipamentoController() {
        this.dao = new EquipamentoDAO();
        this.equipamento = new Equipamento();
    }

    /**
     * Captura os dados da tela e envia para a camada de persistência.
     */
    public void salvar() {
        boolean sucesso = dao.salvar(equipamento);
        
        // Uso correto da API do JSF para captura do contexto atual
        FacesContext context = FacesContext.getCurrentInstance();
        
        if (sucesso) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Sucesso", "Equipamento cadastrado com sucesso no estoque!"));
            
            // Limpa o formulário instanciando um objeto vazio
            this.equipamento = new Equipamento();
            
            // Invalida a lista atual para forçar uma nova busca no banco e atualizar a tabela da tela
            this.listaEquipamentos = null; 
            
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Erro", "Ocorreu um problema ao cadastrar o equipamento."));
        }
    }
    
    /**
     * Acionado pelo botão de exclusão na tabela da interface de equipamentos.
     * @param eqp O objeto correspondente à linha clicada.
     */
    public void excluir(Equipamento eqp) {
        boolean sucesso = dao.excluir(eqp.getId());
        FacesContext context = FacesContext.getCurrentInstance();
        
        if (sucesso) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Sucesso", "Equipamento removido do inventário!"));
            
            // Invalida a lista para forçar uma nova busca e atualizar a tela em tempo real
            this.listaEquipamentos = null; 
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Erro ao excluir", "Não é possível remover este equipamento, pois ele possui histórico de uso em eventos."));
        }
    }

    // --- GETTERS E SETTERS (Obrigatórios para o JSF) ---

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public void setEquipamento(Equipamento equipamento) {
        this.equipamento = equipamento;
    }

    /**
     * Retorna o inventário completo da empresa.
     * Utiliza o padrão Lazy Loading para não sobrecarregar o banco de dados com chamadas repetidas.
     */
    public List<Equipamento> getListaEquipamentos() {
        if (listaEquipamentos == null) {
            listaEquipamentos = dao.listarTodos();
        }
        return listaEquipamentos;
    }
}