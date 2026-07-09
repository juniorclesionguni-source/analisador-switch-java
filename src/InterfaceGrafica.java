import erros.ColetorErros;
import erros.Erro;
import lexico.Lexer;
import lexico.Token;
import semantico.AnalisadorSemantico;
import sintatico.Atribuicao;
import sintatico.Caso;
import sintatico.ComandoBreak;
import sintatico.ComandoSwitch;
import sintatico.Declaracao;
import sintatico.Instrucao;
import sintatico.Parser;
import sintatico.Programa;
import lexico.ClasseToken;
import tabelas.Categoria;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private final JTextArea numerosLinhas = new JTextArea("1");
    private final JTextArea codigoNumerado = new JTextArea();
    private final JTable tabelaLexemas = criarTabela();
    private final PainelArvoreAst arvoreAst = new PainelArvoreAst();      // derivação (notação do docente)
    private final PainelArvoreAst arvoreSimples = new PainelArvoreAst();  // AST simplificada
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
        topo.setBorder(BorderFactory.createEmptyBorder(12, 12, 10, 12));

        JLabel titulo = new JLabel("Analisador SWITCH");
        titulo.setForeground(CAFE_ESCURO);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        topo.add(titulo, BorderLayout.WEST);

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
        configurarNumerosLinhas();

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
        abas.addTab("Arvore de sintaxe", criarRolagem(arvoreAst));
        abas.addTab("AST (simplificada)", criarRolagem(arvoreSimples));
        abas.addTab("Gramatica", criarRolagem(criarPainelGramatica()));

        JScrollPane rolagemEditor = criarRolagem(editorFonte);
        rolagemEditor.setRowHeaderView(numerosLinhas);

        JSplitPane divisao = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                criarSecao("Codigo fonte", rolagemEditor),
                criarSecao("Resultados", abas)
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
        JTable tabela = new JTable() {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component componente = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    componente.setBackground(row % 2 == 0 ? LINHA_CLARA : new Color(248, 236, 232));
                    componente.setForeground(CAFE_ESCURO);
                }
                return componente;
            }
        };
        tabela.setAutoCreateRowSorter(true);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
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

    // ÁRVORE DE SINTAXE (árvore de derivação) na notação do docente (F3/F5):
    // a raiz é o símbolo inicial <programa>; cada nó não-terminal expande nos
    // símbolos do lado direito da regra BNF aplicada; as folhas são os
    // terminais (lexemas) e ϵ quando a regra é vazia.
    private void preencherArvore(Programa programa) {
        NoDesenho raiz = new NoDesenho("<programa>");
        raiz.filhos.add(noListaDecl(programa.declaracoes, 0));
        raiz.filhos.add(noListaSwitch(programa.switches, 0));
        arvoreAst.setRaiz(raiz);
    }

    // AST SIMPLIFICADA (abstract syntax tree): a derivação sem pontuação nem
    // nós de lista — só a estrutura ("can become", como nos slides). Cadeia =
    // switches irmãos sob PROGRAMA; aninhamento = switch filho de um case.
    private void preencherArvoreSimples(Programa programa) {
        NoDesenho raiz = new NoDesenho("PROGRAMA");
        for (Declaracao d : programa.declaracoes) {
            NoDesenho decl = new NoDesenho("declaracao");
            decl.filhos.add(new NoDesenho(d.tipo));
            decl.filhos.add(new NoDesenho(d.identificador));
            if (d.valorLexema != null) {
                decl.filhos.add(new NoDesenho("= " + d.valorLexema));
            }
            raiz.filhos.add(decl);
        }
        for (ComandoSwitch s : programa.switches) {
            raiz.filhos.add(noSwitchSimples(s));
        }
        arvoreSimples.setRaiz(raiz);
    }

    private NoDesenho noSwitchSimples(ComandoSwitch s) {
        NoDesenho no = new NoDesenho("switch (" + s.selector + ")");
        for (Caso c : s.casos) {
            no.filhos.add(noCasoSimples(c));
        }
        if (s.casoDefault != null) {
            no.filhos.add(noCasoSimples(s.casoDefault));
        }
        return no;
    }

    private NoDesenho noCasoSimples(Caso c) {
        NoDesenho no = new NoDesenho(c.ehDefault ? "default" : "case " + c.rotuloLexema);
        for (Instrucao i : c.instrucoes) {
            if (i instanceof ComandoSwitch) {
                no.filhos.add(noSwitchSimples((ComandoSwitch) i)); // aninhamento
            } else if (i instanceof Atribuicao) {
                Atribuicao a = (Atribuicao) i;
                NoDesenho atr = new NoDesenho("=");
                atr.filhos.add(new NoDesenho(a.destino));
                atr.filhos.add(new NoDesenho(a.valorLexema));
                no.filhos.add(atr);
            } else if (i instanceof ComandoBreak) {
                no.filhos.add(new NoDesenho("break"));
            }
        }
        return no;
    }

    /** <lista_decl> ::= <declaracao> ; <lista_decl> | ϵ */
    private NoDesenho noListaDecl(List<Declaracao> ds, int i) {
        NoDesenho no = new NoDesenho("<lista_decl>");
        if (i >= ds.size()) {
            no.filhos.add(new NoDesenho("ϵ"));
            return no;
        }
        no.filhos.add(noDeclaracao(ds.get(i)));
        no.filhos.add(new NoDesenho(";"));
        no.filhos.add(noListaDecl(ds, i + 1));
        return no;
    }

    /** <declaracao> ::= <tipo> ident [= <valor>] | final <tipo> ident = <valor> */
    private NoDesenho noDeclaracao(Declaracao d) {
        NoDesenho no = new NoDesenho("<declaracao>");
        if (d.categoria == Categoria.CONSTANTE) {
            no.filhos.add(new NoDesenho("final"));
        }
        NoDesenho tipo = new NoDesenho("<tipo>");
        tipo.filhos.add(new NoDesenho(d.tipo));
        no.filhos.add(tipo);
        no.filhos.add(new NoDesenho(d.identificador));
        if (d.valorLexema != null) {
            no.filhos.add(new NoDesenho("="));
            no.filhos.add(noValor(d.valorLexema, d.valorClasse));
        }
        return no;
    }

    /** <valor> ::= <constante> | ident        <constante> ::= lit_int | lit_string */
    private NoDesenho noValor(String lexema, ClasseToken classe) {
        NoDesenho no = new NoDesenho("<valor>");
        if (classe == ClasseToken.IDENTIFICADOR) {
            no.filhos.add(new NoDesenho(lexema));
        } else {
            NoDesenho constante = new NoDesenho("<constante>");
            constante.filhos.add(new NoDesenho(lexema));
            no.filhos.add(constante);
        }
        return no;
    }

    /** <lista_switch> ::= <switch> <lista_switch> | <switch> */
    private NoDesenho noListaSwitch(List<ComandoSwitch> ss, int i) {
        NoDesenho no = new NoDesenho("<lista_switch>");
        if (i < ss.size()) {
            no.filhos.add(noSwitch(ss.get(i)));
            if (i + 1 < ss.size()) {
                no.filhos.add(noListaSwitch(ss, i + 1));
            }
        }
        return no;
    }

    /** <switch> ::= switch ( <selector> ) { <lista_caso> [<default>] } */
    private NoDesenho noSwitch(ComandoSwitch s) {
        NoDesenho no = new NoDesenho("<switch>");
        no.filhos.add(new NoDesenho("switch"));
        no.filhos.add(new NoDesenho("("));
        NoDesenho selector = new NoDesenho("<selector>");
        selector.filhos.add(new NoDesenho(s.selector));
        no.filhos.add(selector);
        no.filhos.add(new NoDesenho(")"));
        no.filhos.add(new NoDesenho("{"));
        no.filhos.add(noListaCaso(s.casos, 0));
        if (s.casoDefault != null) {
            no.filhos.add(noDefault(s.casoDefault));
        }
        no.filhos.add(new NoDesenho("}"));
        return no;
    }

    /** <lista_caso> ::= <caso> <lista_caso> | <caso> */
    private NoDesenho noListaCaso(List<Caso> casos, int i) {
        NoDesenho no = new NoDesenho("<lista_caso>");
        if (i < casos.size()) {
            no.filhos.add(noCaso(casos.get(i)));
            if (i + 1 < casos.size()) {
                no.filhos.add(noListaCaso(casos, i + 1));
            }
        }
        return no;
    }

    /** <caso> ::= case <constante> : <lista_instr> */
    private NoDesenho noCaso(Caso c) {
        NoDesenho no = new NoDesenho("<caso>");
        no.filhos.add(new NoDesenho("case"));
        NoDesenho constante = new NoDesenho("<constante>");
        constante.filhos.add(new NoDesenho(c.rotuloLexema));
        no.filhos.add(constante);
        no.filhos.add(new NoDesenho(":"));
        no.filhos.add(noListaInstr(c.instrucoes, 0));
        return no;
    }

    /** <default> ::= default : <lista_instr> */
    private NoDesenho noDefault(Caso c) {
        NoDesenho no = new NoDesenho("<default>");
        no.filhos.add(new NoDesenho("default"));
        no.filhos.add(new NoDesenho(":"));
        no.filhos.add(noListaInstr(c.instrucoes, 0));
        return no;
    }

    /** <lista_instr> ::= <instrucao> <lista_instr> | ϵ */
    private NoDesenho noListaInstr(List<Instrucao> instrucoes, int i) {
        NoDesenho no = new NoDesenho("<lista_instr>");
        if (i >= instrucoes.size()) {
            no.filhos.add(new NoDesenho("ϵ"));
            return no;
        }
        no.filhos.add(noInstrucao(instrucoes.get(i)));
        no.filhos.add(noListaInstr(instrucoes, i + 1));
        return no;
    }

    /** <instrucao> ::= <switch> | <atribuicao> | <break> */
    private NoDesenho noInstrucao(Instrucao instr) {
        NoDesenho no = new NoDesenho("<instrucao>");
        if (instr instanceof ComandoSwitch) {
            no.filhos.add(noSwitch((ComandoSwitch) instr)); // aninhamento
        } else if (instr instanceof Atribuicao) {
            Atribuicao a = (Atribuicao) instr;
            NoDesenho atr = new NoDesenho("<atribuicao>");
            atr.filhos.add(new NoDesenho(a.destino));
            atr.filhos.add(new NoDesenho("="));
            atr.filhos.add(noValor(a.valorLexema, a.valorClasse));
            atr.filhos.add(new NoDesenho(";"));
            no.filhos.add(atr);
        } else if (instr instanceof ComandoBreak) {
            NoDesenho brk = new NoDesenho("<break>");
            brk.filhos.add(new NoDesenho("break"));
            brk.filhos.add(new NoDesenho(";"));
            no.filhos.add(brk);
        }
        return no;
    }

    /** Nó simples para desenho: texto + filhos. */
    private static class NoDesenho {
        final String texto;
        final List<NoDesenho> filhos = new ArrayList<>();

        NoDesenho(String texto) {
            this.texto = texto;
        }
    }

    /**
     * Painel que DESENHA a AST como um diagrama clássico de árvore (estilo dos
     * slides de Compiladores): nó pai em cima, linhas em leque para os filhos.
     * A largura de cada subárvore é calculada recursivamente para os ramos não
     * se sobreporem.
     */
    private static class PainelArvoreAst extends JPanel implements javax.swing.Scrollable {

        private static final int ALTURA_NIVEL = 60;    // distância vertical entre níveis
        private static final int ESPACO = 26;          // espaço horizontal entre subárvores
        private static final int MARGEM = 20;
        private static final double ESCALA_MINIMA = 0.75; // nunca encolher abaixo disto (legibilidade)

        private NoDesenho raiz;

        PainelArvoreAst() {
            setBackground(LINHA_CLARA);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        }

        private int larguraArvore() {
            FontMetrics fm = getFontMetrics(getFont());
            return larguraSubarvore(raiz, fm) + 2 * MARGEM;
        }

        private int alturaArvore() {
            return profundidade(raiz) * ALTURA_NIVEL + 2 * MARGEM;
        }

        /** Área visível do JScrollPane onde o painel está inserido. */
        private Dimension areaVisivel() {
            if (getParent() instanceof javax.swing.JViewport) {
                return ((javax.swing.JViewport) getParent()).getExtentSize();
            }
            return getSize();
        }

        /**
         * Tenta caber no ecrã (zoom-to-fit), mas nunca abaixo de ESCALA_MINIMA:
         * a partir daí a legibilidade manda e o resto vê-se com o scroll.
         */
        private double escalaAtual() {
            if (raiz == null) {
                return 1.0;
            }
            Dimension vp = areaVisivel();
            double fit = Math.min(1.0, Math.min(
                    vp.width / (double) larguraArvore(),
                    vp.height / (double) alturaArvore()));
            return Math.max(ESCALA_MINIMA, fit);
        }

        void setRaiz(NoDesenho raiz) {
            this.raiz = raiz;
            revalidate(); // recalcula o preferredSize para o scroll
            repaint();
        }

        // Ocupa sempre exactamente o viewport do JScrollPane: o desenho é
        // escalado (zoom-to-fit), por isso nunca há partes fora do ecrã.
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        // Só "cola" ao viewport quando a árvore (escalada) cabe nele; caso
        // contrário o painel fica maior e o JScrollPane mostra as barras.
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getPreferredSize().width <= areaVisivel().width;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getPreferredSize().height <= areaVisivel().height;
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) {
            return 64;
        }

        /** Largura em píxeis da subárvore: máx(texto, soma dos filhos). */
        private int larguraSubarvore(NoDesenho no, FontMetrics fm) {
            int larguraTexto = fm.stringWidth(no.texto) + 16;
            if (no.filhos.isEmpty()) {
                return larguraTexto;
            }
            int somaFilhos = 0;
            for (NoDesenho f : no.filhos) {
                somaFilhos += larguraSubarvore(f, fm) + ESPACO;
            }
            somaFilhos -= ESPACO;
            return Math.max(larguraTexto, somaFilhos);
        }

        private int profundidade(NoDesenho no) {
            int max = 0;
            for (NoDesenho f : no.filhos) {
                max = Math.max(max, profundidade(f));
            }
            return 1 + max;
        }

        @Override
        public Dimension getPreferredSize() {
            if (raiz == null) {
                return new Dimension(400, 300);
            }
            double escala = escalaAtual();
            return new Dimension(
                    (int) Math.ceil(larguraArvore() * escala),
                    (int) Math.ceil(alturaArvore() * escala));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (raiz == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(1.4f));
            FontMetrics fm = g2.getFontMetrics();

            double escala = escalaAtual();

            // Centra horizontalmente o espaço que sobra (quando a árvore cabe).
            double sobraX = getWidth() - larguraArvore() * escala;
            g2.translate(Math.max(0, sobraX / 2), 0);
            g2.scale(escala, escala);

            desenhar(g2, fm, raiz, MARGEM, MARGEM + fm.getAscent());
            g2.dispose();
        }

        /**
         * Desenha o nó centrado no seu intervalo [x, x+largura] no nível ySup e,
         * recursivamente, os filhos no nível abaixo, ligados por linhas.
         */
        private void desenhar(Graphics2D g2, FontMetrics fm, NoDesenho no, int x, int ySup) {
            int largura = larguraSubarvore(no, fm);
            int centroX = x + largura / 2;

            g2.setColor(CAFE_ESCURO);
            g2.drawString(no.texto, centroX - fm.stringWidth(no.texto) / 2, ySup);

            int filhoX = x + Math.max(0, (largura - larguraFilhos(no, fm)) / 2);
            int yFilho = ySup + ALTURA_NIVEL;
            for (NoDesenho f : no.filhos) {
                int larguraFilho = larguraSubarvore(f, fm);
                int centroFilhoX = filhoX + larguraFilho / 2;

                g2.setColor(ROSA_CAFE);
                g2.drawLine(centroX, ySup + 5, centroFilhoX, yFilho - fm.getAscent() - 3);

                desenhar(g2, fm, f, filhoX, yFilho);
                filhoX += larguraFilho + ESPACO;
            }
        }

        private int larguraFilhos(NoDesenho no, FontMetrics fm) {
            int soma = 0;
            for (NoDesenho f : no.filhos) {
                soma += larguraSubarvore(f, fm) + ESPACO;
            }
            return Math.max(0, soma - ESPACO);
        }
    }

    /** Aba com a gramática formal (BNF) que o parser implementa. */
    private JTextArea criarPainelGramatica() {
        JTextArea area = new JTextArea(
                "GRAMÁTICA DA LINGUAGEM — NOTAÇÃO BNF (FORMA DE BACKUS-NAUR)\n" +
                "===========================================================\n\n" +
                "P:\n" +
                " 1. <programa>     ::= <lista_decl> <lista_switch>\n" +
                " 2. <lista_decl>   ::= <declaracao> ; <lista_decl>\n" +
                " 3. <lista_decl>   ::= ϵ\n" +
                " 4. <declaracao>   ::= <tipo> ident\n" +
                " 5. <declaracao>   ::= <tipo> ident = <valor>\n" +
                " 6. <declaracao>   ::= final <tipo> ident = <valor>\n" +
                " 7. <tipo>         ::= int\n" +
                " 8. <tipo>         ::= String\n" +
                " 9. <lista_switch> ::= <switch> <lista_switch>\n" +
                "10. <lista_switch> ::= <switch>\n" +
                "11. <switch>       ::= switch ( <selector> ) { <lista_caso> }\n" +
                "12. <switch>       ::= switch ( <selector> ) { <lista_caso> <default> }\n" +
                "13. <selector>     ::= ident\n" +
                "14. <lista_caso>   ::= <caso> <lista_caso>\n" +
                "15. <lista_caso>   ::= <caso>\n" +
                "16. <caso>         ::= case <constante> : <lista_instr>\n" +
                "17. <default>      ::= default : <lista_instr>\n" +
                "18. <lista_instr>  ::= <instrucao> <lista_instr>\n" +
                "19. <lista_instr>  ::= ϵ\n" +
                "20. <instrucao>    ::= <switch>\n" +
                "21. <instrucao>    ::= <atribuicao>\n" +
                "22. <instrucao>    ::= <break>\n" +
                "23. <atribuicao>   ::= ident = <valor> ;\n" +
                "24. <break>        ::= break ;\n" +
                "25. <valor>        ::= <constante>\n" +
                "26. <valor>        ::= ident\n" +
                "27. <constante>    ::= lit_int\n" +
                "28. <constante>    ::= lit_string\n\n" +
                "G = ({<programa>, <lista_decl>, <declaracao>, <tipo>, <lista_switch>,\n" +
                "      <switch>, <selector>, <lista_caso>, <caso>, <default>,\n" +
                "      <lista_instr>, <instrucao>, <atribuicao>, <break>, <valor>,\n" +
                "      <constante>},\n" +
                "     {switch, case, default, break, int, String, final,\n" +
                "      ident, lit_int, lit_string, =, (, ), {, }, :, ;},\n" +
                "     P, <programa>)\n\n" +
                "Não-terminais: abstracções entre < >     Terminais: lexemas/tokens\n" +
                "Regra 20: um <switch> é uma <instrucao> → ANINHAMENTO\n" +
                "Regra  9: <lista_switch> recursiva → switches em CADEIA\n\n" +
                "TIPO DE GRAMÁTICA\n" +
                "=================\n" +
                "Gramática LIVRE DE CONTEXTO (Tipo 2 de Chomsky), escrita em BNF,\n" +
                "com a propriedade LL(1): basta 1 token de lookahead para escolher\n" +
                "a produção, sem ambiguidade nem backtracking. É isso que permite\n" +
                "implementá-la como parser DESCENDENTE RECURSIVO — cada não-terminal\n" +
                "é um método do Parser.java.\n"
        );
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        area.setBackground(LINHA_CLARA);
        area.setForeground(CAFE_ESCURO);
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return area;
    }

    private JPanel criarSecao(String titulo, Component conteudo) {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(FUNDO_CREME);
        painel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JLabel cabecalho = new JLabel(titulo);
        cabecalho.setOpaque(true);
        cabecalho.setBackground(CAFE);
        cabecalho.setForeground(Color.WHITE);
        cabecalho.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        cabecalho.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));

        painel.add(cabecalho, BorderLayout.NORTH);
        painel.add(conteudo, BorderLayout.CENTER);
        return painel;
    }

    private JScrollPane criarRolagem(Component componente) {
        JScrollPane scroll = new JScrollPane(componente);
        scroll.setBorder(BorderFactory.createLineBorder(BORDA_SUAVE));
        scroll.getViewport().setBackground(LINHA_CLARA);
        scroll.setBackground(LINHA_CLARA);
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new JPanel());
        return scroll;
    }

    private void estilizarBotao(JButton botao, boolean destaque) {
        botao.setFocusPainted(false);
        botao.setOpaque(true);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(destaque ? CAFE_ESCURO : ROSA_CAFE),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        Color normal = destaque ? CAFE : ROSA_CAFE;
        Color hover = destaque ? CAFE_ESCURO : new Color(170, 103, 101);
        botao.setBackground(normal);
        botao.setForeground(Color.WHITE);
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(normal);
            }
        });
    }

    private void estilizarMenu(JMenu menu) {
        menu.setForeground(Color.WHITE);
        menu.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
    }

    private void configurarNumerosLinhas() {
        numerosLinhas.setEditable(false);
        numerosLinhas.setFocusable(false);
        numerosLinhas.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        numerosLinhas.setBackground(new Color(239, 220, 214));
        numerosLinhas.setForeground(CAFE);
        numerosLinhas.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        numerosLinhas.setColumns(3);

        editorFonte.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                atualizarNumerosLinhas();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                atualizarNumerosLinhas();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                atualizarNumerosLinhas();
            }
        });
        atualizarNumerosLinhas();
    }

    private void atualizarNumerosLinhas() {
        int total = Math.max(1, editorFonte.getLineCount());
        int largura = String.valueOf(total).length();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= total; i++) {
            sb.append(String.format("%" + largura + "d%n", i));
        }
        numerosLinhas.setText(sb.toString());
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
        preencherArvore(resultado.programa);
        preencherArvoreSimples(resultado.programa);
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
                programa,
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
        private final Programa programa;
        private final List<Simbolo> simbolos;
        private final List<Erro> erros;

        private ResultadoAnalise(List<Token> tokens, Programa programa,
                                 List<Simbolo> simbolos, List<Erro> erros) {
            this.tokens = tokens;
            this.programa = programa;
            this.simbolos = simbolos;
            this.erros = erros;
        }
    }
}
