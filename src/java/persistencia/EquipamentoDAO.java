package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Equipamento;
import util.ConexaoBD;

/**
 * Classe responsável pelo acesso a dados (DAO) da entidade Equipamento.
 * Centraliza as operações de banco de dados, mantendo o código limpo e 
 * isolando a camada de persistência da camada de visão.
 */
public class EquipamentoDAO {

    /**
     * Salva um novo equipamento no estoque da empresa.
     * @param equipamento Objeto contendo as informações digitadas pelo usuário na tela.
     * @return true em caso de sucesso na gravação, false caso ocorra algum erro.
     */
    public boolean salvar(Equipamento equipamento) {
        // 1. O comando SQL de inserção. 
        // Note que passamos 4 interrogações (?) como parâmetros de segurança.
        String sql = "INSERT INTO equipamento (descricao, tipo, quantidade_total, quantidade_em_uso) VALUES (?, ?, ?, ?)";
        
        // 2. Uso do try-with-resources para garantir que a conexão será fechada de forma segura
        // independentemente de a gravação dar certo ou falhar.
        try (Connection conn = ConexaoBD.getConexao(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // 3. Preenchimento seguro dos parâmetros (evitando SQL Injection).
            stmt.setString(1, equipamento.getDescricao());
            stmt.setString(2, equipamento.getTipo());
            stmt.setInt(3, equipamento.getQuantidadeTotal());
            
            // REGRA DE NEGÓCIO: Quando um equipamento é recém-comprado/cadastrado, 
            // a quantidade em uso dele é obrigatoriamente zero, pois ainda não foi alocado.
            stmt.setInt(4, 0); 
            
            // 4. Execução no banco de dados.
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao salvar equipamento: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * NOVO MÉTODO: Atualiza as informações de um equipamento já existente no estoque.
     * @param equipamento Objeto contendo as novas informações.
     * @return true se a atualização foi concluída com sucesso no banco.
     */
    public boolean atualizar(Equipamento equipamento) {
        String sql = "UPDATE equipamento SET descricao = ?, tipo = ?, quantidade_total = ?, quantidade_em_uso = ? WHERE id = ?";
        
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, equipamento.getDescricao());
            stmt.setString(2, equipamento.getTipo());
            stmt.setInt(3, equipamento.getQuantidadeTotal());
            
            // Mantém a quantidade em uso atual, a menos que o administrador esteja forçando um ajuste manual
            stmt.setInt(4, equipamento.getQuantidadeEmUso() != null ? equipamento.getQuantidadeEmUso() : 0);
            
            // O ID é essencial na cláusula WHERE para não atualizar a tabela inteira por acidente
            stmt.setInt(5, equipamento.getId());
            
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar equipamento: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retorna todo o inventário de equipamentos da empresa.
     * Útil para preencher a tabela da tela de controle de estoque.
     * @return Lista populada com objetos Equipamento (nunca retorna null).
     */
    public List<Equipamento> listarTodos() {
        // 1. Inicializamos a lista. O retorno vazio em caso de erro previne NullPointerException.
        List<Equipamento> lista = new ArrayList<>();
        
        // 2. Ordenamos pela descrição para que a tabela fique em ordem alfabética na interface do usuário.
        String sql = "SELECT * FROM equipamento ORDER BY descricao";
        
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            // 3. O ResultSet atua como um cursor iterando sobre as linhas que o banco devolveu.
            while (rs.next()) {
                // Instancia um novo objeto a cada volta do laço para mapear a linha do banco.
                Equipamento eqp = new Equipamento();
                
                eqp.setId(rs.getInt("id"));
                eqp.setDescricao(rs.getString("descricao"));
                eqp.setTipo(rs.getString("tipo"));
                eqp.setQuantidadeTotal(rs.getInt("quantidade_total"));
                eqp.setQuantidadeEmUso(rs.getInt("quantidade_em_uso"));
                
                // 4. Adiciona o objeto preenchido na nossa lista de retorno.
                lista.add(eqp);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao listar equipamentos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return lista;
    }
    
    /**
     * Remove um equipamento do inventário pelo seu ID.
     * @param id Identificador do equipamento a ser deletado.
     * @return true se a exclusão foi concluída, false em caso de falha de banco ou integridade.
     */
    public boolean excluir(int id) {
        String sql = "DELETE FROM equipamento WHERE id = ?";
        
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            // O PostgreSQL vai barrar a exclusão se o equipamento tiver registros na tabela de 'alocacao_equipamento'.
            System.err.println("Erro de integridade referencial ao excluir equipamento: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}