package client;

import commun.plateau.Carte;
import commun.plateau.Joueur;
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
            Carte carte = cartesCentre.get(index);
            carte.setRevelee(true);
            int valeur = carte.getValeur();
            vue.revelerCarteCentre(index, valeur);
            mettreAJourVue();
        }
    }

    public void revelerCarteJoueur(int valeur) {
        // Pour le joueur actuel, révéler sa carte
        Joueur joueur = modele.getMonJoueur();
        if (joueur != null) {
            for (Carte carte : joueur.getDeck()) {
                if (carte.getValeur() == valeur && !carte.isRevelee()) {
                    carte.setRevelee(true);
                    mettreAJourVue();
                    break;
                }
            }
        }
    }
    
    public void boutonVerifier() {
        List<Carte> cartesRevelees = modele.getPlateau().getCarteRevelee();
        if (cartesRevelees.size() >= 3) {
            vue.afficherMessage("Vérification du trio... " + cartesRevelees.size() + " cartes révélées");
        } else {
            vue.afficherMessage("Révélez au moins 3 cartes pour former un trio!");
        }
    }
    
    

    public void revelerCarteAutreJoueur(int idJoueur, boolean plusPetite) {
        List<Joueur> joueurs = modele.getPlateau().getJoueurs();
        for (Joueur j : joueurs) {
            if (j.getId() == idJoueur) {
                List<Carte> deck = j.getDeck();
                if (!deck.isEmpty()) {
                    Carte carteAReveler;
                    if (plusPetite) {
                        carteAReveler = Collections.min(deck, (c1, c2) -> Integer.compare(c1.getValeur(), c2.getValeur()));
                    } else {
                        carteAReveler = Collections.max(deck, (c1, c2) -> Integer.compare(c1.getValeur(), c2.getValeur()));
                    }
                    carteAReveler.setRevelee(true);
                    vue.afficherMessage("Carte du joueur " + j.getNom() + " révélée: " + carteAReveler.getValeur());
                    mettreAJourVue();
                }
                break;
            }
        }
    }

    public void mettreAJourVue() {
        vue.afficherJoueurs(modele.getPlateau().getJoueurs());
        vue.afficherCartesCentre(modele.getPlateau().getMillieu());
        // Afficher les cartes du joueur actuel
        if (modele.getMonJoueur() != null && modele.getMonJoueur().getDeck() != null) {
            vue.afficherCartesJoueur(modele.getMonJoueur().getDeck());
        }
    }
}
