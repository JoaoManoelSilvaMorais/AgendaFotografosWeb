package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date; // Atenção: importar java.sql.Date para o banco, não o java.util.Date
import model.Fotografo;
import util.ConexaoBD;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;

/**
 * Classe responsável por centralizar todas as operações de banco de dados 
 * (CRUD) referentes à entidade Fotografo.
 * O uso do padrão DAO isola a regra de acesso a dados da regra de negócio.
 */
public class FotografoDAO {

    /**
     * Método responsável por inserir um novo fotógrafo no banco de dados.
     * @param fotografo Objeto contendo os dados capturados da tela.
     * @return true se a inserção for bem-sucedida, false caso contrário.
     */
    public boolean salvar(Fotografo fotografo) {
        
        // 1. Definimos o comando SQL com interrogações (?) no lugar dos valores.
        // Isso previne ataques de SQL Injection, um ponto muito importante para citar na apresentação!
        String sql = "INSERT INTO fotografo (nome_completo, cpf, telefone, data_contratacao, ativo) VALUES (?, ?, ?, ?, ?)";
        
        // 2. Abrimos a conexão com o banco e preparamos o comando.
        // O uso do try-catch aqui garante que, se o banco estiver fora do ar, o sistema não "quebra" feio na tela do usuário.
        try (Connection conn = ConexaoBD.getConexao(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // 3. Substituímos as interrogações (?) pelos valores do objeto Fotografo
            stmt.setString(1, fotografo.getNomeCompleto());
            stmt.setString(2, fotografo.getCpf());
            stmt.setString(3, fotografo.getTelefone());
            
            // Tratamento especial para datas: O Java usa LocalDate, mas o JDBC raiz pede java.sql.Date.
            // Aqui fazemos a conversão de forma limpa.
            if (fotografo.getDataContratacao() != null) {
                stmt.setDate(4, Date.valueOf(fotografo.getDataContratacao()));
            } else {
                stmt.setNull(4, java.sql.Types.DATE);
            }
            
            // Por padrão, ao cadastrar, o fotógrafo entra como ativo
            stmt.setBoolean(5, true);
            
            // 4. Executamos o comando no PostgreSQL. 
            // executeUpdate() retorna o número de linhas afetadas. Se for > 0, deu certo.
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
            
        } catch (SQLException e) {
            // Caso ocorra qualquer erro (ex: tentar cadastrar um CPF que já existe, ferindo a regra UNIQUE),
            // o erro é impresso no console para o desenvolvedor analisar.
            System.err.println("Erro ao salvar fotógrafo no banco de dados: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Método responsável por buscar todos os fotógrafos cadastrados no banco.
     * Retorna uma lista de objetos para que a camada de visão (JSF) possa exibir em uma tabela.
     * @return Lista de objetos Fotografo.
     */
    public List<Fotografo> listarTodos() {
        // 1. Inicializamos uma lista vazia. 
        // Boa prática: se o banco não tiver dados, retornamos a lista vazia ao invés de 'null', evitando NullPointerException na tela.
        List<Fotografo> lista = new ArrayList<>();
        
        // 2. Comando SQL para buscar todos os registros, já ordenando pelo nome para facilitar a visualização.
        String sql = "SELECT * FROM fotografo ORDER BY nome_completo";
        
        // 3. Novamente, o try-with-resources garante o fechamento automático da conexão.
        // Adicionamos o ResultSet aqui para receber os dados retornados pelo comando SELECT.
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            // 4. O ResultSet age como um cursor. O while percorre linha por linha da tabela do banco de dados.
            while (rs.next()) {
                // Para cada linha do banco, instanciamos um novo objeto Fotografo na memória do Java.
                Fotografo f = new Fotografo();
                
                // Mapeamento: Pegamos o valor da coluna do banco e "setamos" no atributo do objeto.
                f.setId(rs.getInt("id"));
                f.setNomeCompleto(rs.getString("nome_completo"));
                f.setCpf(rs.getString("cpf"));
                f.setTelefone(rs.getString("telefone"));
                
                // Tratamento especial da data: convertemos o java.sql.Date do banco de volta para o LocalDate do Java.
                java.sql.Date dataSQL = rs.getDate("data_contratacao");
                if (dataSQL != null) {
                    f.setDataContratacao(dataSQL.toLocalDate());
                }
                
                f.setAtivo(rs.getBoolean("ativo"));
                
                // 5. Adicionamos o objeto montado e preenchido na nossa lista.
                lista.add(f);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao listar fotógrafos: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 6. Retornamos a lista cheia (ou vazia, caso dê erro ou não haja registros) para quem chamou o método.
        return lista;
    }
    
    /**
     * Método responsável por remover um fotógrafo do banco de dados pelo seu ID.
     * @param id Identificador único do fotógrafo a ser removido.
     * @return true se a exclusão foi realizada com sucesso, false caso contrário.
     */
    public boolean excluir(int id) {
        // Comando SQL de exclusão física utilizando a cláusula WHERE pelo ID (chave primária)
        String sql = "DELETE FROM fotografo WHERE id = ?";
        
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
            
        } catch (SQLException e) {
            // PONTO CRÍTICO PARA A BANCA: Se o fotógrafo estiver escalado em um evento,
            // a Foreign Key (Chave Estrangeira) do PostgreSQL vai barrar a exclusão automática
            // para evitar dados órfãos. Capturamos essa falha de integridade aqui.
            System.err.println("Erro de integridade referencial ao excluir fotógrafo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
}