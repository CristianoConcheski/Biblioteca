package com.biblioteca.data;

import com.biblioteca.model.Cliente;
import com.biblioteca.model.Emprestimo;
import com.biblioteca.model.Livro;
import com.biblioteca.model.Usuario;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * Camada de dados e regras de negócio. Persiste tudo em um único arquivo
 * (~/.biblioteca/dados.bin), sem necessidade de banco de dados.
 */
public class Biblioteca implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int PRAZO_PADRAO_DIAS = 14;
    public static final int LIMITE_EMPRESTIMOS_POR_CLIENTE = 3;

    private static final Path ARQUIVO = Paths.get(System.getProperty("user.home"), ".biblioteca", "dados.bin");
    private static Biblioteca instancia;

    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Livro> livros = new ArrayList<>();
    private final List<Emprestimo> emprestimos = new ArrayList<>();
    private long proximoId = 1;

    private Biblioteca() {}

    public static synchronized Biblioteca get() {
        if (instancia == null) {
            instancia = carregar();
        }
        return instancia;
    }

    private static Biblioteca carregar() {
        if (Files.exists(ARQUIVO)) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(ARQUIVO))) {
                return (Biblioteca) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Não foi possível ler os dados, iniciando base nova: " + e.getMessage());
            }
        }
        Biblioteca b = new Biblioteca();
        b.usuarios.add(new Usuario("admin", hash("admin123"), "Administrador"));
        b.salvar();
        return b;
    }

    public synchronized void salvar() {
        try {
            Files.createDirectories(ARQUIVO.getParent());
            Path tmp = ARQUIVO.resolveSibling("dados.tmp");
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tmp))) {
                out.writeObject(this);
            }
            Files.move(tmp, ARQUIVO, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gravar os dados: " + e.getMessage(), e);
        }
    }

    private long novoId() { return proximoId++; }

    private static String hash(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(senha.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // ---------------------------------------------------------------- login

    public Usuario autenticar(String login, String senha) {
        if (login == null || senha == null) return null;
        String h = hash(senha);
        return usuarios.stream()
                .filter(u -> u.getLogin().equalsIgnoreCase(login.trim()) && u.getSenha().equals(h))
                .findFirst().orElse(null);
    }

    // ------------------------------------------------------------- clientes

    public List<Cliente> getClientes() { return new ArrayList<>(clientes); }

    public void salvarCliente(Cliente c) {
        if (c.getNome() == null || c.getNome().isBlank()) throw new IllegalArgumentException("Informe o nome do cliente.");
        String cpf = somenteDigitos(c.getCpf());
        if (cpf.length() != 11) throw new IllegalArgumentException("O CPF deve ter 11 dígitos.");
        c.setCpf(cpf);
        boolean duplicado = clientes.stream().anyMatch(o -> o.getCpf().equals(cpf) && o.getId() != c.getId());
        if (duplicado) throw new IllegalArgumentException("Já existe um cliente com esse CPF.");
        if (c.getEmail() != null && !c.getEmail().isBlank() && !c.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        if (c.getId() == 0) {
            c.setId(novoId());
            clientes.add(c);
        }
        salvar();
    }

    public void excluirCliente(Cliente c) {
        if (emprestimos.stream().anyMatch(e -> e.getClienteId() == c.getId() && e.isAtivo())) {
            throw new IllegalArgumentException("O cliente possui empréstimos em aberto e não pode ser excluído.");
        }
        clientes.removeIf(o -> o.getId() == c.getId());
        salvar();
    }

    private static String somenteDigitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    // --------------------------------------------------------------- acervo

    public List<Livro> getLivros() { return new ArrayList<>(livros); }

    public void salvarLivro(Livro l) {
        if (l.getTitulo() == null || l.getTitulo().isBlank()) throw new IllegalArgumentException("Informe o título.");
        if (l.getAutor() == null || l.getAutor().isBlank()) throw new IllegalArgumentException("Informe o autor.");
        if (l.getTotalExemplares() < 1) throw new IllegalArgumentException("A quantidade de exemplares deve ser ao menos 1.");
        String isbn = l.getIsbn() == null ? "" : l.getIsbn().replaceAll("[\\s-]", "");
        l.setIsbn(isbn);
        if (!isbn.isEmpty()) {
            boolean duplicado = livros.stream().anyMatch(o -> o.getIsbn().equals(isbn) && o.getId() != l.getId());
            if (duplicado) throw new IllegalArgumentException("Já existe um livro com esse ISBN.");
        }
        if (l.getId() == 0) {
            l.setId(novoId());
            livros.add(l);
        } else {
            long emprestados = emprestimos.stream().filter(e -> e.getLivroId() == l.getId() && e.isAtivo()).count();
            if (l.getTotalExemplares() < emprestados) {
                throw new IllegalArgumentException("Há " + emprestados + " exemplar(es) emprestado(s); o total não pode ser menor.");
            }
            l.setDisponiveis(l.getTotalExemplares() - (int) emprestados);
        }
        salvar();
    }

    public void excluirLivro(Livro l) {
        if (emprestimos.stream().anyMatch(e -> e.getLivroId() == l.getId() && e.isAtivo())) {
            throw new IllegalArgumentException("O livro possui exemplares emprestados e não pode ser excluído.");
        }
        livros.removeIf(o -> o.getId() == l.getId());
        salvar();
    }

    // ----------------------------------------------------------- empréstimos

    public List<Emprestimo> getEmprestimos() { return new ArrayList<>(emprestimos); }

    public long contarAtivos(Cliente c) {
        return emprestimos.stream().filter(e -> e.getClienteId() == c.getId() && e.isAtivo()).count();
    }

    public Emprestimo emprestar(Cliente cliente, Livro livro, int diasPrazo) {
        if (cliente == null) throw new IllegalArgumentException("Selecione um cliente.");
        if (livro == null) throw new IllegalArgumentException("Selecione um livro.");
        if (diasPrazo < 1) throw new IllegalArgumentException("O prazo deve ser de pelo menos 1 dia.");
        if (livro.getDisponiveis() < 1) throw new IllegalArgumentException("Não há exemplares disponíveis deste livro.");
        if (emprestimos.stream().anyMatch(e -> e.getClienteId() == cliente.getId() && e.isAtrasado())) {
            throw new IllegalArgumentException("O cliente possui empréstimo em atraso. Regularize antes de um novo empréstimo.");
        }
        if (contarAtivos(cliente) >= LIMITE_EMPRESTIMOS_POR_CLIENTE) {
            throw new IllegalArgumentException("Limite de " + LIMITE_EMPRESTIMOS_POR_CLIENTE + " empréstimos simultâneos atingido.");
        }
        boolean jaTem = emprestimos.stream()
                .anyMatch(e -> e.getClienteId() == cliente.getId() && e.getLivroId() == livro.getId() && e.isAtivo());
        if (jaTem) throw new IllegalArgumentException("O cliente já está com um exemplar deste livro.");

        LocalDate hoje = LocalDate.now();
        Emprestimo emp = new Emprestimo(cliente, livro, hoje, hoje.plusDays(diasPrazo));
        emp.setId(novoId());
        emprestimos.add(emp);
        livro.setDisponiveis(livro.getDisponiveis() - 1);
        salvar();
        return emp;
    }

    public Emprestimo devolver(Emprestimo emp) {
        if (emp == null) throw new IllegalArgumentException("Selecione um empréstimo.");
        if (!emp.isAtivo()) throw new IllegalArgumentException("Este empréstimo já foi devolvido.");
        emp.registrarDevolucao(LocalDate.now());
        livros.stream().filter(l -> l.getId() == emp.getLivroId()).findFirst()
                .ifPresent(l -> l.setDisponiveis(l.getDisponiveis() + 1));
        salvar();
        return emp;
    }
}
