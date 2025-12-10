package serveur;
import commun.action.*;
import commun.plateau.*;

public class StatutJeu {
    Plateau plateau;
    Action actionEnCours;


    public StatutJeu(Plateau plateau, Action actionEnCours) {
        this.plateau = plateau;
        this.actionEnCours = actionEnCours;
    }

    public void demarrerServeur() {
        // Implémentation du démarrage du serveur
    }

    public void gerrerclient() {
        // Implémentation de la gestion des clients
        while (true) {
            // Logique de gestion des clients
        }
    }
}
