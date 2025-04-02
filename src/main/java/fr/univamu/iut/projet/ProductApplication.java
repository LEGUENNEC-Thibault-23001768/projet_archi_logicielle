package fr.univamu.iut.projet;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;


@ApplicationPath("/api")
@ApplicationScoped
public class ProductApplication extends Application {

    @Produces
    private ProductRepositoryInterface openDbConnection(){
        ProductRepositoryMariadb db = null;
        try{
            db = new ProductRepositoryMariadb("jdbc:mariadb://mysql-architecture-exam.alwaysdata.net/architecture-exam_produits", "398207", "lechatrouge13");
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
        return db;
    }


    private void closeDbConnection(@Disposes  ProductRepositoryInterface productRepo ) {
        productRepo.close();
    }
}