package serveur;

import commun.action.*;
import commun.plateau.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe Regle : gère les règles du jeu Trio
 */
public class Regle {

    private static final int NB_CARTES_MIN = 3;
    private static final int NB_CARTES_MAX = 3; // Max cartes révélées simultanément

    /**
     * Applique une action au plateau après validation
     */
    public Plateau appliquer(Plateau plateau, Action action) {
        if (!verifAction(plateau, action)) {
            System.out.println("Action invalide ou joueur incorrect !");
            return plateau;
        }

        if (action instanceof ActionMin) {
            return appliquerActionMin(plateau, (ActionMin) action);
        } else if (action instanceof ActionMillieu) {
            return appliquerActionMillieu(plateau, (ActionMillieu) action);
        } else if (action instanceof ActionTrio) {
            // Logique principale : Vérification des cartes révélées
            List<Carte> cartesRevelees = plateau.getCarteRevelee();
            
            if (verifierTrio(cartesRevelees)) {
                System.out.println(">>> TRIO VALIDE ! Joueur " + action.getIdJoueur() + " gagne le trio.");
                return gererTrioGagnant(plateau, action.getIdJoueur(), cartesRevelees);
            } else {
                System.out.println(">>> ECHEC : Pas de trio. On cache les cartes.");
                return cacherToutesLesCartes(plateau);
            }
        }

        return plateau;
    }

    public boolean verifAction(Plateau plateau, Action action) {
        if (action == null || plateau == null) return false;
        // Vérifier que c'est bien au tour du joueur qui envoie l'action
        if (plateau.getJoueurActuel() != action.getIdJoueur()) {
            System.out.println("Ce n'est pas le tour du joueur " + action.getIdJoueur());
            return false;
        }
        
        if (action instanceof ActionMin) return verifActionMin(plateau, (ActionMin) action);
        if (action instanceof ActionMillieu) return verifActionMillieu(plateau, (ActionMillieu) action);
        if (action instanceof ActionTrio) return true; // On peut toujours tenter de valider un trio

        return false;
    }

    // --- LOGIQUE MÉTIER ---

    private Plateau appliquerActionMin(Plateau plateau, ActionMin action) {
        // Dans Trio, une action "Min" ou "Max" révèle souvent une carte
        // Ici, on suppose que l'action révèle la carte ciblée
        // Note: Il faudrait idéalement cibler la carte précise dans le deck du joueur cible
        // Pour l'instant, on simule l'action :
        System.out.println("Joueur " + action.getIdJoueur() + " demande à révéler une carte du joueur " + action.getIdCible());
        
        // TODO: Implémenter la logique pour trouver la carte Min/Max précise dans la main du cible
        // Exemple simplifié : on révèle la première carte non révélée (à adapter selon votre logique exacte)
        Joueur cible = plateau.getJoueurs().stream().filter(j -> j.getId() == action.getIdCible()).findFirst().orElse(null);
        if(cible != null && !cible.getDeck().isEmpty()) {
            cible.getDeck().get(0).setRevelee(true); // Exemple temporaire
        }
        return plateau;
    }

    private Plateau appliquerActionMillieu(Plateau plateau, ActionMillieu action) {
        int place = action.getPlace();
        if(place >= 0 && place < plateau.getMillieu().size()) {
            Carte c = plateau.getMillieu().get(place);
            c.setRevelee(true);
            System.out.println("Carte du milieu révélée : " + c.getValeur());
        }
        return plateau;
    }

    private Plateau gererTrioGagnant(Plateau plateau, int idJoueur, List<Carte> cartesTrio) {
        // 1. Ajouter le trio au joueur
        Joueur gagnant = plateau.getJoueurs().stream()
                .filter(j -> j.getId() == idJoueur)
                .findFirst()
                .orElse(null);

        if (gagnant != null) {
            Trio nouveauTrio = new Trio(new ArrayList<>(cartesTrio), cartesTrio.get(0).getValeur());
            if(gagnant.getTrios() == null) gagnant.setTrios(new ArrayList<>());
            gagnant.getTrios().add(nouveauTrio);
        }

        // 2. Retirer ces cartes du plateau (Mains des joueurs et Milieu)
        for (Carte cTrio : cartesTrio) {
            // Retirer du milieu si présente
            plateau.getMillieu().remove(cTrio);
            
            // Retirer des mains des joueurs
            for (Joueur j : plateau.getJoueurs()) {
                j.getDeck().remove(cTrio);
            }
        }
        
        // 3. Vérifier la victoire (Ex: 3 trios ou le trio de 7)
        if (gagnant != null && gagnant.getTrios().size() >= 3) {
            plateau.setPhaseActuelle(Phase.FIN_PARTIE);
            plateau.setGagnant(idJoueur);
        }
        
        return plateau;
    }

    private Plateau cacherToutesLesCartes(Plateau plateau) {
        // On remet toutes les cartes face cachée
        for (Joueur j : plateau.getJoueurs()) {
            for (Carte c : j.getDeck()) c.setRevelee(false);
        }
        for (Carte c : plateau.getMillieu()) c.setRevelee(false);
        
        // Changement de joueur (tour suivant)
        int indexActuel = plateau.getJoueurActuel(); // Supposons que c'est l'index ou l'ID
        // Logique simple pour passer au joueur suivant (à adapter selon vos IDs)
        // plateau.setJoueurActuel((indexActuel + 1) % plateau.getJoueurs().size());
        
        return plateau;
    }

    public boolean verifierTrio(List<Carte> cartes) {
        if (cartes == null || cartes.size() != 3) {
            return false;
        }
        int valeurRef = cartes.get(0).getValeur();
        for (Carte c : cartes) {
            if (c.getValeur() != valeurRef) return false;
        }
        return true;
    }

    // --- VÉRIFICATIONS ---

    private boolean verifActionMin(Plateau plateau, ActionMin action) {
        return action.getIdJoueur() >= 0 && action.getIdCible() >= 0;
    }

    private boolean verifActionMillieu(Plateau plateau, ActionMillieu action) {
        int place = action.getPlace();
        return place >= 0 && place < plateau.getMillieu().size();
    }
}