package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date; 
import model.Fotografo;
import util.ConexaoBD;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;

public class FotografoDAO {

    public boolean salvar(Fotografo fotografo) {
        String sql = "INSERT INTO fotografo (nome_completo, cpf, telefone, data_contratacao, ativo) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = ConexaoBD.getConexao(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, fotografo.getNomeCompleto());
            stmt.setString(2, fotografo.getCpf());
            stmt.setString(3, fotografo.getTelefone());
            
            if (fotografo.getDataContratacao() != null) {
                stmt.setDate(4, Date.valueOf(fotografo.getDataContratacao()));
            } else {
                stmt.setNull(4, java.sql.Types.DATE);
            }
            
            stmt.setBoolean(5, true);
            
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao salvar fotógrafo no banco de dados: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // NOVO MÉTODO: Atualizar registro existente
    public boolean atualizar(Fotografo fotografo) {
        String sql = "UPDATE fotografo SET nome_completo = ?, cpf = ?, telefone = ?, data_contratacao = ?, ativo = ? WHERE id = ?";
        
        try (Connection conn = ConexaoBD.getConexao(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, fotografo.getNomeCompleto());
            stmt.setString(2, fotografo.getCpf());
            stmt.setString(3, fotografo.getTelefone());
            
            if (fotografo.getDataContratacao() != null) {
                stmt.setDate(4, Date.valueOf(fotografo.getDataContratacao()));
            } else {
                stmt.setNull(4, java.sql.Types.DATE);
            }
            
            // Verifica se o ativo não está nulo para evitar erro, por padrão mantém true
            stmt.setBoolean(5, fotografo.getAtivo() != null ? fotografo.getAtivo() : true);
            
            // O ID vai na cláusula WHERE (última interrogação)
            stmt.setInt(6, fotografo.getId());
            
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar fotógrafo no banco de dados: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Fotografo> listarTodos() {
        List<Fotografo> lista = new ArrayList<>();
        String sql = "SELECT * FROM fotografo ORDER BY nome_completo";
        
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Fotografo f = new Fotografo();
                
                f.setId(rs.getInt("id"));
                f.setNomeCompleto(rs.getString("nome_completo"));
                f.setCpf(rs.getString("cpf"));
                f.setTelefone(rs.getString("telefone"));
                
                java.sql.Date dataSQL = rs.getDate("data_contratacao");
                if (dataSQL != null) {
                    f.setDataContratacao(dataSQL.toLocalDate());
                }
                
                f.setAtivo(rs.getBoolean("ativo"));
                
                lista.add(f);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao listar fotógrafos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return lista;
    }
    
    public boolean excluir(int id) {
        String sql = "DELETE FROM fotografo WHERE id = ?";
        
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro de integridade referencial ao excluir fotógrafo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}