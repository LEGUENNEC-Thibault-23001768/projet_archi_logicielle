package fr.univamu.iut.projet.paniers.entity;

/**
 * Classe représentant un utilisateur
 */
public class User {
    /**
     * Identifiant de l'utilisateur
     */
    protected String id;
    /**
     * Nom de l'utilisateur
     */
    protected String nom;
    /**
     * Mot de passe de l'utilisateur
     */
    protected String password;
    /**
     * email de l'utilisateur
     */
    protected String email;
    /**
     * role de l'utilisateur
     */
    protected String role;

    /**
     * Constructeur par défaut
     */
    public User() {
    }

    /**
     * Constructeur de utilisateur
     * @param id identifiant de l'utilisateur
     * @param nom nom de l'utilisateur
     * @param password mot de passe de l'utilisateur
     * @param email email de l'utilisateur
     * @param role role de l'utilisateur
     */
    public User(String id, String nom, String password, String email, String role) {
        this.id = id;
        this.nom = nom;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    /**
     * Méthode permettant d'accéder à l'id de l'utilisateur
     * @return un entier avec l'id utilisateur
     */
    public String getId() {
        return id;
    }

    /**
     * Méthode permettant de modifier l'id de l'utilisateur
     * @param id un entier avec l'id utilisateur
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Méthode permettant d'accéder au nom de l'utilisateur
     * @return une chaîne de caractères avec le nom de l'utilisateur
     */
    public String getNom() {
        return nom;
    }

    /**
     * Méthode permettant de modifier le nom de l'utilisateur
     * @param nom une chaîne de caractères avec le nom de l'utilisateur
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Méthode permettant d'accéder au mot de passe de l'utilisateur
     * @return une chaîne de caractères avec le mot de passe de l'utilisateur
     */
    public String getPassword() {
        return password;
    }

    /**
     * Méthode permettant de modifier le mot de passe de l'utilisateur
     * @param password une chaîne de caractères avec le mot de passe de l'utilisateur
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Méthode permettant d'accéder au mail de l'utilisateur
     * @return une chaîne de caractères avec le mail de l'utilisateur
     */
    public String getEmail() {
        return email;
    }

    /**
     * Méthode permettant de modifier le mail de l'utilisateur
     * @param email une chaîne de caractères avec le mail de l'utilisateur
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Méthode permettant d'accéder au role de l'utilisateur
     * @return une chaîne de caractères avec le role de l'utilisateur
     */
    public String getRole() {
        return role;
    }

    /**
     * Méthode permettant de modifier le role de l'utilisateur
     * @param role une chaîne de caractères avec le role de l'utilisateur
     */
    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}