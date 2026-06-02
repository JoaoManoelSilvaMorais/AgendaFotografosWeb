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
 * Controller responsável pela interface de cadastro, edição e listagem de equipamentos do estoque.
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
     * Capaz de distinguir entre a criação de um novo equipamento e a edição de um já existente.
     */
    public void salvar() {
        FacesContext context = FacesContext.getCurrentInstance();
        
        // Verifica se é um registro novo (ID nulo ou 0) ou uma edição
        boolean isNovo = (equipamento.getId() == null || equipamento.getId() == 0);
        boolean sucesso;

        if (isNovo) {
            // Lógica de INSERÇÃO
            sucesso = dao.salvar(equipamento);
            if (sucesso) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Sucesso", "Equipamento cadastrado com sucesso no estoque!"));
            }
        } else {
            // Lógica de ATUALIZAÇÃO
            sucesso = dao.atualizar(equipamento);
            if (sucesso) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Sucesso", "Equipamento atualizado com sucesso!"));
            }
        }

        if (sucesso) {
            // Limpa o formulário instanciando um objeto vazio
            cancelarEdicao();
            
            // Invalida a lista atual para forçar uma nova busca no banco e atualizar a tabela da tela
            this.listaEquipamentos = null; 
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Erro", "Ocorreu um problema ao salvar os dados do equipamento."));
        }
    }

    /**
     * Prepara o formulário para edição carregando os dados da linha selecionada na tabela.
     * @param equipamentoSelecionado O objeto correspondente à linha clicada.
     */
    public void prepararEdicao(Equipamento equipamentoSelecionado) {
        this.equipamento = equipamentoSelecionado;
    }

    /**
     * Limpa o formulário e cancela o processo de edição.
     */
    public void cancelarEdicao() {
        this.equipamento = new Equipamento();
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