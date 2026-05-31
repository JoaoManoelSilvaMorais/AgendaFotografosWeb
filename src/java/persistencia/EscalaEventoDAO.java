package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import model.EscalaEvento;
import util.ConexaoBD;

/**
 * DAO responsável por gerenciar a alocação de fotógrafos em eventos.
 * Resolve a cardinalidade N:N transformando-a em dois relacionamentos 1:N,
 * e implementa a verificação de choque de horários na agenda.
 */
public class EscalaEventoDAO {

    /**
     * Vincula um fotógrafo a um evento.
     * @param escala Objeto contendo as instâncias de Fotografo e Evento.
     * @return true se a escala for salva com sucesso.
     */
    public boolean salvar(EscalaEvento escala) {
        // ADICIONADO: A cláusula RETURNING id nativa
        String sql = "INSERT INTO escala_evento (id_fotografo, id_evento) VALUES (?, ?) RETURNING id";
        
        try (Connection conn = ConexaoBD.getConexao(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, escala.getFotografo().getId());
            stmt.setInt(2, escala.getEvento().getId());
            
            // Executa e lê o retorno imediato do banco
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    escala.setId(rs.getInt("id"));
                    return true;
                }
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Erro ao salvar escala: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * REGRA DE NEGÓCIO CRÍTICA: Verifica se há sobreposição de horários.
     * @param idFotografo O ID do fotógrafo que estamos tentando escalar.
     * @param inicioNovoEvento O horário de início do evento desejado.
     * @param fimNovoEvento O horário final do evento desejado.
     * @return true se ele ESTIVER DISPONÍVEL (nenhum choque de horário), false se estiver ocupado.
     */
    public boolean isFotografoDisponivel(Integer idFotografo, LocalDateTime inicioNovoEvento, LocalDateTime fimNovoEvento) {
        
        // A Lógica do Overlap (Sobreposição): 
        // Um choque ocorre se o início do evento já agendado for menor que o fim do novo, 
        // E o fim do evento já agendado for maior que o início do novo.
        String sql = "SELECT COUNT(*) AS total_conflitos FROM escala_evento ee "
                   + "JOIN evento e ON ee.id_evento = e.id "
                   + "WHERE ee.id_fotografo = ? "
                   + "AND e.data_hora_inicio < ? "
                   + "AND e.data_hora_fim > ?";
                   
        try (Connection conn = ConexaoBD.getConexao(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idFotografo);
            stmt.setTimestamp(2, Timestamp.valueOf(fimNovoEvento));
            stmt.setTimestamp(3, Timestamp.valueOf(inicioNovoEvento));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int conflitos = rs.getInt("total_conflitos");
                    // Se o total de conflitos for 0, ele está disponível (retorna true)
                    return conflitos == 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao verificar disponibilidade: " + e.getMessage());
            e.printStackTrace();
            // Em caso de erro de banco, bloqueamos a escala por segurança
            return false; 
        }
        
        return false;
    }
}