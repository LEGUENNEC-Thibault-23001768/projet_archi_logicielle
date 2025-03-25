package fr.univamu.iut.projet.entity;

import java.sql.Timestamp;
import java.util.List;

public class Panier {

    private Integer panierId;
    private Timestamp lastUpdateDate;
    private List<PanierProduit> panierProduits;

    public Panier() {
    }

    public Panier(Integer panierId, Timestamp lastUpdateDate) {
        this.panierId = panierId;
        this.lastUpdateDate = lastUpdateDate;
    }

    public Integer getPanierId() {
        return panierId;
    }

    public void setPanierId(Integer panierId) {
        this.panierId = panierId;
    }

    public Timestamp getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Timestamp lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public List<PanierProduit> getPanierProduits() {
        return panierProduits;
    }

    public void setPanierProduits(List<PanierProduit> panierProduits) {
        this.panierProduits = panierProduits;
    }

    @Override
    public String toString() {
        return "Panier{" +
                "panierId=" + panierId +
                ", lastUpdateDate=" + lastUpdateDate +
                ", panierProduits=" + panierProduits +
                '}';
    }
}