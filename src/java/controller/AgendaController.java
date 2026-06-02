package controller;

import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import model.AlocacaoEquipamento;
import model.Equipamento;
import model.EscalaEvento;
import model.Evento;
import model.Fotografo;
import negocio.AgendaNegocio;
import persistencia.EquipamentoDAO;
import persistencia.EventoDAO;
import persistencia.FotografoDAO;

/**
 * Controller responsável pela tela de Agendamento.
 * Orquestra a criação do Evento, a escala do Fotógrafo e a alocação de Equipamentos,
 * além de gerenciar a edição e atualização das informações.
 */
@ManagedBean
@ViewScoped
public class AgendaController implements Serializable {

    private Evento evento;
    private Integer idFotografoSelecionado;
    private Integer idEquipamentoSelecionado;
    private Integer quantidadeRetirada;

    // Dependências para popular as caixas de seleção (Combobox) da tela
    private List<Fotografo> listaFotografos;
    private List<Equipamento> listaEquipamentos;
    private List<Evento> listaEventos;

    private AgendaNegocio agendaNegocio;
    private EventoDAO eventoDAO;

    public AgendaController() {
        this.evento = new Evento();
        this.agendaNegocio = new AgendaNegocio();
        this.eventoDAO = new EventoDAO();
    }

    /**
     * Método principal chamado pelo botão "Finalizar Agendamento" ou "Atualizar" na tela.
     * Capaz de distinguir entre a criação de um novo evento e a edição de um já existente.
     */
    public void salvar() {
        FacesContext context = FacesContext.getCurrentInstance();
        
        // Verifica se é um registro novo (ID nulo ou 0) ou uma edição
        boolean isNovo = (evento.getId() == null || evento.getId() == 0);

        try {
            if (isNovo) {
                // =========================================================
                // LÓGICA DE INSERÇÃO (Antigo agendar)
                // =========================================================
                
                // 1. Salva o evento base
                boolean eventoSalvo = eventoDAO.salvar(evento); 
                if (!eventoSalvo) {
                    throw new Exception("Falha ao salvar os dados base do evento.");
                }

                // 2. Monta o objeto de Escala
                Fotografo fotografoEscolhido = new Fotografo();
                fotografoEscolhido.setId(idFotografoSelecionado);
                
                EscalaEvento escala = new EscalaEvento();
                escala.setEvento(evento);
                escala.setFotografo(fotografoEscolhido);

                // 3. Verifica o horário do fotógrafo e salva a escala
                agendaNegocio.agendarFotografo(escala);

                // 4. Se o usuário selecionou equipamento extra
                if (idEquipamentoSelecionado != null && quantidadeRetirada != null && quantidadeRetirada > 0) {
                    Equipamento equipamentoEscolhido = null;
                    for (Equipamento eqp : getListaEquipamentos()) {
                        if (eqp.getId().equals(idEquipamentoSelecionado)) {
                            equipamentoEscolhido = eqp;
                            break;
                        }
                    }

                    if (equipamentoEscolhido != null) {
                        AlocacaoEquipamento alocacao = new AlocacaoEquipamento();
                        alocacao.setEscala(escala);
                        alocacao.setEquipamento(equipamentoEscolhido);
                        alocacao.setQuantidade(quantidadeRetirada);

                        // Verifica o estoque e salva a alocação
                        agendaNegocio.alocarEquipamento(alocacao);
                    }
                }

                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Sucesso", "Evento agendado e recursos alocados!"));
            } else {
                // =========================================================
                // LÓGICA DE EDIÇÃO / ATUALIZAÇÃO
                // =========================================================
                agendaNegocio.atualizarEvento(evento);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Sucesso", "Evento atualizado com sucesso!"));
            }

            // Limpa o formulário após sucesso em qualquer um dos casos
            cancelarEdicao();

        } catch (Exception e) {
            // ROLLBACK MANUAL: Se for um evento NOVO e der erro no meio do caminho, limpa o banco.
            if (isNovo && evento.getId() != null) {
                eventoDAO.excluir(evento.getId());
                evento.setId(null); 
            }

            String msg = e.getMessage();
            if (msg == null) {
                msg = "Erro interno no processamento. Veja o log do servidor.";
                e.printStackTrace(); 
            }

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Atenção - Regra de Negócio", msg));
                
        } finally {
            // Recarrega as tabelas para garantir consistência
            this.listaEventos = null;
            this.listaEquipamentos = null;
        }
    }

    /**
     * Carrega os dados da linha selecionada na tabela para o formulário.
     * @param eventoSelecionado O evento clicado na View.
     */
    public void prepararEdicao(Evento eventoSelecionado) {
        this.evento = eventoSelecionado;
        // Ao focar apenas em editar a entidade Evento, limpamos os campos de alocação secundária
        this.idFotografoSelecionado = null;
        this.idEquipamentoSelecionado = null;
        this.quantidadeRetirada = null;
    }

    /**
     * Limpa o formulário e cancela o processo de edição.
     */
    public void cancelarEdicao() {
        this.evento = new Evento();
        this.idFotografoSelecionado = null;
        this.idEquipamentoSelecionado = null;
        this.quantidadeRetirada = null;
    }
    
    /**
     * Método acionado pelo botão de exclusão/cancelamento de um evento na tabela.
     * @param e O objeto Evento selecionado pelo usuário.
     */
    public void excluir(Evento e) {
        boolean sucesso = eventoDAO.excluir(e.getId());
        FacesContext context = FacesContext.getCurrentInstance();
        
        if (sucesso) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Sucesso", "Evento cancelado e todos os recursos e agendas foram liberados!"));
            
            this.listaEventos = null;
            this.listaEquipamentos = null;
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Erro", "Não foi possível cancelar o evento selecionado."));
        }
    }
    
    // --- GETTERS E SETTERS ---
    
    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }

    public Integer getIdFotografoSelecionado() { return idFotografoSelecionado; }
    public void setIdFotografoSelecionado(Integer idFotografoSelecionado) { this.idFotografoSelecionado = idFotografoSelecionado; }

    public Integer getIdEquipamentoSelecionado() { return idEquipamentoSelecionado; }
    public void setIdEquipamentoSelecionado(Integer idEquipamentoSelecionado) { this.idEquipamentoSelecionado = idEquipamentoSelecionado; }

    public Integer getQuantidadeRetirada() { return quantidadeRetirada; }
    public void setQuantidadeRetirada(Integer quantidadeRetirada) { this.quantidadeRetirada = quantidadeRetirada; }

    public List<Fotografo> getListaFotografos() {
        if (listaFotografos == null) {
            listaFotografos = new FotografoDAO().listarTodos();
        }
        return listaFotografos;
    }

    public List<Equipamento> getListaEquipamentos() {
        if (listaEquipamentos == null) {
            listaEquipamentos = new EquipamentoDAO().listarTodos();
        }
        return listaEquipamentos;
    }
    
    public List<Evento> getListaEventos() {
        if (listaEventos == null) {
            listaEventos = eventoDAO.listarTodos();
        }
        return listaEventos;
    }
}