package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp; // Classe essencial para gravar data e hora juntas no PostgreSQL
import java.util.ArrayList;
import java.util.List;
import model.Evento;
import util.ConexaoBD;

/**
 * Classe responsável pelo acesso a dados (DAO) da entidade Evento.
 * Gerencia a persistência da agenda, garantindo a conversão correta 
 * dos fusos e horários da aplicação para o formato aceito pelo banco de dados.
 */
public class EventoDAO {

    /**
     * Método responsável por salvar um novo evento na agenda.
     * @param evento Objeto contendo título, horários e local.
     * @return true se o agendamento for concluído com sucesso, false em caso de falha.
     */
    public boolean salvar(Evento evento) {
        // A cláusula RETURNING id nativa do PostgreSQL
        String sql = "INSERT INTO evento (titulo, data_hora_inicio, data_hora_fim, localizacao) VALUES (?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = ConexaoBD.getConexao(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, evento.getTitulo());
            
            if (evento.getDataHoraInicio() != null) {
                stmt.setTimestamp(2, java.sql.Timestamp.valueOf(evento.getDataHoraInicio()));
            } else {
                stmt.setNull(2, java.sql.Types.TIMESTAMP);
            }
            
            if (evento.getDataHoraFim() != null) {
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(evento.getDataHoraFim()));
            } else {
                stmt.setNull(3, java.sql.Types.TIMESTAMP);
            }
            
            stmt.setString(4, evento.getLocalizacao());
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Captura a coluna 'id' devolvida pelo RETURNING
                    evento.setId(rs.getInt("id")); 
                    return true;
                }
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Erro ao salvar evento: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * NOVO MÉTODO: Atualiza os dados de um evento existente.
     * @param evento Objeto contendo os novos dados do evento.
     * @return true se a atualização for concluída com sucesso.
     */
    public boolean atualizar(Evento evento) {
        String sql = "UPDATE evento SET titulo = ?, data_hora_inicio = ?, data_hora_fim = ?, localizacao = ?, status = ? WHERE id = ?";
        
        try (Connection conn = ConexaoBD.getConexao(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, evento.getTitulo());
            
            if (evento.getDataHoraInicio() != null) {
                stmt.setTimestamp(2, java.sql.Timestamp.valueOf(evento.getDataHoraInicio()));
            } else {
                stmt.setNull(2, java.sql.Types.TIMESTAMP);
            }
            
            if (evento.getDataHoraFim() != null) {
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(evento.getDataHoraFim()));
            } else {
                stmt.setNull(3, java.sql.Types.TIMESTAMP);
            }
            
            stmt.setString(4, evento.getLocalizacao());
            stmt.setString(5, evento.getStatus() != null ? evento.getStatus() : "Agendado");
            stmt.setInt(6, evento.getId());
            
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar evento: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recupera todos os eventos cadastrados.
     * @return Lista populada de eventos, ordenada cronologicamente.
     */
    public List<Evento> listarTodos() {
        List<Evento> lista = new ArrayList<>();
        
        String sql = "SELECT * FROM evento ORDER BY data_hora_inicio";
        
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Evento evt = new Evento();
                
                evt.setId(rs.getInt("id"));
                evt.setTitulo(rs.getString("titulo"));
                
                Timestamp tsInicio = rs.getTimestamp("data_hora_inicio");
                if (tsInicio != null) {
                    evt.setDataHoraInicio(tsInicio.toLocalDateTime());
                }
                
                Timestamp tsFim = rs.getTimestamp("data_hora_fim");
                if (tsFim != null) {
                    evt.setDataHoraFim(tsFim.toLocalDateTime());
                }
                
                evt.setLocalizacao(rs.getString("localizacao"));
                evt.setStatus(rs.getString("status")); 
                
                lista.add(evt);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao listar eventos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return lista;
    }
    
    /**
     * Remove um evento de forma totalmente transacional.
     * Desfaz as alocações devolvendo os equipamentos ao estoque, limpa as escalas
     * e remove o evento sem deixar dados órfãos ou corromper o inventário.
     * @param idEvento ID do evento a ser excluído.
     * @return true se toda a cadeia de exclusão foi um sucesso.
     */
    public boolean excluir(int idEvento) {
        String sqlUpdateEstoque = "UPDATE equipamento SET quantidade_em_uso = quantidade_em_uso - ae.quantidade "
                               + "FROM alocacao_equipamento ae "
                               + "JOIN escala_evento ee ON ae.id_escala = ee.id "
                               + "WHERE ee.id_evento = ? AND equipamento.id = ae.id_equipamento";
        
        String sqlDeleteAlocacao = "DELETE FROM alocacao_equipamento WHERE id_escala IN (SELECT id FROM escala_evento WHERE id_evento = ?)";
        
        String sqlDeleteEscala = "DELETE FROM escala_evento WHERE id_evento = ?";
        
        String sqlDeleteEvento = "DELETE FROM evento WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmtUpdate = null;
        PreparedStatement stmtDelAloc = null;
        PreparedStatement stmtDelEsc = null;
        PreparedStatement stmtDelEvt = null;
        
        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false); 
            
            stmtUpdate = conn.prepareStatement(sqlUpdateEstoque);
            stmtUpdate.setInt(1, idEvento);
            stmtUpdate.executeUpdate();
            
            stmtDelAloc = conn.prepareStatement(sqlDeleteAlocacao);
            stmtDelAloc.setInt(1, idEvento);
            stmtDelAloc.executeUpdate();
            
            stmtDelEsc = conn.prepareStatement(sqlDeleteEscala);
            stmtDelEsc.setInt(1, idEvento);
            stmtDelEsc.executeUpdate();
            
            stmtDelEvt = conn.prepareStatement(sqlDeleteEvento);
            stmtDelEvt.setInt(1, idEvento);
            int linhasAfetadas = stmtDelEvt.executeUpdate();
            
            conn.commit(); 
            return linhasAfetadas > 0;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); 
                    System.err.println("Rollback executado: Erro ao tentar limpar dependências do evento.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Erro ao excluir evento de forma transacional: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmtUpdate != null) stmtUpdate.close();
                if (stmtDelAloc != null) stmtDelAloc.close();
                if (stmtDelEsc != null) stmtDelEsc.close();
                if (stmtDelEvt != null) stmtDelEvt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}