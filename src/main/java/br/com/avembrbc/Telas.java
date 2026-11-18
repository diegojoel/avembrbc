package br.com.avembrbc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

final class Telas {

    interface App {
        Banco banco();

        void ir(String carta);

        void sincronizar();

        void avisar(String mensagem);
    }

    final JPanel inicio = new JPanel();
    final JPanel areas = new JPanel(new BorderLayout());
    final JPanel campos = new JPanel(new BorderLayout());
    final JPanel dicionario = new JPanel(new BorderLayout());
    final JPanel similaridade = new JPanel(new BorderLayout());
    final JPanel revisar = new JPanel(new BorderLayout());

    private final App app;
    private final JLabel[] numeros = new JLabel[5];
    private final DefaultTableModel modeloAreas = modelo("Código", "Descrição");
    private final JTable tabelaAreas = tabela(modeloAreas);
    private final JTextField nomeArea = new JTextField(24);
    private Long areaId;

    private final JComboBox<Banco.Area> areaCampo = new JComboBox<Banco.Area>();
    private final JTextField nomeCampo = new JTextField(18);
    private final JComboBox<String> tipoCampo = new JComboBox<String>(new String[] {"TEXTO", "NUMERICO"});
    private final JTextField pesoCampo = new JTextField("1", 4);
    private final DefaultTableModel modeloCampos = modelo("Código", "Campo", "Tipo", "Peso");
    private final JTable tabelaCampos = tabela(modeloCampos);
    private Long campoId;

    private final JTextField chave = new JTextField(24);
    private final JTextArea equivalentes = new JTextArea(5, 24);
    private final DefaultTableModel modeloPalavras = modelo("Palavra-chave", "Equivalentes");
    private final JTable tabelaPalavras = tabela(modeloPalavras);
    private Long palavraId;

    private final JComboBox<Item> campoPar = new JComboBox<Item>();
    private final JComboBox<String> valorConsulta = new JComboBox<String>();
    private final JComboBox<String> valorCaso = new JComboBox<String>();
    private final JTextField percentual = new JTextField("100", 4);
    private final DefaultTableModel modeloPares = modelo("Campo", "Consulta", "Caso", "Similaridade", "Id");
    private final JTable tabelaPares = tabela(modeloPares);

    private final JComboBox<Banco.Area> filtroRevisao = new JComboBox<Banco.Area>();
    private final JCheckBox soPendentes = new JCheckBox("Somente pendentes de revisão", true);
    private final DefaultTableModel modeloRevisao = modelo("Caso", "Área", "Situação", "Solução");
    private final JTable tabelaRevisao = tabela(modeloRevisao);
    private final JTextArea descricao = texto(false);
    private final JTextArea solucaoRevisao = texto(true);
    private final JCheckBox validado = new JCheckBox("Caso validado");
    private final JTextArea complemento = texto(true);
    private final DefaultTableModel modeloComplementos = modelo("Complemento");
    private final JTable tabelaComplementos = tabela(modeloComplementos);
    private final JLabel origem = new JLabel(" ");
    private List<Banco.Revisao> revisoes = java.util.Collections.emptyList();

    Telas(App app) {
        this.app = app;
        montarInicio();
        montarAreas();
        montarCampos();
        montarDicionario();
        montarSimilaridade();
        montarRevisao();
        atualizarInicio();
    }

    void atualizarInicio() {
        Banco banco = app.banco();
        int total = banco.contar();
        numeros[0].setText(String.valueOf(total));
        numeros[1].setText(String.valueOf(banco.contarValidos()));
        numeros[2].setText(String.valueOf(total - banco.contarValidos()));
        numeros[3].setText(String.valueOf(banco.contarAreas()));
        numeros[4].setText(String.valueOf(banco.contarChaves()));
    }

    void atualizarListas() {
        recarregarAreas(areaCampo);
        recarregarAreas(filtroRevisao);
        listarAreas();
        listarCampos();
        listarPalavras();
        listarPares();
        listarRevisao();
        atualizarInicio();
    }

    private void montarInicio() {
        inicio.setLayout(new BoxLayout(inicio, BoxLayout.Y_AXIS));
        inicio.setOpaque(false);
        JLabel titulo = new JLabel("Bem-vindo ao AvembRBC");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(Tema.GRAFITE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea intro = nota("O AvembRBC apoia a decisão em avicultura. Um caso é a descrição do problema "
                + "(atributos da área, cada um com peso) mais a solução. A busca percorre os casos dessa área "
                + "e devolve os mais parecidos.");
        JPanel indicadores = new JPanel(new GridLayout(1, 5, 8, 0));
        indicadores.setOpaque(false);
        indicadores.setAlignmentX(Component.LEFT_ALIGNMENT);
        indicadores.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        String[] legendas = {"casos na base", "casos validados", "pendentes de revisão", "áreas configuradas", "palavras no dicionário"};
        for (int i = 0; i < legendas.length; i++) {
            numeros[i] = new JLabel("0", SwingConstants.CENTER);
            numeros[i].setFont(new Font("Segoe UI", Font.BOLD, 26));
            numeros[i].setForeground(Tema.VERDE);
            JLabel legenda = new JLabel(legendas[i], SwingConstants.CENTER);
            legenda.setForeground(Tema.AZUL);
            JPanel caixa = new JPanel(new BorderLayout());
            caixa.setBackground(Color.WHITE);
            caixa.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Tema.AZUL),
                    new EmptyBorder(8, 6, 8, 6)));
            caixa.add(numeros[i], BorderLayout.CENTER);
            caixa.add(legenda, BorderLayout.SOUTH);
            indicadores.add(caixa);
        }
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acoes.setOpaque(false);
        acoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton pesquisar = botao("Pesquisar solução para um novo problema");
        pesquisar.addActionListener(e -> app.ir("consultar"));
        JButton revisar = botaoSecundario("Revisar casos");
        revisar.addActionListener(e -> app.ir("revisar"));
        acoes.add(pesquisar);
        acoes.add(revisar);

        JLabel cicloTitulo = tituloMenor("Ciclo do raciocínio baseado em casos");
        JPanel ciclo = new JPanel(new GridLayout(2, 2, 8, 8));
        ciclo.setOpaque(false);
        ciclo.setAlignmentX(Component.LEFT_ALIGNMENT);
        ciclo.add(cartaoMini("1. Recuperar", "Cada atributo recebe uma similaridade local. A global é a média ponderada: "
                + "soma de peso × similaridade, dividida pela soma dos pesos. Campo em branco entra com 0 e o peso continua na conta. "
                + "A lista traz até 8 casos. Empate prefere o caso validado e, depois, o mais recente. Percentual 0 não aparece."));
        ciclo.add(cartaoMini("2. Reutilizar", "Aplicar copia a solução do caso escolhido. Onde o texto cita um valor do caso antigo, "
                + "esse valor é trocado pelo do problema novo. Palavra que o dicionário trata como igual não é trocada."));
        ciclo.add(cartaoMini("3. Revisar", "O caso retido nasce pendente. Na revisão dá para ajustar a solução, acrescentar um complemento "
                + "e marcar o caso como validado. Só então ele pesa como caso confirmado quando a busca pede somente validados."));
        ciclo.add(cartaoMini("4. Reter", "Grava a descrição e a solução. Se a solução veio de um caso recuperado, ficam guardados "
                + "o caso de origem, a similaridade e as trocas. Uma descrição já existente com a mesma solução não é gravada de novo."));
        JLabel compara = tituloMenor("Como cada atributo é comparado");
        JPanel medidas = new JPanel(new GridLayout(1, 3, 8, 8));
        medidas.setOpaque(false);
        medidas.setAlignmentX(Component.LEFT_ALIGNMENT);
        medidas.add(cartaoMini("Número", "Medida escalar: 1 − |a − b| / (máximo − mínimo) dos valores desse atributo na área, "
                + "incluindo o problema novo. Idade ou temperatura próximas ficam com percentual alto."));
        medidas.add(cartaoMini("Texto", "Contagem de palavras em comum, depois de tirar acento e palavra irrelevante. "
                + "O dicionário une sinônimos: Gumboro e doença infecciosa da bursa contam como a mesma palavra."));
        medidas.add(cartaoMini("Tabela", "Se o atributo tem pares em Similaridade local, a busca usa essa tabela no lugar do texto. "
                + "O par vale só no sentido consulta → caso. Sem par para aquele atributo, vale número ou texto. "
                + "A base já cobre espécie, sistema, fase e agente, com alguns pares entre valores próximos."));
        inicio.add(titulo);
        inicio.add(Box.createVerticalStrut(8));
        inicio.add(intro);
        inicio.add(Box.createVerticalStrut(12));
        inicio.add(indicadores);
        inicio.add(Box.createVerticalStrut(12));
        inicio.add(acoes);
        inicio.add(Box.createVerticalStrut(16));
        inicio.add(cicloTitulo);
        inicio.add(Box.createVerticalStrut(8));
        inicio.add(ciclo);
        inicio.add(Box.createVerticalStrut(16));
        inicio.add(compara);
        inicio.add(Box.createVerticalStrut(8));
        inicio.add(medidas);
    }

    private void montarAreas() {
        tabelaAreas.getSelectionModel().addListSelectionListener(e -> {
            int linha = tabelaAreas.getSelectedRow();
            if (linha < 0) {
                return;
            }
            areaId = ((Number) modeloAreas.getValueAt(linha, 0)).longValue();
            nomeArea.setText(String.valueOf(modeloAreas.getValueAt(linha, 1)));
        });
        JButton novo = botao("Nova área");
        novo.addActionListener(e -> criarArea());
        nomeArea.addActionListener(e -> criarArea());
        JButton salvar = botao("Salvar");
        salvar.addActionListener(e -> executar(() -> {
            app.banco().salvarArea(areaId, nomeArea.getText());
            areaId = null;
            nomeArea.setText("");
            listarAreas();
            app.sincronizar();
        }));
        JButton excluir = botaoSecundario("Excluir");
        excluir.addActionListener(e -> executar(() -> {
            if (areaId == null) {
                return;
            }
            app.banco().excluirArea(areaId);
            areaId = null;
            nomeArea.setText("");
            listarAreas();
            app.sincronizar();
        }));
        JPanel form = linha(new JLabel("Descrição"), nomeArea, novo, salvar, excluir);
        areas.add(cartao("Áreas da avicultura", corpo(form, tabelaAreas, 360)), BorderLayout.CENTER);
        listarAreas();
    }

    private void criarArea() {
        executar(() -> {
            String nome = nomeArea.getText().trim();
            int linha = tabelaAreas.getSelectedRow();
            String atual = linha >= 0 ? String.valueOf(modeloAreas.getValueAt(linha, 1)) : null;
            if (nome.isEmpty() || nome.equals(atual)) {
                areaId = null;
                nomeArea.setText("");
                tabelaAreas.clearSelection();
                nomeArea.requestFocusInWindow();
                return;
            }
            app.banco().salvarArea(null, nome);
            areaId = null;
            nomeArea.setText("");
            tabelaAreas.clearSelection();
            listarAreas();
            app.sincronizar();
            nomeArea.requestFocusInWindow();
        });
    }

    private void montarCampos() {
        areaCampo.addActionListener(e -> listarCampos());
        tabelaCampos.getSelectionModel().addListSelectionListener(e -> {
            int linha = tabelaCampos.getSelectedRow();
            if (linha < 0) {
                return;
            }
            campoId = ((Number) modeloCampos.getValueAt(linha, 0)).longValue();
            nomeCampo.setText(String.valueOf(modeloCampos.getValueAt(linha, 1)));
            tipoCampo.setSelectedItem(modeloCampos.getValueAt(linha, 2));
            pesoCampo.setText(String.valueOf(modeloCampos.getValueAt(linha, 3)));
        });
        JButton novo = botao("Novo campo");
        novo.addActionListener(e -> criarCampo());
        nomeCampo.addActionListener(e -> criarCampo());
        JButton salvar = botao("Salvar");
        salvar.addActionListener(e -> executar(() -> {
            Banco.Area area = (Banco.Area) areaCampo.getSelectedItem();
            if (area == null) {
                throw new IllegalArgumentException("Escolha a área.");
            }
            app.banco().salvarCampo(campoId, area.id, nomeCampo.getText(), String.valueOf(tipoCampo.getSelectedItem()),
                    Integer.parseInt(pesoCampo.getText().trim()));
            campoId = null;
            nomeCampo.setText("");
            listarCampos();
        }));
        JButton excluir = botaoSecundario("Excluir");
        excluir.addActionListener(e -> executar(() -> {
            if (campoId == null) {
                return;
            }
            app.banco().excluirCampo(campoId);
            campoId = null;
            nomeCampo.setText("");
            listarCampos();
        }));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        form.setBackground(Color.WHITE);
        form.add(new JLabel("Área"));
        areaCampo.setPreferredSize(new Dimension(180, 28));
        form.add(areaCampo);
        form.add(new JLabel("Campo"));
        form.add(nomeCampo);
        form.add(new JLabel("Tipo"));
        form.add(tipoCampo);
        form.add(new JLabel("Peso"));
        form.add(pesoCampo);
        form.add(novo);
        form.add(salvar);
        form.add(excluir);
        JTextArea ajuda = nota("Aqui se configura a descrição do problema de cada área: quais campos entram na busca e o peso de cada um.");
        JPanel miolo = new JPanel(new BorderLayout(0, 8));
        miolo.setBackground(Color.WHITE);
        miolo.add(ajuda, BorderLayout.NORTH);
        miolo.add(form, BorderLayout.CENTER);
        campos.add(cartao("Campos e pesos", corpo(miolo, tabelaCampos, 360)), BorderLayout.CENTER);
    }

    private void criarCampo() {
        executar(() -> {
            Banco.Area area = (Banco.Area) areaCampo.getSelectedItem();
            if (area == null) {
                throw new IllegalArgumentException("Escolha a área.");
            }
            String nome = nomeCampo.getText().trim();
            String tipo = String.valueOf(tipoCampo.getSelectedItem());
            String peso = pesoCampo.getText().trim();
            int linha = tabelaCampos.getSelectedRow();
            boolean igual = linha >= 0
                    && nome.equals(String.valueOf(modeloCampos.getValueAt(linha, 1)))
                    && tipo.equals(String.valueOf(modeloCampos.getValueAt(linha, 2)))
                    && peso.equals(String.valueOf(modeloCampos.getValueAt(linha, 3)));
            if (nome.isEmpty() || igual) {
                campoId = null;
                nomeCampo.setText("");
                pesoCampo.setText("1");
                tabelaCampos.clearSelection();
                nomeCampo.requestFocusInWindow();
                return;
            }
            int valor;
            try {
                valor = Integer.parseInt(peso);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("O peso começa em 1.");
            }
            app.banco().salvarCampo(null, area.id, nome, tipo, valor);
            campoId = null;
            nomeCampo.setText("");
            pesoCampo.setText("1");
            tabelaCampos.clearSelection();
            listarCampos();
            app.sincronizar();
            nomeCampo.requestFocusInWindow();
        });
    }

    private void montarDicionario() {
        equivalentes.setLineWrap(true);
        equivalentes.setWrapStyleWord(true);
        tabelaPalavras.getSelectionModel().addListSelectionListener(e -> {
            int linha = tabelaPalavras.getSelectedRow();
            if (linha < 0) {
                return;
            }
            for (Banco.Palavra palavra : app.banco().palavras()) {
                if (palavra.chave.equals(modeloPalavras.getValueAt(linha, 0))) {
                    palavraId = palavra.id;
                    chave.setText(palavra.chave);
                    equivalentes.setText(palavra.equivalentes);
                    break;
                }
            }
        });
        JButton novo = botao("Nova palavra-chave");
        novo.addActionListener(e -> {
            palavraId = null;
            chave.setText("");
            equivalentes.setText("");
            tabelaPalavras.clearSelection();
        });
        JButton salvar = botao("Salvar");
        salvar.addActionListener(e -> executar(() -> {
            app.banco().salvarPalavra(palavraId, chave.getText(), equivalentes.getText());
            palavraId = null;
            chave.setText("");
            equivalentes.setText("");
            listarPalavras();
            atualizarInicio();
        }));
        JButton excluir = botaoSecundario("Excluir");
        excluir.addActionListener(e -> executar(() -> {
            if (palavraId == null) {
                return;
            }
            app.banco().excluirPalavra(palavraId);
            palavraId = null;
            chave.setText("");
            equivalentes.setText("");
            listarPalavras();
            atualizarInicio();
        }));
        JPanel form = new JPanel(new BorderLayout(8, 8));
        form.setBackground(Color.WHITE);
        JPanel topo = linha(new JLabel("Palavra-chave"), chave, novo, salvar, excluir);
        form.add(topo, BorderLayout.NORTH);
        JPanel baixo = new JPanel(new BorderLayout(8, 0));
        baixo.setBackground(Color.WHITE);
        baixo.add(new JLabel("Equivalentes, uma por linha"), BorderLayout.NORTH);
        baixo.add(new JScrollPane(equivalentes), BorderLayout.CENTER);
        form.add(baixo, BorderLayout.CENTER);
        JTextArea ajuda = nota("Na busca, a palavra-chave e as equivalentes contam como a mesma palavra. "
                + "Ex.: Gumboro equivale a doença infecciosa da bursa.");
        JPanel miolo = new JPanel(new BorderLayout(0, 8));
        miolo.setBackground(Color.WHITE);
        miolo.add(ajuda, BorderLayout.NORTH);
        miolo.add(form, BorderLayout.CENTER);
        dicionario.add(cartao("Dicionário de dados", corpo(miolo, tabelaPalavras, 240)), BorderLayout.CENTER);
        listarPalavras();
    }

    private void montarSimilaridade() {
        valorConsulta.setEditable(true);
        valorCaso.setEditable(true);
        valorConsulta.setPrototypeDisplayValue("Digite ou selecione");
        valorCaso.setPrototypeDisplayValue("Digite ou selecione");
        campoPar.addActionListener(e -> encherSugestoes());
        JButton salvar = botao("Salvar par");
        salvar.addActionListener(e -> executar(() -> {
            Item item = (Item) campoPar.getSelectedItem();
            if (item == null) {
                throw new IllegalArgumentException("Escolha o campo.");
            }
            app.banco().salvarPar(item.id, texto(valorConsulta), texto(valorCaso), Integer.parseInt(percentual.getText().trim()));
            listarPares();
            encherSugestoes();
        }));
        JButton excluir = botaoSecundario("Excluir");
        excluir.addActionListener(e -> executar(() -> {
            int linha = tabelaPares.getSelectedRow();
            if (linha < 0) {
                return;
            }
            app.banco().excluirPar(((Number) modeloPares.getValueAt(linha, 4)).longValue());
            listarPares();
        }));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        form.setBackground(Color.WHITE);
        form.add(new JLabel("Campo"));
        campoPar.setPreferredSize(new Dimension(220, 28));
        form.add(campoPar);
        form.add(new JLabel("Valor na consulta"));
        form.add(valorConsulta);
        form.add(new JLabel("Valor no caso"));
        form.add(valorCaso);
        form.add(new JLabel("%"));
        form.add(percentual);
        form.add(salvar);
        form.add(excluir);
        JTextArea ajuda = nota("Para um atributo com pares, a busca usa esta tabela no lugar da contagem de palavras. "
                + "O par vale só no sentido consulta → caso. O sentido inverso é outro par. Dá para digitar um valor novo.");
        JPanel miolo = new JPanel(new BorderLayout(0, 8));
        miolo.setBackground(Color.WHITE);
        miolo.add(ajuda, BorderLayout.NORTH);
        miolo.add(form, BorderLayout.CENTER);
        similaridade.add(cartao("Similaridade local", corpo(miolo, tabelaPares, 320)), BorderLayout.CENTER);
    }

    private void montarRevisao() {
        filtroRevisao.addActionListener(e -> listarRevisao());
        soPendentes.addActionListener(e -> listarRevisao());
        soPendentes.setBackground(Color.WHITE);
        tabelaRevisao.getSelectionModel().addListSelectionListener(e -> mostrarRevisao());
        tabelaRevisao.setPreferredScrollableViewportSize(new Dimension(100, 180));
        JButton salvar = botao("Salvar revisão");
        salvar.addActionListener(e -> executar(() -> {
            Banco.Revisao item = selecionada();
            if (item == null) {
                return;
            }
            app.banco().salvarRevisao(item.id, solucaoRevisao.getText(), validado.isSelected());
            listarRevisao();
            app.sincronizar();
            atualizarInicio();
        }));
        JButton acrescentar = botaoSecundario("Acrescentar complemento");
        acrescentar.addActionListener(e -> executar(() -> {
            Banco.Revisao item = selecionada();
            if (item == null) {
                return;
            }
            app.banco().complementar(item.id, complemento.getText());
            complemento.setText("");
            mostrarRevisao();
        }));
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtro.setBackground(Color.WHITE);
        filtro.add(new JLabel("Área"));
        filtroRevisao.setPreferredSize(new Dimension(180, 28));
        filtro.add(filtroRevisao);
        filtro.add(soPendentes);
        JPanel detalhe = new JPanel(new GridLayout(1, 2, 8, 0));
        detalhe.setBackground(Color.WHITE);
        detalhe.setPreferredSize(new Dimension(100, 220));
        JPanel esquerda = new JPanel(new BorderLayout(0, 4));
        esquerda.setBackground(Color.WHITE);
        esquerda.add(new JLabel("Descrição do problema"), BorderLayout.NORTH);
        esquerda.add(new JScrollPane(descricao), BorderLayout.CENTER);
        esquerda.add(origem, BorderLayout.SOUTH);
        origem.setForeground(Tema.AZUL);
        JPanel direita = new JPanel(new BorderLayout(0, 4));
        direita.setBackground(Color.WHITE);
        JPanel sul = new JPanel(new BorderLayout(0, 4));
        sul.setBackground(Color.WHITE);
        validado.setBackground(Color.WHITE);
        sul.add(validado, BorderLayout.NORTH);
        sul.add(salvar, BorderLayout.WEST);
        direita.add(new JLabel("Solução"), BorderLayout.NORTH);
        direita.add(new JScrollPane(solucaoRevisao), BorderLayout.CENTER);
        direita.add(sul, BorderLayout.SOUTH);
        detalhe.add(esquerda);
        detalhe.add(direita);
        JPanel extra = new JPanel(new BorderLayout(8, 4));
        extra.setBackground(Color.WHITE);
        extra.add(new JLabel("Complementos da solução"), BorderLayout.NORTH);
        tabelaComplementos.setPreferredScrollableViewportSize(new Dimension(100, 80));
        extra.add(new JScrollPane(tabelaComplementos), BorderLayout.CENTER);
        JPanel novoComplemento = new JPanel(new BorderLayout(8, 0));
        novoComplemento.setBackground(Color.WHITE);
        novoComplemento.add(new JScrollPane(complemento), BorderLayout.CENTER);
        novoComplemento.add(acrescentar, BorderLayout.EAST);
        extra.add(novoComplemento, BorderLayout.SOUTH);
        JPanel miolo = new JPanel(new BorderLayout(0, 8));
        miolo.setBackground(Color.WHITE);
        miolo.add(filtro, BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setBackground(Color.WHITE);
        centro.add(new JScrollPane(tabelaRevisao), BorderLayout.NORTH);
        centro.add(detalhe, BorderLayout.CENTER);
        centro.add(extra, BorderLayout.SOUTH);
        miolo.add(centro, BorderLayout.CENTER);
        revisar.add(cartao("Revisar casos", miolo), BorderLayout.CENTER);
    }

    private void listarAreas() {
        modeloAreas.setRowCount(0);
        if (app.banco() == null) {
            return;
        }
        for (Banco.Area area : app.banco().areas()) {
            modeloAreas.addRow(new Object[] {area.id, area.nome});
        }
    }

    private void listarCampos() {
        modeloCampos.setRowCount(0);
        Banco.Area area = (Banco.Area) areaCampo.getSelectedItem();
        if (area == null || app.banco() == null) {
            return;
        }
        for (Banco.Campo campo : app.banco().campos(area.id)) {
            modeloCampos.addRow(new Object[] {campo.id, campo.nome, campo.tipo, campo.peso});
        }
    }

    private void listarPalavras() {
        modeloPalavras.setRowCount(0);
        if (app.banco() == null) {
            return;
        }
        for (Banco.Palavra palavra : app.banco().palavras()) {
            modeloPalavras.addRow(new Object[] {palavra.chave, palavra.equivalentes.replace('\n', ';')});
        }
    }

    private void listarPares() {
        modeloPares.setRowCount(0);
        campoPar.removeAllItems();
        if (app.banco() == null) {
            return;
        }
        for (Banco.Area area : app.banco().areas()) {
            for (Banco.Campo campo : app.banco().campos(area.id)) {
                campoPar.addItem(new Item(campo.id, area.nome + " — " + campo.nome));
            }
        }
        for (Banco.Par par : app.banco().pares()) {
            modeloPares.addRow(new Object[] {
                    par.campo, par.consulta, par.caso,
                    String.valueOf(Math.round(par.similaridade * 100)) + "%",
                    par.id
            });
        }
        if (modeloPares.getColumnCount() > 4) {
            tabelaPares.getColumnModel().getColumn(4).setMinWidth(0);
            tabelaPares.getColumnModel().getColumn(4).setMaxWidth(0);
        }
    }

    private void listarRevisao() {
        modeloRevisao.setRowCount(0);
        if (app.banco() == null) {
            return;
        }
        Banco.Area area = (Banco.Area) filtroRevisao.getSelectedItem();
        revisoes = app.banco().revisoes(area == null ? null : area.id, soPendentes.isSelected());
        for (Banco.Revisao item : revisoes) {
            String texto = item.solucao == null ? "" : item.solucao;
            if (texto.length() > 140) {
                texto = texto.substring(0, 140) + "…";
            }
            modeloRevisao.addRow(new Object[] {"nº " + item.id, item.area, item.valido ? "Validado" : "Pendente", texto});
        }
    }

    private void mostrarRevisao() {
        int linha = tabelaRevisao.getSelectedRow();
        modeloComplementos.setRowCount(0);
        if (linha < 0 || linha >= revisoes.size()) {
            descricao.setText("");
            solucaoRevisao.setText("");
            origem.setText(" ");
            return;
        }
        Banco.Revisao item = revisoes.get(linha);
        descricao.setText(app.banco().descricao(item.id));
        solucaoRevisao.setText(item.solucao);
        validado.setSelected(item.valido);
        if (item.origemId == null) {
            origem.setText(" ");
        } else {
            String nota = item.similaridade == null ? "" : " (" + Math.round(item.similaridade * 100) + "%). ";
            origem.setText("Recuperado do caso nº " + item.origemId + nota + (item.adaptacao == null ? "" : item.adaptacao));
        }
        for (String texto : app.banco().complementos(item.id)) {
            modeloComplementos.addRow(new Object[] {texto});
        }
    }

    private Banco.Revisao selecionada() {
        int linha = tabelaRevisao.getSelectedRow();
        if (linha < 0 || linha >= revisoes.size()) {
            throw new IllegalArgumentException("Selecione um caso.");
        }
        return revisoes.get(linha);
    }

    private void encherSugestoes() {
        valorConsulta.removeAllItems();
        valorCaso.removeAllItems();
        Item item = (Item) campoPar.getSelectedItem();
        if (item == null || app.banco() == null) {
            return;
        }
        for (String sugestao : app.banco().sugestoes(item.id)) {
            valorConsulta.addItem(sugestao);
            valorCaso.addItem(sugestao);
        }
    }

    private void recarregarAreas(JComboBox<Banco.Area> combo) {
        Object atual = combo.getSelectedItem();
        combo.removeAllItems();
        if (combo == filtroRevisao) {
            combo.addItem(null);
        }
        if (app.banco() == null) {
            return;
        }
        for (Banco.Area area : app.banco().areas()) {
            combo.addItem(area);
        }
        if (atual instanceof Banco.Area) {
            combo.setSelectedItem(atual);
        }
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException ex) {
            app.avisar(ex.getMessage() == null ? "Não foi possível concluir." : ex.getMessage());
        }
    }

    private static String texto(JComboBox<String> combo) {
        Object item = combo.isEditable() ? combo.getEditor().getItem() : combo.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    private static JTextArea texto(boolean edita) {
        JTextArea area = new JTextArea(4, 20);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(edita);
        return area;
    }

    private static JLabel tituloMenor(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(Tema.AZUL);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static JTextArea nota(String texto) {
        JTextArea area = new JTextArea(texto);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setForeground(new Color(0x3E5166));
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        return area;
    }

    private static JPanel cartaoMini(String titulo, String texto) {
        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, Tema.VERDE),
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.BORDA), new EmptyBorder(8, 8, 8, 8))));
        JLabel faixa = new JLabel(titulo);
        faixa.setForeground(Tema.AZUL);
        faixa.setFont(faixa.getFont().deriveFont(Font.BOLD));
        painel.add(faixa, BorderLayout.NORTH);
        painel.add(nota(texto), BorderLayout.CENTER);
        return painel;
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
        if (corpo.getBorder() == null) {
            corpo.setBorder(new EmptyBorder(12, 12, 12, 12));
        }
        painel.add(faixa, BorderLayout.NORTH);
        painel.add(corpo, BorderLayout.CENTER);
        return painel;
    }

    private static JPanel corpo(JComponent form, JTable grade, int altura) {
        grade.setPreferredScrollableViewportSize(new Dimension(100, altura));
        JPanel painel = new JPanel(new BorderLayout(0, 8));
        painel.setBackground(Color.WHITE);
        painel.setBorder(new EmptyBorder(12, 12, 12, 12));
        painel.add(form, BorderLayout.NORTH);
        painel.add(new JScrollPane(grade), BorderLayout.CENTER);
        return painel;
    }

    private static JPanel linha(JComponent... partes) {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        painel.setBackground(Color.WHITE);
        for (JComponent parte : partes) {
            painel.add(parte);
        }
        return painel;
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
                return celula;
            }
        });
        return tabela;
    }

    private static final class Item {
        final long id;
        final String rotulo;

        Item(long id, String rotulo) {
            this.id = id;
            this.rotulo = rotulo;
        }

        @Override
        public String toString() {
            return rotulo;
        }
    }
}
