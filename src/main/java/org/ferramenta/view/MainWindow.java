package org.ferramenta.view;

import org.ferramenta.model.service.DBService;
import org.ferramenta.model.service.IDBService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {
    private IDBService dbService; // Agora usamos só a interface

    private JComboBox<String> tabelaCombo = new JComboBox<>();
    private JTable tabelaDados = new JTable() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private JButton btnInserir = new JButton("Inserir");
    private JButton btnEditar = new JButton("Editar");
    private JButton btnExcluir = new JButton("Excluir");
    private JButton btnProximo = new JButton("Próximo");
    private JButton btnAnterior = new JButton("Anterior");
    JButton btnExecutarSQL = new JButton("Executar SQL");

    private int paginaAtual = 0;
    private int limitePorPagina = 50;

    public MainWindow(IDBService service) {

        this.dbService = service;

        setTitle("Visualizador de Tabelas");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Tabela:"));
        topPanel.add(tabelaCombo);
        topPanel.add(btnInserir);
        topPanel.add(btnEditar);
        topPanel.add(btnExcluir);
        topPanel.add(btnExecutarSQL);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnAnterior);
        bottomPanel.add(btnProximo);
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(tabelaDados), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        tabelaCombo.addActionListener(e -> {
            paginaAtual = 0;
            carregarDadosTabela();
        });
        btnInserir.addActionListener(e -> abrirFormulario("inserir"));
        btnEditar.addActionListener(e -> abrirFormulario("editar"));
        btnExcluir.addActionListener(e -> excluirRegistro());
        btnProximo.addActionListener(e -> {
            paginaAtual++;
            carregarDadosTabela();
        });
        btnAnterior.addActionListener(e -> {
            if (paginaAtual > 0) {
                paginaAtual--;
                carregarDadosTabela();
            }
        });

        btnExecutarSQL.addActionListener(
                e -> new QueryExecutor(this, dbService)
        );

        carregarTabelas();
        setVisible(true);
    }

    private void carregarTabelas() {
        try {
            List<String> tabelas = dbService.listarTabelas(null);
            tabelaCombo.removeAllItems();
            for (String tabela : tabelas) {
                tabelaCombo.addItem(tabela);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao listar tabelas: " + e.getMessage());
        }
    }

    public void carregarDadosTabela() {
        String tabela = (String) tabelaCombo.getSelectedItem();
        if (tabela == null) return;
        try {
            String[] colunas = dbService.listarColunas(tabela);
            List<String[]> dados = dbService.listarDados(tabela, limitePorPagina, paginaAtual * limitePorPagina);
            DefaultTableModel model = new DefaultTableModel(colunas, 0);
            for (String[] linha : dados) {
                model.addRow(linha);
            }
            tabelaDados.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage());
        }
    }

    private void abrirFormulario(String acao) {
        String tabela = (String) tabelaCombo.getSelectedItem();
        if (tabela == null) return;
        String[] colunas;
        String[] dados = null;
        try {
            colunas = dbService.listarColunas(tabela);
            if ("editar".equals(acao)) {
                int row = tabelaDados.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Selecione uma linha para editar.");
                    return;
                }
                dados = new String[colunas.length];
                for (int i = 0; i < colunas.length; i++) {
                    Object value = tabelaDados.getValueAt(row, i);
                    dados[i] = value != null ? value.toString() : "";
                }
            }
            new FormularioRegistro(this, dbService, tabela, colunas, dados, acao);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void excluirRegistro() {
        String tabela = (String) tabelaCombo.getSelectedItem();
        if (tabela == null) return;
        try {
            String[] colunas = dbService.listarColunas(tabela);
            int row = tabelaDados.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma linha para excluir.");
                return;
            }
            List<String> condicoes = new ArrayList<>();

            for (int i = 0; i < colunas.length; i++) {
                Object cellValue = tabelaDados.getValueAt(row, i);
                if (cellValue != null) {
                    String valor = cellValue.toString().trim();
                    if (!valor.isEmpty()) {
                        condicoes.add(colunas[i] + " = '" + valor + "'");
                    }
                }
            }

            StringBuilder condicao = new StringBuilder(String.join(" AND ", condicoes));

            int confirm = JOptionPane.showConfirmDialog(this, "Deseja realmente excluir este registro?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.out.println(tabela);
                System.out.println(condicao);
                dbService.excluirRegistro(tabela, condicao.toString());
                carregarDadosTabela();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + e.getMessage());
        }
    }
}

