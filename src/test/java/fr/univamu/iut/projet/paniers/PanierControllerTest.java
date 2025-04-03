/*package fr.univamu.iut.projet.paniers.controller;

import fr.univamu.iut.projet.paniers.entity.Panier;
import fr.univamu.iut.projet.paniers.service.PanierService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PanierControllerTest {

    @Mock
    private PanierService panierService;

    @InjectMocks
    private PanierController panierController;

    private Panier panier1;
    private Panier panier2;

    @BeforeEach
    void setUp() {
        panier1 = new Panier();
        panier1.setId(1);
        panier1.setNomClient("Client Test 1");

        panier2 = new Panier();
        panier2.setId(2);
        panier2.setNomClient("Client Test 2");
    }

    @Test
    void getAllPaniers_shouldReturnListOfPaniers() {
        List<Panier> paniers = Arrays.asList(panier1, panier2);
        when(panierService.getAllPaniers()).thenReturn(paniers);

        Response response = panierController.getAllPaniers();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(paniers, response.getEntity());
        verify(panierService).getAllPaniers();
    }

    @Test
    void getPanierById_whenPanierExists_shouldReturnPanier() {
        when(panierService.getPanierById(1)).thenReturn(panier1);

        Response response = panierController.getPanierById(1);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(panier1, response.getEntity());
        verify(panierService).getPanierById(1);
    }

    @Test
    void getPanierById_whenPanierDoesNotExist_shouldReturnNotFound() {
        when(panierService.getPanierById(99)).thenReturn(null);

        Response response = panierController.getPanierById(99);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(panierService).getPanierById(99);
    }

    @Test
    void createPanier_shouldReturnCreatedPanier() {
        Panier newPanier = new Panier();
        newPanier.setNomClient("Nouveau Client");
        when(panierService.createPanier(any(Panier.class))).thenReturn(panier1); // Assume panier1 is the created one with ID

        Response response = panierController.createPanier(newPanier);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(panier1, response.getEntity());
        verify(panierService).createPanier(newPanier);
    }

    @Test
    void updatePanier_whenPanierExists_shouldReturnUpdatedPanier() {
        Panier updatedDetails = new Panier();
        updatedDetails.setNomClient("Client Mis à Jour");
        when(panierService.updatePanier(eq(1), any(Panier.class))).thenReturn(panier1); // Assume panier1 is the updated one

        Response response = panierController.updatePanier(1, updatedDetails);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(panier1, response.getEntity());
        verify(panierService).updatePanier(eq(1), eq(updatedDetails));
    }

    @Test
    void updatePanier_whenPanierDoesNotExist_shouldReturnNotFound() {
        Panier updatedDetails = new Panier();
        updatedDetails.setNomClient("Client Mis à Jour");
        when(panierService.updatePanier(eq(99), any(Panier.class))).thenReturn(null);

        Response response = panierController.updatePanier(99, updatedDetails);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(panierService).updatePanier(eq(99), eq(updatedDetails));
    }

    @Test
    void deletePanier_shouldReturnNoContent() {
        doNothing().when(panierService).deletePanier(1);

        Response response = panierController.deletePanier(1);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(panierService).deletePanier(1);
    }
}*/