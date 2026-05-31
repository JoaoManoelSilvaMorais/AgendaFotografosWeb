package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBD {
    
    private static final String URL = "jdbc:postgresql://localhost:5432/agendafotografos";
    private static final String USUARIO = "postgres"; 
    private static final String SENHA = "123456"; // Coloque a sua senha aqui!

    public static Connection getConexao() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver do PostgreSQL n√£o encontrado! Verifique a biblioteca.", e);
        }
    }

    /*// --- BLOCO DE TESTE ---
    public static void main(String[] args) {
        try {
            Connection conn = getConexao();
            if (conn != null) {
                System.out.println("‚úÖ Sucesso! Conex√£o com o PostgreSQL estabelecida perfeitamente.");
                conn.close(); 
            }
        } catch (SQLException e) {
            System.err.println("‚?å Erro ao conectar com o banco de dados:");
            e.printStackTrace();
        }
    }*/
}