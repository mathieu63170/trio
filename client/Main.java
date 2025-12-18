package client;

import commun.plateau.Carte;
import commun.plateau.Joueur;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        // Créer le modèle
        Modele modele = new Modele();

        // Ajouter des joueurs avec des cartes
        ArrayList<Carte> deck1 = new ArrayList<>();
        deck1.add(new Carte(1, false));
        deck1.add(new Carte(2, false));
        deck1.add(new Carte(3, false));
        Joueur joueur1 = new Joueur(1, deck1, new ArrayList<>());

        ArrayList<Carte> deck2 = new ArrayList<>();
        deck2.add(new Carte(1, false));
        deck2.add(new Carte(2, false));
        Joueur joueur2 = new Joueur(2, deck2, new ArrayList<>());

        ArrayList<Carte> deck3 = new ArrayList<>();
        deck3.add(new Carte(2, false));
        deck3.add(new Carte(3, false));
        Joueur joueur3 = new Joueur(3, deck3, new ArrayList<>());

        modele.ajouterJoueur(joueur1);
        modele.ajouterJoueur(joueur2);
        modele.ajouterJoueur(joueur3);

        // Créer la vue et le contrôleur
        Vue vue = new Vue(modele, null);
        Controleur controleur = new Controleur(modele, vue);

        // Lier le contrôleur à la vue
        // (Déjà fait dans Vue)

        // Démarrer la partie
        modele.demarrerPartie();

        // Initialiser les affichages
        ArrayList<Carte> cartesCentre = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            cartesCentre.add(new Carte(i % 3 + 1, false));
        }
        modele.getPlateau().setMillieu(cartesCentre);

        // Mettre à jour la vue
        controleur.mettreAJourVue(); // Appeler une méthode publique pour mettre à jour
    }
}