import erros.ColetorErros;
import erros.Erro;
import lexico.Lexer;
import lexico.Token;
import semantico.AnalisadorSemantico;
import sintatico.Parser;
import sintatico.Programa;
import tabelas.Simbolo;
import tabelas.TabelaLexemas;
import tabelas.TabelaSimbolos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Interface grafica do analisador SWITCH.
 *
 * Mantem a logica original intacta e apenas apresenta os resultados numa janela.
 */
public class InterfaceGrafica extends JFrame {

    private static final Color FUNDO_CREME = new Color(250, 244, 239);
    private static final Color PAINEL_ROSA_CAFE = new Color(232, 205, 198);
    private static final Color ROSA_CAFE = new Color(188, 123, 119);
    private static final Color CAFE = new Color(103, 68, 56);
    private static final Color CAFE_ESCURO = new Color(61, 39, 34);
    private static final Color BORDA_SUAVE = new Color(210, 178, 169);
    private static final Color LINHA_CLARA = new Color(255, 250, 247);
    private static final Color SELECAO = new Color(220, 167, 158);

    private final JTextArea editorFonte = new JTextArea();
    private final JTextArea codigoNumerado = new JTextArea();
    private final JTable tabelaLexemas = criarTabela();
    private final JTable tabelaSimbolos = criarTabela();
    private final JTable tabelaErros = criarTabela();
    private final JTextField campoFicheiro = new JTextField();
    private final JLabel estado = new JLabel("Pronto.");

    private File ficheiroAtual;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configurarAparencia();
            InterfaceGrafica tela = new InterfaceGrafica();
            tela.setVisible(true);
        });
    }

    public InterfaceGrafica() {
        super("Analisador da Estrutura SWITCH de Java");
        configurarJanela();
        montarInterface();
        novoExemplo();
    }

    private static void configurarAparencia() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
            // Usa o visual padrao da plataforma se Nimbus nao estiver disponivel.
        }
    }

    private void configurarJanela() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 640));
        setSize(1150, 740);
        setLocationRelativeTo(null);
    }

    private void montarInterface() {
        setJMenuBar(criarMenu());
        getContentPane().setBackground(FUNDO_CREME);

        JPanel topo = new JPanel(new BorderLayout(8, 0));
        topo.setBackground(PAINEL_ROSA_CAFE);
        topo.setBorder(BorderFactory.createEmptyBorder(10, 10, 8, 10));

        campoFicheiro.setEditable(false);
        campoFicheiro.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        campoFicheiro.setBackground(FUNDO_CREME);
        campoFicheiro.setForeground(CAFE_ESCURO);
        campoFicheiro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA_SUAVE),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        topo.add(campoFicheiro, BorderLayout.CENTER);

        JPanel botoes = new JPanel();
        botoes.setOpaque(false);
        JButton abrir = new JButton("Abrir");
        JButton analisar = new JButton("Analisar");
        JButton limpar = new JButton("Limpar");
        estilizarBotao(abrir, false);
        estilizarBotao(analisar, true);
        estilizarBotao(limpar, false);
        abrir.addActionListener(this::abrirFicheiro);
        analisar.addActionListener(e -> analisarFonte());
        limpar.addActionListener(e -> limparResultados());
        botoes.add(abrir);
        botoes.add(analisar);
        botoes.add(limpar);
        topo.add(botoes, BorderLayout.EAST);

        editorFonte.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        editorFonte.setTabSize(4);
        editorFonte.setBackground(new Color(255, 252, 249));
        editorFonte.setForeground(CAFE_ESCURO);
        editorFonte.setCaretColor(CAFE);
        editorFonte.setSelectionColor(SELECAO);
        editorFonte.setSelectedTextColor(CAFE_ESCURO);
        editorFonte.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        codigoNumerado.setEditable(false);
        codigoNumerado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        codigoNumerado.setBackground(LINHA_CLARA);
        codigoNumerado.setForeground(CAFE_ESCURO);
        codigoNumerado.setSelectionColor(SELECAO);
        codigoNumerado.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTabbedPane abas = new JTabbedPane();
        abas.setBackground(FUNDO_CREME);
        abas.setForeground(CAFE_ESCURO);
        abas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        abas.addTab("Codigo numerado", criarRolagem(codigoNumerado));
        abas.addTab("Lexemas", criarRolagem(tabelaLexemas));
        abas.addTab("Simbolos", criarRolagem(tabelaSimbolos));
        abas.addTab("Erros", criarRolagem(tabelaErros));

        JSplitPane divisao = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                criarRolagem(editorFonte),
                abas
        );
        divisao.setResizeWeight(0.46);
        divisao.setDividerLocation(520);
        divisao.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        divisao.setBackground(FUNDO_CREME);

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(PAINEL_ROSA_CAFE);
        rodape.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));
        estado.setForeground(CAFE_ESCURO);
        estado.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        rodape.add(estado, BorderLayout.WEST);

        add(topo, BorderLayout.NORTH);
        add(divisao, BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);
    }

    private JMenuBar criarMenu() {
        JMenuBar barra = new JMenuBar();
        barra.setBackground(CAFE);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CAFE_ESCURO));

        JMenu ficheiro = new JMenu("Ficheiro");
        estilizarMenu(ficheiro);

        JMenuItem novo = new JMenuItem("Novo exemplo");
        novo.addActionListener(e -> novoExemplo());

        JMenuItem abrir = new JMenuItem("Abrir...");
        abrir.addActionListener(this::abrirFicheiro);

        JMenuItem sair = new JMenuItem("Sair");
        sair.addActionListener(e -> dispose());

        ficheiro.add(novo);
        ficheiro.add(abrir);
        ficheiro.addSeparator();
        ficheiro.add(sair);

        JMenu analisar = new JMenu("Analise");
        estilizarMenu(analisar);
        JMenuItem executar = new JMenuItem("Executar analise");
        executar.addActionListener(e -> analisarFonte());
        analisar.add(executar);

        barra.add(ficheiro);
        barra.add(analisar);
        return barra;
    }

    private JTable criarTabela() {
        JTable tabela = new JTable();
        tabela.setAutoCreateRowSorter(true);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabela.setFillsViewportHeight(true);
        tabela.setRowHeight(26);
        tabela.setGridColor(new Color(226, 202, 195));
        tabela.setBackground(LINHA_CLARA);
        tabela.setForeground(CAFE_ESCURO);
        tabela.setSelectionBackground(SELECAO);
        tabela.setSelectionForeground(CAFE_ESCURO);
        tabela.setShowVerticalLines(false);
        tabela.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        tabela.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        tabela.getTableHeader().setBackground(CAFE);
        tabela.getTableHeader().setForeground(Color.WHITE);
        tabela.getTableHeader().setOpaque(true);
        return tabela;
    }

    private JScrollPane criarRolagem(Component componente) {
        JScrollPane scroll = new JScrollPane(componente);
        scroll.setBorder(BorderFactory.createLineBorder(BORDA_SUAVE));
        scroll.getViewport().setBackground(LINHA_CLARA);
        return scroll;
    }

    private void estilizarBotao(JButton botao, boolean destaque) {
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(destaque ? CAFE_ESCURO : ROSA_CAFE),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        botao.setBackground(destaque ? CAFE : ROSA_CAFE);
        botao.setForeground(Color.WHITE);
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    }

    private void estilizarMenu(JMenu menu) {
        menu.setForeground(Color.WHITE);
        menu.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
    }

    private void abrirFicheiro(ActionEvent e) {
        JFileChooser seletor = new JFileChooser(ficheiroAtual != null ? ficheiroAtual.getParentFile() : new File("."));
        seletor.setFileFilter(new FileNameExtensionFilter("Ficheiros de texto (*.txt)", "txt"));

        if (seletor.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File escolhido = seletor.getSelectedFile();
        try {
            String conteudo = new String(Files.readAllBytes(escolhido.toPath()), StandardCharsets.UTF_8);
            ficheiroAtual = escolhido;
            campoFicheiro.setText(escolhido.getAbsolutePath());
            editorFonte.setText(conteudo);
            editorFonte.setCaretPosition(0);
            analisarFonte();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Nao foi possivel abrir o ficheiro:\n" + ex.getMessage(),
                    "Erro ao abrir",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void novoExemplo() {
        ficheiroAtual = null;
        campoFicheiro.setText("Exemplo interno");
        editorFonte.setText(
                "int opcao = 2;\n" +
                "String mensagem = \"inicio\";\n" +
                "final int LIMITE = 3;\n" +
                "\n" +
                "switch (opcao) {\n" +
                "    case 1:\n" +
                "        mensagem = \"um\";\n" +
                "        break;\n" +
                "    case 2:\n" +
                "        mensagem = \"dois\";\n" +
                "        break;\n" +
                "    default:\n" +
                "        mensagem = \"outro\";\n" +
                "        break;\n" +
                "}\n"
        );
        editorFonte.setCaretPosition(0);
        analisarFonte();
    }

    private void limparResultados() {
        editorFonte.setText("");
        codigoNumerado.setText("");
        tabelaLexemas.setModel(new DefaultTableModel());
        tabelaSimbolos.setModel(new DefaultTableModel());
        tabelaErros.setModel(new DefaultTableModel());
        estado.setText("Resultados limpos.");
    }

    private void analisarFonte() {
        String fonte = editorFonte.getText();
        ResultadoAnalise resultado = analisar(fonte);

        preencherCodigoNumerado(fonte);
        preencherLexemas(resultado.tokens);
        preencherSimbolos(resultado.simbolos);
        preencherErros(resultado.erros);

        int totalTokens = resultado.tokens.size();
        int totalSimbolos = resultado.simbolos.size();
        int totalErros = resultado.erros.size();
        estado.setText("Analise concluida: " + totalTokens + " lexemas, "
                + totalSimbolos + " simbolos, " + totalErros + " erro(s).");
    }

    private ResultadoAnalise analisar(String fonte) {
        ColetorErros erros = new ColetorErros();

        Lexer lexer = new Lexer(fonte, erros);
        List<Token> todosTokens = lexer.analisar();

        Parser parser = new Parser(todosTokens, erros);
        Programa programa = parser.analisar();

        TabelaSimbolos tabelaSimbolos = new TabelaSimbolos();
        AnalisadorSemantico semantico = new AnalisadorSemantico(tabelaSimbolos, erros);
        semantico.analisar(programa);

        TabelaLexemas tabelaLexemas = new TabelaLexemas(todosTokens);
        return new ResultadoAnalise(
                tabelaLexemas.tokens(),
                new ArrayList<>(tabelaSimbolos.todos()),
                new ArrayList<>(erros.lista())
        );
    }

    private void preencherCodigoNumerado(String fonte) {
        String[] linhas = fonte.replace("\r", "").split("\n", -1);
        int total = linhas.length;
        if (total > 0 && linhas[total - 1].isEmpty()) {
            total--;
        }

        int largura = String.valueOf(Math.max(total, 1)).length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < total; i++) {
            sb.append(String.format("%" + largura + "d | %s%n", i + 1, linhas[i]));
        }
        codigoNumerado.setText(sb.toString());
        codigoNumerado.setCaretPosition(0);
    }

    private void preencherLexemas(List<Token> tokens) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"TOKEN", "CLASSIFICACAO", "LINHA"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Token token : tokens) {
            modelo.addRow(new Object[]{
                    token.lexema,
                    classificacaoLexema(token),
                    token.linha
            });
        }
        tabelaLexemas.setModel(modelo);
        ajustarLargurasTabelaLexemas();
    }

    private void ajustarLargurasTabelaLexemas() {
        int[] larguras = {150, 260, 80};
        TableColumnModel colunas = tabelaLexemas.getColumnModel();
        for (int i = 0; i < larguras.length && i < colunas.getColumnCount(); i++) {
            colunas.getColumn(i).setPreferredWidth(larguras[i]);
        }
    }

    private String classificacaoLexema(Token token) {
        switch (token.classe) {
            case PALAVRA_RESERVADA:
                if ("int".equals(token.lexema)) {
                    return "Tipo inteiro";
                }
                if ("String".equals(token.lexema) || "string".equals(token.lexema)) {
                    return "Tipo objecto";
                }
                return "Palavra reservada";
            case IDENTIFICADOR:
                return "Identificador";
            case LITERAL_INT:
                return "Literal inteiro";
            case LITERAL_STRING:
                return "Literal string";
            case OP_ATRIBUICAO:
                return "Operador de atribuicao";
            case DELIMITADOR:
            case PONTUACAO:
                return "Simbolo especial";
            default:
                return token.classe.name();
        }
    }

    private void preencherSimbolos(Collection<Simbolo> simbolos) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{
                        "IDENTIFICADOR",
                        "CATEGORIA",
                        "TIPO",
                        "EST.MEM.",
                        "NIVEL",
                        "NR. PARAM",
                        "SEQ.PARAM",
                        "F.PASSAGEM",
                        "VALOR",
                        "DIMENSAO",
                        "REF"
                }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Simbolo simbolo : simbolos) {
            modelo.addRow(new Object[]{
                    simbolo.identificador,
                    simbolo.categoria.descricao(),
                    simbolo.tipo,
                    estadoMemoria(simbolo.tipo),
                    "0",
                    "--------",
                    "--------",
                    "--------",
                    simbolo.valor,
                    "--------",
                    "--------"
            });
        }
        tabelaSimbolos.setModel(modelo);
        ajustarLargurasTabelaSimbolos();
    }

    private void ajustarLargurasTabelaSimbolos() {
        int[] larguras = {120, 105, 80, 90, 65, 95, 100, 105, 90, 95, 80};
        TableColumnModel colunas = tabelaSimbolos.getColumnModel();
        for (int i = 0; i < larguras.length && i < colunas.getColumnCount(); i++) {
            colunas.getColumn(i).setPreferredWidth(larguras[i]);
        }
    }

    private String estadoMemoria(String tipo) {
        if ("int".equals(tipo)) {
            return "primitiva";
        }
        if ("String".equals(tipo) || "string".equals(tipo)) {
            return "objecto";
        }
        return "--------";
    }

    private void preencherErros(List<Erro> erros) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"#", "Fase", "Descricao", "Linha"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Erro> ordenados = new ArrayList<>(erros);
        ordenados.sort(Comparator.comparingInt(e -> e.linha));

        int i = 1;
        for (Erro erro : ordenados) {
            modelo.addRow(new Object[]{
                    i++,
                    erro.fase.descricao(),
                    erro.descricao,
                    erro.linha
            });
        }
        tabelaErros.setModel(modelo);
    }

    private static class ResultadoAnalise {
        private final List<Token> tokens;
        private final List<Simbolo> simbolos;
        private final List<Erro> erros;

        private ResultadoAnalise(List<Token> tokens, List<Simbolo> simbolos, List<Erro> erros) {
            this.tokens = tokens;
            this.simbolos = simbolos;
            this.erros = erros;
        }
    }
}
