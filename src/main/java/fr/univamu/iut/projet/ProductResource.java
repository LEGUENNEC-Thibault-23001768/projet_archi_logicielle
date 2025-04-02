package fr.univamu.iut.projet;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


/**
 * Ressource associée aux produits
 * (point d'accès de l'API REST)
 */
@Path("/products")
@ApplicationScoped
public class ProductResource {

    /**
     * Service utilisé pour accéder aux données des produits et récupérer/modifier leurs informations
     */
    private ProductService service;

    /**
     * Constructeur par défaut
     */
    public ProductResource(){}


    /**
     * Constructeur permettant d'initialiser le service avec une interface d'accès aux données
     * @param productRepo objet implémentant l'interface d'accès aux données
     */
    public @Inject ProductResource(ProductRepositoryInterface productRepo ){
        this.service = new ProductService( productRepo) ;
    }

    /**
     * Constructeur permettant d'initialiser le service d'accès aux produits
     */
    public ProductResource(ProductService service ){
        this.service = service;
    }

    /**
     * Enpoint permettant de publier de tous les produits enregistrés
     * @return la liste des produits (avec leurs informations) au format JSON
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllProducts() {
        return service.getAllProductsJSON();
    }

    /**
     * Endpoint permettant de publier les informations d'un produit dont la référence est passée paramètre dans le chemin
     * @param id référence du produit recherché
     * @return les informations du produit recherché au format JSON
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getProduct( @PathParam("id") String id){

        String result = service.getProductJSON(id);

        // si le produit n'a pas été trouvé
        if( result == null )
            throw new NotFoundException();

        return result;
    }

    /**
     * Endpoint permettant de mettre à jours un produit
     * @param id la référence du produit dont il faut changer les informations
     * @param product le produit transmis en HTTP au format JSON et convertit en objet Product
     * @return une réponse "updated" si la mise à jour a été effectuée, une erreur NotFound sinon
     */
    @PUT
    @Path("{id}")
    @Consumes("application/json")
    public Response updateProduct(@PathParam("id") String id, Product product ){

        // si le produit n'a pas été trouvé
        if( ! service.updateProduct(id, product) )
            throw new NotFoundException();
        else
            return Response.ok("updated").build();
    }

    /**
     * Endpoint permettant de supprimer un produit
     * @param id la référence du produit à supprimer
     * @return une réponse "deleted" si la suppression a été effectuée, une erreur NotFound sinon
     */
    @DELETE
    @Path("{id}")
    public Response deleteProduct(@PathParam("id") String id ){

        // si le produit n'a pas été trouvé
        if( ! service.deleteProduct(id) )
            throw new NotFoundException();
        else
            return Response.ok("deleted").build();
    }

    /**
     * Endpoint permettant de creer un produit
     * @param product le produit transmis en HTTP au format JSON et convertit en objet Product
     * @return une réponse "created" si la création a été effectuée, une erreur Conflict sinon
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProduct(Product product ){

        // si le produit n'a pas pu être créé (id déjà utilisé)
        if( ! service.createProduct(product) )
            return Response.status(Response.Status.CONFLICT).build();
        else
            return Response.ok("created").build();
    }
}