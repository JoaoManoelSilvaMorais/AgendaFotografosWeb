package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import model.AlocacaoEquipamento;
import util.ConexaoBD;

/**
 * DAO responsável por gerenciar a retirada de equipamentos para os eventos.
 * Implementa controle transacional para garantir a consistência do estoque.
 */
public class AlocacaoEquipamentoDAO {

    /**
     * Salva a alocação e atualiza a quantidade em uso do equipamento simultaneamente.
     * @param alocacao Objeto contendo os dados da escala, equipamento e quantidade retirada.
     * @return true se ambas as operações (inserção e atualização) forem um sucesso.
     */
    public boolean salvar(AlocacaoEquipamento alocacao) {
        // Teremos duas instruções SQL rodando em sequência
        String sqlInsert = "INSERT INTO alocacao_equipamento (id_escala, id_equipamento, quantidade) VALUES (?, ?, ?)";
        
        // Atualiza o estoque somando a quantidade recém-retirada ao que já estava em uso
        String sqlUpdate = "UPDATE equipamento SET quantidade_em_uso = quantidade_em_uso + ? WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmtInsert = null;
        PreparedStatement stmtUpdate = null;
        
        try {
            conn = ConexaoBD.getConexao();
            
            // 1. REGRA DE OURO DA TRANSAÇÃO: Desligamos o "salvamento automático".
            // Isso diz ao banco: "Espere eu dar a ordem final antes de gravar de verdade."
            conn.setAutoCommit(false);
            
            // 2. Prepara e executa a INSERÇÃO na tabela de alocação
            stmtInsert = conn.prepareStatement(sqlInsert);
            stmtInsert.setInt(1, alocacao.getEscala().getId());
            stmtInsert.setInt(2, alocacao.getEquipamento().getId());
            stmtInsert.setInt(3, alocacao.getQuantidade());
            stmtInsert.executeUpdate(); // Fica em espera (pendente)
            
            // 3. Prepara e executa a ATUALIZAÇÃO na tabela de equipamento
            stmtUpdate = conn.prepareStatement(sqlUpdate);
            stmtUpdate.setInt(1, alocacao.getQuantidade());
            stmtUpdate.setInt(2, alocacao.getEquipamento().getId());
            stmtUpdate.executeUpdate(); // Fica em espera (pendente)
            
            // 4. COMMIT: Se o código chegou até aqui sem dar erro, confirmamos TUDO de uma vez.
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            // 5. ROLLBACK: Se deu qualquer problema em qualquer etapa, nós desfazemos tudo.
            // O banco volta exatamente para o estado em que estava antes de o método ser chamado.
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transação desfeita (Rollback) para manter a integridade do estoque.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Erro ao processar alocação: " + e.getMessage());
            e.printStackTrace();
            return false;
            
        } finally {
            // 6. Fechamento seguro das conexões (como não usamos try-with-resources aqui, fechamos manualmente)
            try {
                if (stmtInsert != null) stmtInsert.close();
                if (stmtUpdate != null) stmtUpdate.close();
                if (conn != null) {
                    // Restauramos o comportamento padrão do banco antes de devolver a conexão
                    conn.setAutoCommit(true); 
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}