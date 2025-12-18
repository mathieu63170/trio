package client;

import commun.plateau.Joueur;
import commun.plateau.Plateau;
import java.util.stream.IntStream;

public class Modele {
    private Plateau plateau;
    private Joueur monJoueur;

    public Modele(Plateau plat) {
    	plateau = plat;
    }

    public void demarrerPartie() {
        // Logique pour démarrer la partie
        // Distribuer les cartes, etc.
    }
    
    public Joueur getMonJoueur() {
    	return monJoueur;
    }

    public Joueur getJoueurSuivant() {
    	Joueur joueurSuivant = plateau.getJoueurs().get((IntStream.range(0, plateau.getJoueurs().size()).filter(i -> plateau.getJoueurs().get(i).getId() == monJoueur.getId()).findFirst().orElse(-1) + 1) % plateau.getJoueurs().size());
        return joueurSuivant;
    }
    
    public Plateau getPlateau() {
    	return plateau;
    }
}
