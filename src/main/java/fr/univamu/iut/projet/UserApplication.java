package fr.univamu.iut.projet;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
@ApplicationScoped
public class UserApplication extends Application {

    @Produces
    private UserRepositoryInterface openUserDbConnection(){
        UserRepositoryMariadb db = null;
        try{
            db = new UserRepositoryMariadb("jdbc:mariadb://mysql-architecture-exam.alwaysdata.net/architecture-exam_produits", "398207", "lechatrouge13");
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
        return db;
    }


    private void closeUserDbConnection(@Disposes UserRepositoryInterface userRepo ) {
        userRepo.close();
    }
}