package com.interbanking.interbanking.model;

import jakarta.persistence.*;

import java.math.BigInteger;
import java.util.Date;

@Entity
@Table
public class Company {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private BigInteger cuit;

    @Column
    private String razonSocial;

    @Column
    private Date fechaAdhesion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigInteger getCuit() {
        return cuit;
    }

    public void setCuit(BigInteger cuit) {
        this.cuit = cuit;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public Date getFechaAdhesion() {
        return fechaAdhesion;
    }

    public void setFechaAdhesion(Date fechaAdhesion) {
        this.fechaAdhesion = fechaAdhesion;
    }
}
