package commun.plateau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Plateau implements Serializable {
    private static final long serialVersionUID = 1L; 
    private List<Joueur> joueurs;
    private List<Carte> millieu;
    private List<CarteRevealee> cartesRevelees;  // Cartes révélées publiquement (MAX/MIN)
    private int joueurActuel;
    private int etapeJoueurActuel;  // Compteur d'étapes du joueur actuel (0-3)
    private Phase phaseActuelle;
    private int gagnant;

    public Plateau(List<Joueur> joueurs, List<Carte> millieu, int joueurActuel, 
                   Phase phaseActuelle, int gagnant) {
        this.joueurs = joueurs;
        this.millieu = millieu;
        this.cartesRevelees = new ArrayList<>();
        this.joueurActuel = joueurActuel;
        this.etapeJoueurActuel = 0;
        this.phaseActuelle = phaseActuelle;
        this.gagnant = gagnant;
    }

    // --- GETTERS & SETTERS (Standards) ---
    public List<Joueur> getJoueurs() { return joueurs; }
    public void setJoueurs(List<Joueur> joueurs) { this.joueurs = joueurs; }
    
    public List<Carte> getMillieu() { return millieu; }
    public void setMillieu(List<Carte> millieu) { this.millieu = millieu; }
    
    public List<CarteRevealee> getCartesRevelees() { return cartesRevelees; }
    public void setCartesRevelees(List<CarteRevealee> cartesRevelees) { this.cartesRevelees = cartesRevelees; }
    
    public int getJoueurActuel() { return joueurActuel; }
    public void setJoueurActuel(int joueurActuel) { this.joueurActuel = joueurActuel; }
    
    public int getEtapeJoueurActuel() { return etapeJoueurActuel; }
    public void setEtapeJoueurActuel(int etape) { this.etapeJoueurActuel = etape; }
    
    public Phase getPhaseActuelle() { return phaseActuelle; }
    public void setPhaseActuelle(Phase phaseActuelle) { this.phaseActuelle = phaseActuelle; }
    
    public int getGagnant() { return gagnant; }
    public void setGagnant(int gagnant) { this.gagnant = gagnant; }

    /**
     * Récupère toutes les cartes actuellement révélées sur tout le plateau
     * (Mains des joueurs + Milieu)
     */
    public List<Carte> getCarteRevelee(){
        List<Carte> liste = new ArrayList<>();
        
        // 1. Chercher dans les mains des joueurs
        if (joueurs != null) {
            for(Joueur j : joueurs){
                for(Carte c : j.getDeck()){
                    if(c.isRevelee()){
                        liste.add(c);
                    }
                }
            }
        }
        
        // 2. Chercher dans le milieu (Correction importante)
        if (millieu != null) {
            for(Carte c : millieu){
                if(c.isRevelee()){
                    liste.add(c);
                }
            }
        }
        
        return liste;
    }
}