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
 * passando pelas regras de negócio rigorosas da aplicação.
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
     * Método principal chamado pelo botão "Finalizar Agendamento" na tela.
     */
    public void agendar() {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
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
                
                // CORREÇÃO: Em vez de criar um equipamento vazio só com ID, 
                // nós buscamos o objeto INTEIRO da lista para ter as quantidades carregadas!
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

            // SUCESSO ABSOLUTO
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Sucesso", "Evento agendado e recursos alocados!"));
            
            // Limpa o formulário
            this.evento = new Evento();
            this.idFotografoSelecionado = null;
            this.idEquipamentoSelecionado = null;
            this.quantidadeRetirada = null;

        } catch (Exception e) {
            // ROLLBACK MANUAL: Se deu erro (estoque insuficiente ou fotógrafo ocupado) 
            // DEPOIS que o evento já tinha sido gravado, nós apagamos ele para não sujar o banco.
            if (evento.getId() != null) {
                eventoDAO.excluir(evento.getId());
                evento.setId(null); // Tira o ID da memória
            }

            // Tratamento contra mensagens nulas (NullPointerException)
            String msg = e.getMessage();
            if (msg == null) {
                msg = "Erro interno no processamento. Veja o log do GlassFish.";
                e.printStackTrace(); // Registra no console para o programador ver
            }

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Atenção - Regra de Negócio", msg));
                
        } finally {
            // BLOCO FINALLY: Executa sempre, dando erro ou sucesso!
            // Isso garante que a tabela no rodapé da página SEMPRE atualize, não importa o que aconteça.
            this.listaEventos = null;
            this.listaEquipamentos = null;
        }
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
            
            // Reseta as listas locais para que a tela atualize o estoque e os eventos de imediato
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

    /**
     * Carrega a lista de fotógrafos para o SelectOneMenu (Dropdown) da tela.
     */
    public List<Fotografo> getListaFotografos() {
        if (listaFotografos == null) {
            listaFotografos = new FotografoDAO().listarTodos();
        }
        return listaFotografos;
    }

    /**
     * Carrega a lista de equipamentos para o SelectOneMenu (Dropdown) da tela.
     */
    public List<Equipamento> getListaEquipamentos() {
        if (listaEquipamentos == null) {
            listaEquipamentos = new EquipamentoDAO().listarTodos();
        }
        return listaEquipamentos;
    }
    
    /**
     * Getter da lista de eventos utilizando Lazy Loading.
     */
    public List<Evento> getListaEventos() {
        if (listaEventos == null) {
            listaEventos = eventoDAO.listarTodos();
        }
        return listaEventos;
    }
}