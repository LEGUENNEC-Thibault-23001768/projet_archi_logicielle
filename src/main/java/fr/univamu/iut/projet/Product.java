package fr.univamu.iut.projet;

/**
 * Classe représentant un produit
 */
public class Product {

    /**
     * id du produit
     */
    protected String id;

    /**
     * Nom du produit
     */
    protected String nom;

    /**
     * description du produit
     */
    protected String description;

    /**
     * prix du produit
     */
    protected double prix;

    /**
     * unité du produit
     */
    protected String unite;

    /**
     * stock du produit
     */
    protected double stock;

    /**
     * categorie du produit
     */
    protected String categorie;


    /**
     * Constructeur par défaut
     */
    public Product(){
    }

    /**
     * Constructeur de produit
     * @param id id du produit
     * @param nom nom du produit
     * @param description description du produit
     * @param prix prix du produit
     * @param unite unité du produit
     * @param stock stock du produit
     * @param categorie categorie du produit
     */
    public Product(String id, String nom, String description, double prix, String unite, double stock, String categorie){
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.unite = unite;
        this.stock = stock;
        this.categorie = categorie;
    }

    /**
     * Méthode permettant d'accéder à l'id du produit
     * @return un chaîne de caractères avec l'id du produit
     */
    public String getId() {
        return id;
    }

    /**
     * Méthode permettant d'accéder au nom du produit
     * @return un chaîne de caractères avec le nom du produit
     */
    public String getNom() {
        return nom;
    }

    /**
     * Méthode permettant d'accéder à la description du produit
     * @return un chaîne de caractères avec la description du produit
     */
    public String getDescription() {
        return description;
    }

    /**
     * Méthode permettant d'accéder au prix du produit
     * @return un double avec le prix du produit
     */
    public double getPrix() {
        return prix;
    }

    /**
     * Méthode permettant d'accéder à l'unité du produit
     * @return un chaîne de caractères avec l'unité du produit
     */
    public String getUnite() {
        return unite;
    }

    /**
     * Méthode permettant d'accéder au stock du produit
     * @return un double avec le stock du produit
     */
    public double getStock() {
        return stock;
    }

    /**
     * Méthode permettant d'accéder à la categorie du produit
     * @return un chaîne de caractères avec la categorie du produit
     */
    public String getCategorie() {
        return categorie;
    }

    /**
     * Méthode permettant de modifier l'id du produit
     * @param id une chaîne de caractères avec l'id à utiliser
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Méthode permettant de modifier le nom du produit
     * @param nom une chaîne de caractères avec le nom à utiliser
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Méthode permettant de modifier la description du produit
     * @param description une chaîne de caractères avec la description à utiliser
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Méthode permettant de modifier le prix du produit
     * @param prix un double avec le prix à utiliser
     */
    public void setPrix(double prix) {
        this.prix = prix;
    }

    /**
     * Méthode permettant de modifier l'unité du produit
     * @param unite une chaîne de caractères avec l'unité à utiliser
     */
    public void setUnite(String unite) {
        this.unite = unite;
    }

    /**
     * Méthode permettant de modifier le stock du produit
     * @param stock un double avec le stock à utiliser
     */
    public void setStock(double stock) {
        this.stock = stock;
    }

    /**
     * Méthode permettant de modifier la categorie du produit
     * @param categorie une chaîne de caractères avec la categorie à utiliser
     */
    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", unite='" + unite + '\'' +
                ", stock=" + stock +
                ", categorie='" + categorie + '\'' +
                '}';
    }
}