package serveur;

import commun.action.*;
import commun.plateau.*;
import java.util.List;

/**
 * Classe Regle : gère les règles du jeu Trio
 * Valide les actions des joueurs et applique les règles de jeu
 */
public class Regle {

    // Constantes pour les règles
    private static final int NB_CARTES_MIN = 3;
    private static final int NB_CARTES_MAX = 3;

    /**
     * Applique une action au plateau après validation
     * @param plateau le plateau courant
     * @param action l'action à appliquer
     * @return le plateau modifié
     */
    public Plateau appliquer(Plateau plateau, Action action) {
        if (!verifAction(plateau, action)) {
            System.out.println("Action invalide !");
            return plateau;
        }

        if (action instanceof ActionMin) {
            return appliquerActionMin(plateau, (ActionMin) action);
        } else if (action instanceof ActionMillieu) {
            return appliquerActionMillieu(plateau, (ActionMillieu) action);
        }else if (action instanceof ActionTrio) {
            List<Carte> cartes = plateau.getCarteRevelee(); // Récupérer les cartes concernées par l'action
            verifierTrio(cartes);
        }

        return plateau;
    }

    /**
     * Vérifie si une action est valide selon les règles du jeu
     * @param plateau le plateau courant
     * @param action l'action à vérifier
     * @return true si l'action est valide
     */
    public boolean verifAction(Plateau plateau, Action action) {
        if (action == null || plateau == null) {
            return false;
        }

        if (action instanceof ActionMin) {
            return verifActionMin(plateau, (ActionMin) action);
        } else if (action instanceof ActionMillieu) {
            return verifActionMillieu(plateau, (ActionMillieu) action);
        }

        return false;
    }

    /**
     * Applique une action "Min" (piocher une carte du milieu)
     * @param plateau le plateau courant
     * @param action l'action ActionMin
     * @return le plateau modifié
     */
    private Plateau appliquerActionMin(Plateau plateau, ActionMin action) {
        int idJoueur = action.getIdJoueur();
        int idCible = action.getIdCible();

        // Logique : piocher une carte du milieu et l'ajouter à la main du joueur
        // Placeholder pour la logique métier
        System.out.println("Joueur " + idJoueur + " pioche la carte " + idCible + " du milieu");

        return plateau;
    }

    /**
     * Applique une action "Millieu" (placer une carte au milieu)
     * @param plateau le plateau courant
     * @param action l'action ActionMillieu
     * @return le plateau modifié
     */
    private Plateau appliquerActionMillieu(Plateau plateau, ActionMillieu action) {
        int idJoueur = action.getIdJoueur();
        int place = action.getPlace();

        // Logique : placer une carte du joueur au milieu à une position donnée
        // Placeholder pour la logique métier
        System.out.println("Joueur " + idJoueur + " place une carte au milieu à la position " + place);

        return plateau;
    }

    /**
     * Vérifie si une ActionMin est valide
     * @param plateau le plateau courant
     * @param action l'action ActionMin
     * @return true si l'action est valide
     */
    private boolean verifActionMin(Plateau plateau, ActionMin action) {
        // Vérifier que l'id du joueur est valide
        if (action.getIdJoueur() < 0) {
            return false;
        }

        // Vérifier que la cible existe dans le milieu
        if (action.getIdCible() < 0) {
            return false;
        }

        return true;
    }

    /**
     * Vérifie si une ActionMillieu est valide
     * @param plateau le plateau courant
     * @param action l'action ActionMillieu
     * @return true si l'action est valide
     */
    private boolean verifActionMillieu(Plateau plateau, ActionMillieu action) {
        // Vérifier que l'id du joueur est valide
        if (action.getIdJoueur() < 0) {
            return false;
        }

        // Vérifier que la place est valide (entre 0 et NB_CARTES_MAX-1)
        int place = action.getPlace();
        if (place < 0 || place >= NB_CARTES_MAX) {
            return false;
        }

        return true;
    }

    /**
     * Vérifie s'il existe un trio valide parmi les cartes données
     * @param cartes la liste de cartes à vérifier
     * @return true si un trio est détecté
     */
    public boolean verifierTrio(List<Carte> cartes) {
        if (cartes == null || cartes.size() > 3) {
            return false;
        }
        // Placeholder : logique de vérification de trio
        // Un trio au jeu Trio = 3 cartes formant une suite cohérente
        // (même couleur/forme/remplissage ou tous différents)
        if(plateauu.get)

        return false;
    }

    /**
     * Retourne le nombre de cartes minimum pour former un trio
     */
    public int getNbCartesMinTrio() {
        return NB_CARTES_MIN;
    }

    /**
     * Retourne le nombre de cartes maximum en main
     */
    public int getNbCartesMax() {
        return NB_CARTES_MAX;
    }
}
