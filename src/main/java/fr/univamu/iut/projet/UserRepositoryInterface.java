package fr.univamu.iut.projet;

import java.util.ArrayList;

public interface UserRepositoryInterface {
    /**
     *  Méthode fermant le dépôt où sont stockées les informations sur les utilisateurs
     */
    public void close();

    /**
     * Méthode retournant l'utilisateur dont l'id est passé en paramètre
     * @param id identifiant de l'utilisateur recherché
     * @return un objet User représentant l'utilisateur recherché
     */
    public User getUser( String id );

    /**
     * Méthode permettant de récupérer un utilisateur via son email
     * @param email email de l'utilisateur recherché
     * @return un objet User représentant l'utilisateur recherché
     */
    public User getUserByEmail(String email);


    /**
     * Méthode retournant la liste des utilisateurs
     * @return une liste d'objets utilisateurs
     */
    public ArrayList<User> getAllUsers() ;

    /**
     * Méthode permettant de mettre à jours un utilisateur enregistré
     * @param id identifiant de l'utilisateur à mettre à jours
     * @param nom nouveau nom de l'utilisateur
     * @param password nouveau password de l'utilisateur
     * @param email nouveau email de l'utilisateur
     * @param role nouveau role de l'utilisateur
     * @return true si l'utilisateur existe et la mise à jours a été faite, false sinon
     */
    public boolean updateUser(String id, String nom, String password, String email, String role);

    /**
     * Méthode permettant de supprimer un utilisateur enregistré
     * @param id identifiant de l'utilisateur à supprimer
     * @return true si l'utilisateur existe et la suppression a été faite, false sinon
     */
    public boolean deleteUser(String id);

    /**
     * Méthode permettant de creer un utilisateur
     * @param user utilisateur à creer
     * @return true si l'utilisateur a été creer, false sinon
     */
    public boolean createUser(User user);
}