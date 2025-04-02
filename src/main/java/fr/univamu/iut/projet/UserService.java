package fr.univamu.iut.projet;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.util.ArrayList;
import java.util.Objects;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepositoryInterface userRepo;

    /**
     * Méthode retournant les informations (sans password) sur les utilisateurs au format JSON
     * @return une chaîne de caractère contenant les informations au format JSON
     */
    public String getAllUsersJSON(){

        ArrayList<User> allUsers = userRepo.getAllUsers();

        // on supprime les informations sur les mots de passe
        for( User currentUser : allUsers ){
            currentUser.setPassword("");
        }

        // création du json et conversion de la liste de livres
        String result = null;
        try( Jsonb jsonb = JsonbBuilder.create()){
            result = jsonb.toJson(allUsers);
        }
        catch (Exception e){
            System.err.println( e.getMessage() );
        }

        return result;
    }

    /**
     * Méthode retournant au format JSON les informations sur un utilisateur recherché
     * @param id l'identifiant de l'utilisateur recherché
     * @return une chaîne de caractère contenant les informations au format JSON
     */
    public String getUserJSON( String id ){
        String result = null;
        User myUser = userRepo.getUser(id);

        // si l'utilisateur a été trouvé
        if( myUser != null ) {
            myUser.setPassword(""); // on supprime les informations sur le mot de passe

            // création du json et conversion du livre
            try (Jsonb jsonb = JsonbBuilder.create()) {
                result = jsonb.toJson(myUser);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return result;
    }

    /**
     * Méthode retournant au format JSON les informations sur un utilisateur recherché par email
     * @param email l'email de l'utilisateur recherché
     * @return une chaîne de caractère contenant les informations au format JSON
     */
    public String getUserByEmailJSON( String email ){
        String result = null;
        User myUser = userRepo.getUserByEmail(email);

        // si l'utilisateur a été trouvé
        if( myUser != null ) {
            myUser.setPassword(""); // on supprime les informations sur le mot de passe

            // création du json et conversion du livre
            try (Jsonb jsonb = JsonbBuilder.create()) {
                result = jsonb.toJson(myUser);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return result;
    }

    /**
     * Méthode permettant de mettre à jours les informations d'un utilisateur
     * @param id référence de l'utilisateur à mettre à jours
     * @param user les nouvelles informations a été utiliser
     * @return true si l'utilisateur a pu être mis à jours
     */
    public boolean updateUser(String id, User user) {
        return userRepo.updateUser(id, user.nom, user.password, user.email, user.role);
    }

    /**
     * Méthode permettant de supprimer un utilisateur
     * @param id référence de l'utilisateur à supprimer
     * @return true si l'utilisateur a pu être supprimé
     */
    public boolean deleteUser(String id) {
        return userRepo.deleteUser(id);
    }

    /**
     * Méthode permettant de creer un utilisateur
     * @param user utilisateur à creer
     * @return true si l'utilisateur a pu être creer
     */
    public boolean createUser(User user) {
        return userRepo.createUser(user);
    }

    /**
     * Attempts to authenticate a user based on email and plain text password.
     * **WARNING: Compares plain text passwords - INSECURE.**
     * @param email The user's email.
     * @param plainPassword The user's plain text password.
     * @return The authenticated User object if successful (with password cleared), null otherwise.
     */
    public User authenticateUser(String email, String plainPassword) {
        User user = userRepo.getUserByEmail(email);

        if (user != null && user.getPassword() != null && plainPassword != null) {

            if (Objects.equals(plainPassword, user.getPassword())) {
                user.setPassword(null);
                return user;
            }
        }
        return null;
    }
}