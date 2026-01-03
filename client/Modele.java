package client;

import commun.plateau.Joueur;
import commun.plateau.Plateau;
import commun.plateau.Carte;
import commun.plateau.Phase;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class Modele {
    private Plateau plateau;
    private Joueur monJoueur;

    public Modele() {
    	// Initialiser avec un plateau vide pour le moment
    	this.plateau = new Plateau(new ArrayList<>(), new ArrayList<>(), 0, Phase.PREMIERE_MANCHE, -1);
    	this.monJoueur = new Joueur(0, "Vous", new ArrayList<>(), new ArrayList<>());
    }
    
    public Modele(Plateau plat) {
    	plateau = plat;
    }

    public void demarrerPartie() {
        // Logique pour démarrer la partie
        // Distribuer les cartes, etc.
    }
    
    public void ajouterJoueur(Joueur joueur) {
        if (plateau == null) {
            plateau = new Plateau(new ArrayList<>(), new ArrayList<>(), 0, null, -1);
        }
        plateau.getJoueurs().add(joueur);
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
