package org.ferramenta.model.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class PostgresService extends BaseDBService {

    public PostgresService(Connection connection) {
        super(connection);
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public Map<String, Boolean> colunasAutoIncrementadas(String tabela) throws SQLException {
        return null;
    }

    @Override
    public Map<String, Boolean> colunasObrigatorias(String tabela) throws SQLException {
        return null;
    }

    @Override
    public void inserirRegistro(String tabela, Map<String, String> dados) throws SQLException {

    }

    @Override
    public void editarRegistro(String tabela, Map<String, String> dados, String condicao) throws SQLException {

    }

    // Se necessário, sobrescreva métodos específicos do PostgreSQL aqui
}