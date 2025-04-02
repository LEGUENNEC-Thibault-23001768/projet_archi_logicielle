package fr.univamu.iut.projet.commande.entity;

import java.math.BigDecimal;
import java.sql.Date;


public class Commande {

    /**
     * Statuts possibles d'une commande
     */
    public enum Statut {
        EN_ATTENTE,
        VALIDEE,
        ANNULEE
    }

    private Integer id;
    private Integer clientId;
    private Integer panierId;
    private Date dateRetrait;
    private Integer relaiId;
    private BigDecimal prixTotal;
    private Statut statut;

    public Commande() {
    }

    public Commande(Integer id, Integer clientId, Integer panierId, Date dateRetrait, Integer relaiId, BigDecimal prixTotal, Statut statut) {
        this.id = id;
        this.clientId = clientId;
        this.panierId = panierId;
        this.dateRetrait = dateRetrait;
        this.relaiId = relaiId;
        this.prixTotal = prixTotal;
        this.statut = statut;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public Integer getPanierId() {
        return panierId;
    }

    public void setPanierId(Integer panierId) {
        this.panierId = panierId;
    }

    public Date getDateRetrait() {
        return dateRetrait;
    }

    public void setDateRetrait(Date dateRetrait) {
        this.dateRetrait = dateRetrait;
    }

    public Integer getRelaiId() {
        return relaiId;
    }

    public void setRelaiId(Integer relaiId) {
        this.relaiId = relaiId;
    }

    public BigDecimal getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(BigDecimal prixTotal) {
        this.prixTotal = prixTotal;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", panierId=" + panierId +
                ", dateRetrait=" + dateRetrait +
                ", relaiId=" + relaiId +
                ", prixTotal=" + prixTotal +
                ", statut=" + statut +
                '}';
    }
}