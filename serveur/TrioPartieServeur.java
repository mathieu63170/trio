package serveur;

import java.util.*;

/**
 * TrioPartieServeur - Gère la logique du jeu côté serveur
 */
public class TrioPartieServeur {
    
    private List<String> joueurs;
    private Map<String, Integer> selections;
    private int joueurActuelIndex;
    private boolean partieActive;
    
    /**
     * Constructeur
     */
    public TrioPartieServeur(List<String> nomJoueurs) {
        this.joueurs = new ArrayList<>(nomJoueurs);
        this.selections = new HashMap<>();
        this.joueurActuelIndex = 0;
        this.partieActive = true;
        
        for (String joueur : joueurs) {
            selections.put(joueur, 0);
        }
    }
    
    /**
     * Sélectionner une carte
     */
    public void selectionnerCarte(String nomJoueur, int index) {
        if (joueurs.contains(nomJoueur)) {
            System.out.println("[PARTIE] " + nomJoueur + " sélectionne la carte " + index);
        }
    }
    
    /**
     * Vérifier le trio
     */
    public void verifierTrio(String nomJoueur) {
        if (joueurs.contains(nomJoueur)) {
            System.out.println("[PARTIE] " + nomJoueur + " vérifie un trio");
        }
    }
    
    /**
     * Annuler la sélection
     */
    public void annulerSelection(String nomJoueur) {
        if (joueurs.contains(nomJoueur)) {
            selections.put(nomJoueur, 0);
            System.out.println("[PARTIE] " + nomJoueur + " annule sa sélection");
        }
    }
    
    /**
     * Obtenir l'état de la partie
     */
    public String getEtat() {
        return "Joueur: " + joueurs.get(joueurActuelIndex) + " | Sélections: " + selections.toString();
    }
}
