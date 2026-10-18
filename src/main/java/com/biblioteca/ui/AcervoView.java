package com.biblioteca.ui;

import com.biblioteca.data.Biblioteca;
import com.biblioteca.model.Livro;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AcervoView {
    private final VBox root = new VBox(14);
    private final ObservableList<Livro> dados = FXCollections.observableArrayList();
    private final FilteredList<Livro> filtrados = new FilteredList<>(dados, l -> true);
    private final TableView<Livro> tabela = new TableView<>(filtrados);

    private final TextField titulo = new TextField();
    private final TextField autor = new TextField();
    private final TextField isbn = new TextField();
    private final TextField categoria = new TextField();
    private final Spinner<Integer> exemplares = new Spinner<>(1, 999, 1);
    private final TextField busca = new TextField();
    private final Label formTitulo = new Label("Novo livro");

    private Livro editando;

    public AcervoView() {
        root.setPadding(new Insets(18));

        titulo.setPromptText("Título da obra");
        autor.setPromptText("Autor");
        isbn.setPromptText("ISBN (opcional)");
        categoria.setPromptText("Ex.: Romance, Técnico, Infantil");
        exemplares.setEditable(true);
        exemplares.setMaxWidth(Double.MAX_VALUE);
        // confirma o valor digitado ao sair do campo
        exemplares.focusedProperty().addListener((o, a, focado) -> {
            if (!focado) {
                try {
                    exemplares.increment(0);
                } catch (RuntimeException ignorado) {
                    exemplares.getEditor().setText(String.valueOf(exemplares.getValue()));
                }
            }
        });
        formTitulo.getStyleClass().add("secao");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        form.getColumnConstraints().addAll(c1, c2);
        form.add(rotulo("Título *", titulo), 0, 0, 2, 1);
        form.add(rotulo("Autor *", autor), 0, 1);
        form.add(rotulo("ISBN", isbn), 1, 1);
        form.add(rotulo("Categoria", categoria), 0, 2);
        form.add(rotulo("Exemplares *", exemplares), 1, 2);

        Button salvar = new Button("Salvar");
        salvar.getStyleClass().add("primario");
        salvar.setOnAction(e -> salvar());
        Button novo = new Button("Novo");
        novo.setOnAction(e -> limpar());
        Button excluir = new Button("Excluir");
        excluir.getStyleClass().add("perigo");
        excluir.setOnAction(e -> excluir());
        excluir.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());

        VBox cardForm = new VBox(12, formTitulo, form, new HBox(8, salvar, novo, excluir));
        cardForm.getStyleClass().add("card");
        cardForm.setPadding(new Insets(16));

        busca.setPromptText("Buscar por título, autor, categoria ou ISBN…");
        busca.textProperty().addListener((o, a, b) -> {
            String t = b == null ? "" : b.trim().toLowerCase();
            filtrados.setPredicate(l -> t.isEmpty()
                    || l.getTitulo().toLowerCase().contains(t)
                    || l.getAutor().toLowerCase().contains(t)
                    || nulo(l.getCategoria()).toLowerCase().contains(t)
                    || nulo(l.getIsbn()).toLowerCase().contains(t));
        });

        montarTabela();
        tabela.getSelectionModel().selectedItemProperty().addListener((o, a, l) -> {
            if (l != null) carregar(l);
        });

        VBox.setVgrow(tabela, Priority.ALWAYS);
        root.getChildren().addAll(cardForm, busca, tabela);
        refresh();
    }

    private void montarTabela() {
        tabela.getColumns().addAll(java.util.List.of(
                coluna("Título", "titulo", 240),
                coluna("Autor", "autor", 170),
                coluna("Categoria", "categoria", 120),
                coluna("ISBN", "isbn", 120),
                coluna("Total", "totalExemplares", 60),
                coluna("Disponíveis", "disponiveis", 90)));
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhum livro cadastrado."));
    }

    private static <T> TableColumn<Livro, T> coluna(String nome, String propriedade, double largura) {
        TableColumn<Livro, T> c = new TableColumn<>(nome);
        c.setCellValueFactory(new PropertyValueFactory<>(propriedade));
        c.setPrefWidth(largura);
        return c;
    }

    private static VBox rotulo(String texto, javafx.scene.Node campo) {
        Label l = new Label(texto);
        l.getStyleClass().add("rotulo");
        if (campo instanceof TextField tf) tf.setMaxWidth(Double.MAX_VALUE);
        return new VBox(4, l, campo);
    }

    private static String nulo(String s) { return s == null ? "" : s; }

    private void carregar(Livro l) {
        editando = l;
        titulo.setText(l.getTitulo());
        autor.setText(l.getAutor());
        isbn.setText(l.getIsbn());
        categoria.setText(l.getCategoria());
        exemplares.getValueFactory().setValue(l.getTotalExemplares());
        formTitulo.setText("Editando: " + l.getTitulo());
    }

    private void limpar() {
        editando = null;
        tabela.getSelectionModel().clearSelection();
        titulo.clear();
        autor.clear();
        isbn.clear();
        categoria.clear();
        exemplares.getValueFactory().setValue(1);
        formTitulo.setText("Novo livro");
        titulo.requestFocus();
    }

    private void salvar() {
        try {
            exemplares.increment(0); // força a leitura do texto digitado
        } catch (RuntimeException ex) {
            Alertas.erro("Informe uma quantidade de exemplares válida.");
            return;
        }
        Livro l = editando != null ? editando : new Livro("", "", "", "", 1);
        String tAnt = l.getTitulo(), aAnt = l.getAutor(), iAnt = l.getIsbn(), cAnt = l.getCategoria();
        int totAnt = l.getTotalExemplares();
        try {
            l.setTitulo(titulo.getText().trim());
            l.setAutor(autor.getText().trim());
            l.setIsbn(isbn.getText().trim());
            l.setCategoria(categoria.getText().trim());
            l.setTotalExemplares(exemplares.getValue());
            if (editando == null) l.setDisponiveis(exemplares.getValue());
            Biblioteca.get().salvarLivro(l);
            limpar();
            refresh();
            Alertas.info("Acervo", "Livro salvo com sucesso.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            if (editando != null) {
                l.setTitulo(tAnt); l.setAutor(aAnt); l.setIsbn(iAnt); l.setCategoria(cAnt);
                l.setTotalExemplares(totAnt);
            }
            Alertas.erro(ex.getMessage());
        }
    }

    private void excluir() {
        Livro l = tabela.getSelectionModel().getSelectedItem();
        if (l == null) return;
        if (!Alertas.confirmar("Excluir o livro \"" + l.getTitulo() + "\"?")) return;
        try {
            Biblioteca.get().excluirLivro(l);
            limpar();
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Alertas.erro(ex.getMessage());
        }
    }

    public void refresh() {
        dados.setAll(Biblioteca.get().getLivros());
    }

    public Parent getRoot() { return root; }
}
