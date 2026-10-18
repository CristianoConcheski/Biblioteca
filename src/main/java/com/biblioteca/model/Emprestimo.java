package com.biblioteca.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Emprestimo implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final BigDecimal MULTA_POR_DIA = new BigDecimal("1.00");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private long id;
    private final long clienteId;
    private final String clienteNome;
    private final long livroId;
    private final String livroTitulo;
    private final LocalDate dataEmprestimo;
    private final LocalDate dataPrevista;
    private LocalDate dataDevolucao;
    private BigDecimal multaCobrada = BigDecimal.ZERO;

    public Emprestimo(Cliente cliente, Livro livro, LocalDate dataEmprestimo, LocalDate dataPrevista) {
        this.clienteId = cliente.getId();
        this.clienteNome = cliente.getNome();
        this.livroId = livro.getId();
        this.livroTitulo = livro.getTitulo();
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevista = dataPrevista;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getClienteId() { return clienteId; }
    public String getClienteNome() { return clienteNome; }
    public long getLivroId() { return livroId; }
    public String getLivroTitulo() { return livroTitulo; }
    public LocalDate getDataEmprestimo() { return dataEmprestimo; }
    public LocalDate getDataPrevista() { return dataPrevista; }
    public LocalDate getDataDevolucao() { return dataDevolucao; }
    public BigDecimal getMultaCobrada() { return multaCobrada; }

    public boolean isAtivo() { return dataDevolucao == null; }

    public boolean isAtrasado() {
        return isAtivo() && LocalDate.now().isAfter(dataPrevista);
    }

    /** Multa calculada para o dia de hoje (empréstimo ativo) ou a que foi cobrada (devolvido). */
    public BigDecimal getMulta() {
        if (!isAtivo()) return multaCobrada;
        return calcularMulta(LocalDate.now());
    }

    private BigDecimal calcularMulta(LocalDate ate) {
        long dias = ChronoUnit.DAYS.between(dataPrevista, ate);
        return dias > 0 ? MULTA_POR_DIA.multiply(BigDecimal.valueOf(dias)) : BigDecimal.ZERO;
    }

    public void registrarDevolucao(LocalDate data) {
        this.dataDevolucao = data;
        this.multaCobrada = calcularMulta(data);
    }

    public String getStatus() {
        if (!isAtivo()) return "Devolvido";
        return isAtrasado() ? "Atrasado" : "Em andamento";
    }

    public String getDataEmprestimoFmt() { return dataEmprestimo.format(FMT); }
    public String getDataPrevistaFmt() { return dataPrevista.format(FMT); }
    public String getDataDevolucaoFmt() { return dataDevolucao == null ? "-" : dataDevolucao.format(FMT); }
    public String getMultaFmt() { return "R$ " + getMulta().setScale(2).toPlainString().replace('.', ','); }
}
