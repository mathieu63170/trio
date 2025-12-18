package client;

import commun.plateau.Carte;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Controleur {
    private Modele modele;
    private Vue vue;

    public Controleur(Modele modele, Vue vue) {
        this.modele = modele;
        this.vue = vue;
    }

    public void revelerCarteCentre(int index) {
        List<Carte> cartesCentre = modele.getPlateau().getMillieu();
        if (index < cartesCentre.size()) {
            int valeur = cartesCentre.get(index).getValeur();
            vue.revelerCarteCentre(index, valeur);
        }
    }

    public void revelerCarteJoueur(int valeur) {
        // Pour le joueur actuel, révéler sa carte
        
    }
    
    public void boutonVerifier() {
    	// Ajouter les actions à réaliser quand le joueur veut vérifier un trio
    }
    
    

    public void revelerCarteAutreJoueur(int idJoueur, boolean plusPetite) {
        //dévoiler la carte du joueur idJoueur (plusPetite = false <==> plus grande carte)
    	//passer le paramètre de la carte revelee à true
    	//demander l'actualisation de l'affichage de ce joueur
    }

    public void mettreAJourVue() {
        vue.afficherJoueurs(modele.getPlateau().getJoueurs());
        vue.afficherCartesCentre(modele.getPlateau().getMillieu());
        // Afficher les cartes du joueur actuel
        vue.afficherCartesJoueur(modele.getMonJoueur().getDeck());
    }
}
