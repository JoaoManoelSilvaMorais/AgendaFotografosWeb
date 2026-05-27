package negocio;

import model.AlocacaoEquipamento;
import model.EscalaEvento;
import model.Equipamento;
import persistencia.EscalaEventoDAO;
import persistencia.AlocacaoEquipamentoDAO;

/**
 * Classe de Negócio responsável por orquestrar as regras da agenda.
 * Atua como uma intermediária entre o Controlador (Tela) e os DAOs (Banco de Dados).
 */
public class AgendaNegocio {

    private EscalaEventoDAO escalaDAO;
    private AlocacaoEquipamentoDAO alocacaoDAO;

    public AgendaNegocio() {
        this.escalaDAO = new EscalaEventoDAO();
        this.alocacaoDAO = new AlocacaoEquipamentoDAO();
    }

    /**
     * Tenta agendar um fotógrafo para um evento.
     * Aplica a regra de negócio de verificação de choque de horários.
     * * @param escala Objeto contendo o Fotografo e o Evento pretendido.
     * @throws Exception Caso o fotógrafo já esteja ocupado no horário.
     */
    public void agendarFotografo(EscalaEvento escala) throws Exception {
        
        // 1. Extrai os IDs e horários para a validação
        Integer idFotografo = escala.getFotografo().getId();
        java.time.LocalDateTime inicio = escala.getEvento().getDataHoraInicio();
        java.time.LocalDateTime fim = escala.getEvento().getDataHoraFim();

        // 2. REGRA DE NEGÓCIO: Consulta o banco para saber se há choque de horário
        boolean disponivel = escalaDAO.isFotografoDisponivel(idFotografo, inicio, fim);

        if (!disponivel) {
            // Se estiver ocupado, barramos a operação aqui mesmo, estourando um erro
            // que será capturado pela tela para avisar o usuário.
            throw new Exception("Operação negada: O fotógrafo já possui um evento agendado neste horário.");
        }

        // 3. Se passou pela validação, manda o DAO gravar no banco
        boolean sucesso = escalaDAO.salvar(escala);
        
        if (!sucesso) {
            throw new Exception("Erro interno ao tentar salvar a escala no banco de dados.");
        }
    }

    /**
     * Registra a retirada de um equipamento para um evento específico.
     * Valida se a empresa tem estoque suficiente antes de alocar.
     * * @param alocacao Objeto contendo a escala, o equipamento e a quantidade desejada.
     * @throws Exception Caso a quantidade solicitada seja maior que o estoque disponível.
     */
    public void alocarEquipamento(AlocacaoEquipamento alocacao) throws Exception {
        
        Equipamento eqp = alocacao.getEquipamento();
        int quantidadeSolicitada = alocacao.getQuantidade();
        
        // 1. REGRA DE NEGÓCIO: Cálculo de estoque disponível em tempo real
        int estoqueDisponivel = eqp.getQuantidadeTotal() - eqp.getQuantidadeEmUso();

        if (quantidadeSolicitada > estoqueDisponivel) {
            throw new Exception("Operação negada: Estoque insuficiente. Temos apenas " + estoqueDisponivel + " unidades disponíveis.");
        }

        if (quantidadeSolicitada <= 0) {
            throw new Exception("A quantidade retirada deve ser maior que zero.");
        }

        // 2. Estando tudo certo, aciona o DAO transacional para gravar a alocação e atualizar o estoque
        boolean sucesso = alocacaoDAO.salvar(alocacao);
        
        if (!sucesso) {
            throw new Exception("Erro interno ao tentar registrar a alocação do equipamento.");
        }
    }
}