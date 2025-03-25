package fr.univamu.iut.projet.entity;

public class PanierProduit {

    private Integer panierProduitId;
    private Integer panierId;
    private String productId;
    private int quantity;
    private String unit;

    public PanierProduit() {
    }

    public PanierProduit(Integer panierProduitId, Integer panierId, String productId, int quantity, String unit) {
        this.panierProduitId = panierProduitId;
        this.panierId = panierId;
        this.productId = productId;
        this.quantity = quantity;
        this.unit = unit;
    }

    public Integer getPanierProduitId() {
        return panierProduitId;
    }

    public void setPanierProduitId(Integer panierProduitId) {
        this.panierProduitId = panierProduitId;
    }

    public Integer getPanierId() {
        return panierId;
    }

    public void setPanierId(Integer panierId) {
        this.panierId = panierId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "PanierProduit{" +
                "panierProduitId=" + panierProduitId +
                ", panierId=" + panierId +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                '}';
    }
}