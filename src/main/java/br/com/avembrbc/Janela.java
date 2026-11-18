package br.com.avembrbc;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public final class Janela extends JFrame implements Telas.App {

    private static final String DICA = "Digite ou selecione";
    private static final Color DICA_COR = new Color(0x8AA0B8);

    private Banco banco;
    private boolean ajustando;
    private final CardLayout cartas = new CardLayout();
    private final JPanel paginas = new JPanel(cartas);
    private final JComboBox<Banco.Area> areas = new JComboBox<Banco.Area>();
    private final JPanel formulario = new JPanel(new GridBagLayout());
    private final JTextArea ajudaInicial = ajuda("Escolha a área para descrever o problema e buscar uma solução.");
    private final JTextArea ajudaCampos = ajuda("Digite o valor ou escolha um da lista. Em branco, entra na conta com similaridade 0 e o peso permanece. "
            + "Suspeita de Newcastle, influenza aviária, salmonelose ou micoplasmose comunica-se ao serviço veterinário oficial.");
    private final Map<Long, JComboBox<String>> entradas = new LinkedHashMap<Long, JComboBox<String>>();
    private final JCheckBox soValidos = new JCheckBox("Somente casos validados");
    private final JTextArea solucao = new JTextArea(6, 20);
    private final JLabel metaSolucao = new JLabel(" ");
    private final JButton reter = botao("Reter caso");
    private final JPanel blocoResultados = cartao("Casos similares recuperados", new JPanel(new BorderLayout(0, 8)));
    private final DefaultTableModel modeloResultados = modelo("#", "Similaridade", "Caso", "Quadro", "Situação");
    private final JTable resultados = tabela(modeloResultados);
    private final JLabel vazio = new JLabel("Nenhum caso parecido com o problema descrito.");
    private final DefaultTableModel modeloComparacao = modelo("Atributo", "Peso", "Novo problema", "Caso recuperado", "Similaridade local");
    private final JTable comparacao = tabela(modeloComparacao);
    private final DefaultTableModel modeloCasos = modelo("Id", "Área", "Quadro", "Validado", "Fonte");
    private final JTable casos = tabela(modeloCasos);
    private final JLabel statusBanco = new JLabel(" ");
    private final JLabel status = new JLabel(" ");
    private final List<JButton> menu = new ArrayList<JButton>();
    private List<Motor.Recuperado> achados = new ArrayList<Motor.Recuperado>();
    private Motor.Recuperado aplicado;
    private Telas telas;

    Janela() {
        super("AvembRBC");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImage(Tema.icone(64));
        setMinimumSize(new Dimension(980, 680));
        setSize(1120, 760);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Tema.PAGINA);
        banco = Banco.h2Quiet();
        telas = new Telas(this);
        prepararSolucao();
        setJMenuBar(menuArquivo());
        add(cabecalho(), BorderLayout.NORTH);
        paginas.setOpaque(false);
        paginas.add(pagina(telas.inicio), "inicio");
        paginas.add(pagina(consulta()), "consultar");
        paginas.add(pagina(telas.revisar), "revisar");
        paginas.add(pagina(telas.areas), "areas");
        paginas.add(pagina(telas.campos), "campos");
        paginas.add(pagina(telas.dicionario), "dicionario");
        paginas.add(pagina(telas.similaridade), "similaridade");
        paginas.add(pagina(painelCasos()), "casos");
        paginas.add(pagina(sobre()), "sobre");
        cartas.show(paginas, "inicio");
        add(paginas, BorderLayout.CENTER);
        status.setBorder(new EmptyBorder(6, 16, 6, 16));
        status.setForeground(Tema.AZUL);
        status.setOpaque(true);
        status.setBackground(Tema.GELO);
        add(status, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (banco != null) {
                    banco.close();
                }
            }
        });
        areas.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                String texto = value == null ? "Selecione a área" : ((Banco.Area) value).nome;
                return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
            }
        });
        areas.addActionListener(e -> {
            if (!ajustando) {
                montarFormulario();
            }
        });
        resultados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultados.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarComparacao();
            }
        });
        carregarAreas();
        listarCasos();
        atualizarStatus();
        selecionarMenu(0);
    }

    private JMenuBar menuArquivo() {
        JMenuBar barra = new JMenuBar();
        JMenu arquivo = new JMenu("Arquivo");
        JMenuItem sair = new JMenuItem("Sair");
        sair.addActionListener(e -> dispose());
        arquivo.add(sair);
        JMenu dados = new JMenu("Banco");
        JMenuItem h2 = new JMenuItem("H2 em memória");
        h2.addActionListener(e -> usarH2());
        JMenuItem pg = new JMenuItem("PostgreSQL...");
        pg.addActionListener(e -> usarPostgres());
        dados.add(h2);
        dados.add(pg);
        barra.add(arquivo);
        barra.add(dados);
        return barra;
    }

    private JPanel cabecalho() {
        JPanel topo = new JPanel(new BorderLayout(16, 0));
        topo.setBackground(Color.WHITE);
        topo.setBorder(new EmptyBorder(10, 18, 8, 18));
        JLabel marca = new JLabel("<html><span style='color:#333333'>Avemb</span><span style='color:#00783E'>RBC</span></html>",
                new javax.swing.ImageIcon(Tema.icone(36)), SwingConstants.LEFT);
        marca.setFont(new Font("Segoe UI", Font.BOLD, 26));
        marca.setIconTextGap(8);
        topo.add(marca, BorderLayout.WEST);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        nav.setOpaque(false);
        nav.add(itemMenu("INÍCIO", "tudo inicia aqui", "inicio", 0));
        nav.add(menuSuspenso("CADASTROS", "cadastros", 1,
                new String[] {"Áreas", "Campos", "Dicionário", "Similaridade local", "Casos"},
                new String[] {"areas", "campos", "dicionario", "similaridade", "casos"}));
        nav.add(menuSuspenso("SOLUÇÕES", "buscar solução", 2,
                new String[] {"Pesquisa de casos", "Revisar casos"},
                new String[] {"consultar", "revisar"}));
        nav.add(itemMenu("SOBRE", "sobre o sistema", "sobre", 3));
        topo.add(nav, BorderLayout.CENTER);

        JPanel lado = new JPanel();
        lado.setOpaque(false);
        lado.setLayout(new BoxLayout(lado, BoxLayout.Y_AXIS));
        statusBanco.setForeground(Tema.AZUL);
        statusBanco.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JButton postgres = botaoSecundario("PostgreSQL");
        postgres.setAlignmentX(Component.RIGHT_ALIGNMENT);
        postgres.addActionListener(e -> usarPostgres());
        lado.add(statusBanco);
        lado.add(Box.createVerticalStrut(4));
        lado.add(postgres);
        topo.add(lado, BorderLayout.EAST);

        JPanel faixa = new JPanel();
        faixa.setBackground(Tema.AZUL);
        faixa.setPreferredSize(new Dimension(1, 3));
        JPanel bloco = new JPanel(new BorderLayout());
        bloco.add(topo, BorderLayout.CENTER);
        bloco.add(faixa, BorderLayout.SOUTH);
        return bloco;
    }

    private JButton itemMenu(String titulo, String subtitulo, String carta, int indice) {
        JButton botao = new JButton("<html><center><b>" + titulo + "</b><br><span style='color:#8AA0B8;font-size:10px'><i>"
                + subtitulo + "</i></span></center></html>");
        botao.setOpaque(false);
        botao.setContentAreaFilled(false);
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setForeground(Tema.GRAFITE);
        botao.addActionListener(e -> ir(carta));
        menu.add(botao);
        return botao;
    }

    private JButton menuSuspenso(String titulo, String subtitulo, int indice, String[] nomes, String[] destinos) {
        JButton botao = new JButton(new javax.swing.ImageIcon(Tema.menuComSeta(titulo, subtitulo)));
        botao.setOpaque(false);
        botao.setContentAreaFilled(false);
        botao.setFocusPainted(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setForeground(Tema.GRAFITE);
        JPopupMenu popup = new JPopupMenu();
        for (int i = 0; i < nomes.length; i++) {
            JMenuItem item = new JMenuItem(nomes[i]);
            String destino = destinos[i];
            item.addActionListener(e -> ir(destino));
            popup.add(item);
        }
        botao.addActionListener(e -> popup.show(botao, 0, botao.getHeight()));
        menu.add(botao);
        return botao;
    }

    @Override
    public void ir(String carta) {
        cartas.show(paginas, carta);
        if ("inicio".equals(carta)) {
            selecionarMenu(0);
            telas.atualizarInicio();
        } else if ("consultar".equals(carta) || "revisar".equals(carta)) {
            selecionarMenu(2);
            if ("revisar".equals(carta)) {
                telas.atualizarListas();
            }
        } else if ("sobre".equals(carta)) {
            selecionarMenu(3);
        } else {
            selecionarMenu(1);
            telas.atualizarListas();
        }
    }

    @Override
    public Banco banco() {
        return banco;
    }

    @Override
    public void sincronizar() {
        carregarAreas();
        listarCasos();
        atualizarStatus();
        telas.atualizarListas();
    }

    @Override
    public void avisar(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "AvembRBC", JOptionPane.WARNING_MESSAGE);
    }

    private void selecionarMenu(int indice) {
        for (int i = 0; i < menu.size(); i++) {
            JButton botao = menu.get(i);
            botao.setBorder(new EmptyBorder(4, 14, 4, 14));
            if (i == indice) {
                botao.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 3, 0, Tema.AZUL),
                        new EmptyBorder(4, 14, 1, 14)));
            }
        }
    }

    private JScrollPane pagina(JPanel conteudo) {
        conteudo.setOpaque(false);
        JPanel faixa = new JPanel(new BorderLayout());
        faixa.setOpaque(false);
        faixa.setBorder(new EmptyBorder(16, 16, 16, 16));
        faixa.add(conteudo, BorderLayout.NORTH);
        JScrollPane rolagem = new JScrollPane(faixa);
        rolagem.setBorder(null);
        rolagem.getViewport().setBackground(Tema.PAGINA);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        return rolagem;
    }

    private JPanel consulta() {
        JPanel corpo = new JPanel();
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.setBackground(Color.WHITE);

        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linha.setBackground(Color.WHITE);
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);
        linha.add(new JLabel("Área"));
        areas.setPreferredSize(new Dimension(260, 28));
        linha.add(areas);
        soValidos.setBackground(Color.WHITE);
        linha.add(soValidos);

        formulario.setBackground(Color.WHITE);
        formulario.setAlignmentX(Component.LEFT_ALIGNMENT);
        ajudaInicial.setAlignmentX(Component.LEFT_ALIGNMENT);
        ajudaCampos.setAlignmentX(Component.LEFT_ALIGNMENT);

        solucao.setLineWrap(true);
        solucao.setWrapStyleWord(true);
        solucao.setBorder(new EmptyBorder(6, 6, 6, 6));
        JScrollPane caixa = new JScrollPane(solucao);
        caixa.setAlignmentX(Component.LEFT_ALIGNMENT);
        caixa.setPreferredSize(new Dimension(100, 120));
        JLabel rotuloSolucao = new JLabel("Solução do problema");
        rotuloSolucao.setFont(rotuloSolucao.getFont().deriveFont(Font.BOLD));
        rotuloSolucao.setAlignmentX(Component.LEFT_ALIGNMENT);
        metaSolucao.setForeground(Tema.AZUL);
        metaSolucao.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton pesquisar = botao("Pesquisar");
        pesquisar.addActionListener(e -> buscar());
        reter.addActionListener(e -> reter());
        JButton cancelar = botaoSecundario("Cancelar");
        cancelar.addActionListener(e -> cancelar());
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acoes.setBackground(Color.WHITE);
        acoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        acoes.add(pesquisar);
        acoes.add(reter);
        acoes.add(cancelar);

        corpo.add(linha);
        corpo.add(Box.createVerticalStrut(8));
        corpo.add(ajudaInicial);
        corpo.add(formulario);
        corpo.add(Box.createVerticalStrut(8));
        corpo.add(ajudaCampos);
        corpo.add(Box.createVerticalStrut(12));
        corpo.add(rotuloSolucao);
        corpo.add(Box.createVerticalStrut(4));
        corpo.add(caixa);
        corpo.add(Box.createVerticalStrut(4));
        corpo.add(metaSolucao);
        corpo.add(Box.createVerticalStrut(8));
        corpo.add(acoes);

        JPanel resultadosCorpo = (JPanel) ((BorderLayout) blocoResultados.getLayout() == null
                ? blocoResultados.getComponent(1) : ((JPanel) blocoResultados.getComponent(1)));
        montarResultados(resultadosCorpo);
        blocoResultados.setVisible(false);
        blocoResultados.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel coluna = new JPanel();
        coluna.setLayout(new BoxLayout(coluna, BoxLayout.Y_AXIS));
        coluna.setOpaque(false);
        JPanel pesquisa = cartao("Pesquisa de casos", corpo);
        pesquisa.setAlignmentX(Component.LEFT_ALIGNMENT);
        coluna.add(pesquisa);
        coluna.add(Box.createVerticalStrut(16));
        coluna.add(blocoResultados);
        return coluna;
    }

    private void montarResultados(JPanel corpo) {
        corpo.setBackground(Color.WHITE);
        vazio.setForeground(Tema.GRAFITE);
        JButton aplicar = botao("Aplicar");
        aplicar.addActionListener(e -> aplicar());
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setBackground(Color.WHITE);
        barra.add(aplicar);
        barra.add(vazio);
        resultados.setPreferredScrollableViewportSize(new Dimension(100, 160));
        comparacao.setPreferredScrollableViewportSize(new Dimension(100, 120));
        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setBackground(Color.WHITE);
        centro.add(new JScrollPane(resultados), BorderLayout.NORTH);
        centro.add(new JScrollPane(comparacao), BorderLayout.CENTER);
        corpo.add(barra, BorderLayout.NORTH);
        corpo.add(centro, BorderLayout.CENTER);
    }

    private JPanel painelCasos() {
        JPanel corpo = new JPanel(new BorderLayout(0, 8));
        corpo.setBackground(Color.WHITE);
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acoes.setBackground(Color.WHITE);
        JButton validar = botao("Validar");
        validar.addActionListener(e -> alterarCaso(true));
        JButton excluir = botaoSecundario("Excluir");
        excluir.addActionListener(e -> alterarCaso(false));
        acoes.add(validar);
        acoes.add(excluir);
        casos.setPreferredScrollableViewportSize(new Dimension(100, 420));
        corpo.add(acoes, BorderLayout.NORTH);
        corpo.add(new JScrollPane(casos), BorderLayout.CENTER);
        return cartao("Manter casos", corpo);
    }

    private JPanel sobre() {
        JTextArea area = ajuda("");
        area.setText("O AvembRBC apoia a decisão de quem acompanha aviário. O ciclo recupera os casos parecidos, "
                + "reutiliza a solução trocando os valores do caso antigo pelos da consulta, deixa a revisão com uma pessoa "
                + "e retém o caso novo como pendente até a validação.\n\n"
                + "A similaridade global é a média ponderada das similaridades locais. Número usa a distância escalar dentro da faixa "
                + "conhecida do atributo. Texto usa a proporção de palavras em comum, depois de unificar sinônimos "
                + "(Gumboro e doença infecciosa da bursa, Newcastle, bouba e varíola, ascite e barriga d'água). "
                + "Caso com similaridade 0 não aparece. A lista traz no máximo oito casos.\n\n"
                + "A base inicial reúne problemas e condutas publicados pela Embrapa. O projeto de TCC da UNC que originou esta ideia "
                + "não foi localizado nos repositórios públicos.\n\n"
                + "As cores seguem o manual da Embrapa: verde #00783E e azul #29428C, sobre branco. "
                + "Isto não é um sistema oficial da Embrapa e não substitui o médico veterinário.\n\n"
                + "O banco padrão é o H2 em memória. PostgreSQL fica no menu Banco, em localhost:5432, banco avembrbc, usuário e senha postgres.");
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        return cartao("Sobre o sistema", area);
    }

    private void montarFormulario() {
        formulario.removeAll();
        entradas.clear();
        Banco.Area area = (Banco.Area) areas.getSelectedItem();
        boolean tem = area != null;
        ajudaInicial.setVisible(!tem);
        formulario.setVisible(tem);
        ajudaCampos.setVisible(tem);
        if (!tem) {
            formulario.revalidate();
            limparConsulta();
            return;
        }
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.weightx = 0.34;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        formulario.add(celula(rotuloTabela("Campo"), true), g);
        g.gridx = 1;
        g.weightx = 0.66;
        formulario.add(celula(rotuloTabela("Descrição — digite ou selecione"), true), g);
        int linha = 1;
        for (Banco.Campo campo : banco.campos(area.id)) {
            g.gridy = linha;
            g.gridx = 0;
            g.weightx = 0.34;
            formulario.add(celula(new JLabel(campo.nome + "  (" + campo.peso + ")"), false), g);
            JComboBox<String> combo = combo(banco.sugestoes(campo.id));
            g.gridx = 1;
            g.weightx = 0.66;
            formulario.add(celula(combo, false), g);
            entradas.put(campo.id, combo);
            linha++;
        }
        formulario.revalidate();
        formulario.repaint();
        limparConsulta();
    }

    private void buscar() {
        Banco.Area area = (Banco.Area) areas.getSelectedItem();
        if (area == null) {
            JOptionPane.showMessageDialog(this, "Escolha a área.", "AvembRBC", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            achados = Motor.recuperar(banco, area.id, lerProblema(), soValidos.isSelected());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "AvembRBC", JOptionPane.WARNING_MESSAGE);
            return;
        }
        aplicado = null;
        modeloResultados.setRowCount(0);
        List<Banco.Campo> campos = banco.campos(area.id);
        int numero = 1;
        for (Motor.Recuperado item : achados) {
            modeloResultados.addRow(new Object[] {
                    numero++,
                    String.format(Locale.forLanguageTag("pt-BR"), "%.0f%%", item.similaridade * 100),
                    "nº " + item.caso.id,
                    item.caso.valor("Quadro", campos),
                    item.caso.valido ? "Validado" : "Pendente"
            });
        }
        boolean tem = !achados.isEmpty();
        vazio.setVisible(!tem);
        resultados.setVisible(tem);
        comparacao.setVisible(tem);
        modeloComparacao.setRowCount(0);
        blocoResultados.setVisible(true);
        blocoResultados.revalidate();
        if (tem) {
            resultados.setRowSelectionInterval(0, 0);
        }
    }

    private void mostrarComparacao() {
        int linha = resultados.getSelectedRow();
        modeloComparacao.setRowCount(0);
        if (linha < 0 || linha >= achados.size()) {
            return;
        }
        for (Motor.Avaliado avaliado : achados.get(linha).avaliados) {
            modeloComparacao.addRow(new Object[] {
                    avaliado.nome,
                    avaliado.peso,
                    avaliado.consulta == null ? "—" : avaliado.consulta,
                    avaliado.caso == null ? "—" : avaliado.caso,
                    String.format(Locale.forLanguageTag("pt-BR"), "%.0f%%", avaliado.sim * 100)
            });
        }
    }

    private void aplicar() {
        int linha = resultados.getSelectedRow();
        if (linha < 0 || linha >= achados.size()) {
            JOptionPane.showMessageDialog(this, "Selecione um caso para aplicar.", "AvembRBC", JOptionPane.WARNING_MESSAGE);
            return;
        }
        aplicado = achados.get(linha);
        solucao.setText(aplicado.solucaoAdaptada);
        solucao.setCaretPosition(0);
        String troca = aplicado.trocas == null || aplicado.trocas.isEmpty() ? "Sem substituição de valor." : aplicado.trocas;
        metaSolucao.setText("Reutilizado do caso nº " + aplicado.caso.id + " ("
                + String.format(Locale.forLanguageTag("pt-BR"), "%.0f%%", aplicado.similaridade * 100)
                + "). " + aplicado.caso.fonte + " " + troca);
        reter.setEnabled(true);
    }

    private void reter() {
        Banco.Area area = (Banco.Area) areas.getSelectedItem();
        String texto = solucao.getText() == null ? "" : solucao.getText().trim();
        if (area == null || texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Descreva a solução antes de reter.", "AvembRBC", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (aplicado == null) {
                banco.reter(area.id, lerProblema(), texto, null, null, "");
            } else {
                banco.reter(area.id, lerProblema(), texto, aplicado.caso.id, aplicado.similaridade, aplicado.trocas);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "AvembRBC", JOptionPane.WARNING_MESSAGE);
            return;
        }
        listarCasos();
        atualizarStatus();
        JOptionPane.showMessageDialog(this, "Caso retido, pendente de validação.", "AvembRBC", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelar() {
        for (JComboBox<String> combo : entradas.values()) {
            combo.setSelectedItem("");
            JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
            editor.setForeground(DICA_COR);
            editor.setText(DICA);
        }
        solucao.setText("");
        limparConsulta();
    }

    private void alterarCaso(boolean validar) {
        int linha = casos.getSelectedRow();
        if (linha < 0) {
            return;
        }
        long id = ((Number) modeloCasos.getValueAt(linha, 0)).longValue();
        if (!validar) {
            int ok = JOptionPane.showConfirmDialog(this, "Excluir o caso " + id + "?", "AvembRBC", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) {
                return;
            }
            banco.excluir(id);
        } else {
            banco.validar(id);
        }
        listarCasos();
        atualizarStatus();
    }

    private void usarH2() {
        trocar(Banco.h2Quiet());
    }

    private void usarPostgres() {
        JTextField host = new JTextField("localhost", 16);
        JTextField porta = new JTextField("5432", 6);
        JTextField nome = new JTextField("avembrbc", 16);
        JTextField usuario = new JTextField("postgres", 16);
        JPasswordField senha = new JPasswordField("postgres", 16);
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.add(rotulo("Servidor", host));
        painel.add(rotulo("Porta", porta));
        painel.add(rotulo("Banco", nome));
        painel.add(rotulo("Usuário", usuario));
        painel.add(rotulo("Senha", senha));
        int ok = JOptionPane.showConfirmDialog(this, painel, "PostgreSQL", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            trocar(Banco.postgres(host.getText().trim(), porta.getText().trim(), nome.getText().trim(),
                    usuario.getText().trim(), new String(senha.getPassword())));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Não conectou. O banco precisa existir. " + ex.getMessage(),
                    "AvembRBC", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void trocar(Banco novo) {
        Banco antigo = banco;
        banco = novo;
        antigo.close();
        carregarAreas();
        listarCasos();
        atualizarStatus();
        telas.atualizarListas();
        limparConsulta();
    }

    private void carregarAreas() {
        ajustando = true;
        areas.removeAllItems();
        areas.addItem(null);
        for (Banco.Area area : banco.areas()) {
            areas.addItem(area);
        }
        areas.setSelectedIndex(0);
        ajustando = false;
        montarFormulario();
    }

    private void listarCasos() {
        modeloCasos.setRowCount(0);
        for (Banco.Linha linha : banco.listar()) {
            modeloCasos.addRow(new Object[] {
                    linha.id, linha.area, linha.quadro == null ? "" : linha.quadro,
                    linha.valido ? "Validado" : "Pendente", linha.fonte
            });
        }
    }

    private void atualizarStatus() {
        String guarda = banco.memoria ? " · os casos novos se perdem ao fechar" : " · os casos ficam gravados";
        String texto = banco.descricao + " · " + banco.contar() + " casos" + guarda;
        status.setText(texto);
        statusBanco.setText(banco.descricao);
    }

    private Map<Long, String> lerProblema() {
        Map<Long, String> problema = new LinkedHashMap<Long, String>();
        for (Map.Entry<Long, JComboBox<String>> entrada : entradas.entrySet()) {
            problema.put(entrada.getKey(), textoCombo(entrada.getValue()));
        }
        return problema;
    }

    private void limparConsulta() {
        achados = new ArrayList<Motor.Recuperado>();
        aplicado = null;
        modeloResultados.setRowCount(0);
        modeloComparacao.setRowCount(0);
        metaSolucao.setText(" ");
        reter.setEnabled(solucao.getText() != null && !solucao.getText().trim().isEmpty());
        blocoResultados.setVisible(false);
    }

    private void prepararSolucao() {
        solucao.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                reter.setEnabled(!solucao.getText().trim().isEmpty());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reter.setEnabled(!solucao.getText().trim().isEmpty());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reter.setEnabled(!solucao.getText().trim().isEmpty());
            }
        });
        reter.setEnabled(false);
    }

    private static String textoCombo(JComboBox<String> combo) {
        Object item = combo.getEditor().getItem();
        String texto = item == null ? "" : item.toString().trim();
        if (texto.isEmpty() || DICA.equals(texto)) {
            return "";
        }
        return texto;
    }

    private static JComboBox<String> combo(List<String> sugestoes) {
        JComboBox<String> combo = new JComboBox<String>();
        combo.setEditable(true);
        combo.setMaximumRowCount(12);
        combo.setPrototypeDisplayValue("Digite ou selecione um valor da lista");
        combo.addItem("");
        for (String sugestao : sugestoes) {
            combo.addItem(sugestao);
        }
        JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
        editor.setForeground(DICA_COR);
        editor.setText(DICA);
        editor.setToolTipText(DICA);
        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (DICA.equals(editor.getText())) {
                    editor.setText("");
                    editor.setForeground(Tema.TEXTO);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (editor.getText().trim().isEmpty()) {
                    editor.setForeground(DICA_COR);
                    editor.setText(DICA);
                }
            }
        });
        combo.addActionListener(e -> {
            Object escolhido = combo.getSelectedItem();
            if (escolhido != null && !escolhido.toString().isEmpty() && !DICA.equals(escolhido.toString())) {
                editor.setForeground(Tema.TEXTO);
            }
        });
        return combo;
    }

    private static JLabel rotuloTabela(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private static JPanel celula(JComponent componente, boolean cabecalho) {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(cabecalho ? Tema.VERDE : Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 1, Tema.BORDA),
                new EmptyBorder(6, 8, 6, 8)));
        painel.add(componente, BorderLayout.CENTER);
        return painel;
    }

    private static JTextArea ajuda(String texto) {
        JTextArea area = new JTextArea(texto);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setForeground(new Color(0x3E5166));
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setBorder(new EmptyBorder(4, 0, 4, 0));
        return area;
    }

    private static JPanel cartao(String titulo, JComponent corpo) {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createLineBorder(Tema.BORDA));
        JLabel faixa = new JLabel(titulo);
        faixa.setOpaque(true);
        faixa.setBackground(Tema.AZUL);
        faixa.setForeground(Color.WHITE);
        faixa.setFont(faixa.getFont().deriveFont(Font.BOLD));
        faixa.setBorder(new EmptyBorder(8, 12, 8, 12));
        corpo.setBorder(new EmptyBorder(12, 12, 12, 12));
        painel.add(faixa, BorderLayout.NORTH);
        painel.add(corpo, BorderLayout.CENTER);
        return painel;
    }

    private static JPanel rotulo(String nome, JTextField campo) {
        JPanel linha = new JPanel(new BorderLayout(8, 4));
        JLabel label = new JLabel(nome);
        label.setPreferredSize(new Dimension(80, 24));
        linha.add(label, BorderLayout.WEST);
        linha.add(campo, BorderLayout.CENTER);
        linha.setBorder(new EmptyBorder(2, 0, 2, 0));
        return linha;
    }

    private static JButton botao(String texto) {
        JButton botao = new JButton(texto);
        botao.setBackground(Tema.VERDE);
        botao.setForeground(Color.WHITE);
        botao.setOpaque(true);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.VERDE_ESCURO),
                new EmptyBorder(6, 14, 6, 14)));
        return botao;
    }

    private static JButton botaoSecundario(String texto) {
        JButton botao = new JButton(texto);
        botao.setBackground(Color.WHITE);
        botao.setForeground(Tema.AZUL);
        botao.setOpaque(true);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.AZUL),
                new EmptyBorder(6, 14, 6, 14)));
        return botao;
    }

    private static DefaultTableModel modelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static JTable tabela(DefaultTableModel modelo) {
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(26);
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionBackground(Tema.GELO);
        tabela.setSelectionForeground(Tema.TEXTO);
        tabela.setGridColor(Tema.BORDA);
        tabela.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel celula = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                celula.setBackground(Tema.VERDE);
                celula.setForeground(Color.WHITE);
                celula.setFont(celula.getFont().deriveFont(Font.BOLD));
                celula.setOpaque(true);
                celula.setBorder(new EmptyBorder(4, 6, 4, 6));
                return celula;
            }
        });
        tabela.getTableHeader().setBackground(Tema.VERDE);
        return tabela;
    }
}
