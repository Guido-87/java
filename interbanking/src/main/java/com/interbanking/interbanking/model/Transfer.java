package com.interbanking.interbanking.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table
public class Transfer {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private int importe;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_empresa", referencedColumnName = "id")
    private Company empresa;

    @Column
    private int cuentaDebito;

    @Column
    private int cuentaCredito;

    @Column
    private Date fecha;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getImporte() {
        return importe;
    }

    public void setImporte(int importe) {
        this.importe = importe;
    }

    public Company getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Company empresa) {
        this.empresa = empresa;
    }

    public int getCuentaDebito() {
        return cuentaDebito;
    }

    public void setCuentaDebito(int cuentaDebito) {
        this.cuentaDebito = cuentaDebito;
    }

    public int getCuentaCredito() {
        return cuentaCredito;
    }

    public void setCuentaCredito(int cuentaCredito) {
        this.cuentaCredito = cuentaCredito;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}
