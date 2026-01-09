package commun.plateau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Représente l'état du jeu : joueurs, cartes au milieu et cartes révélées
public class Plateau implements Serializable {
    private static final long serialVersionUID = 1L; 
    private List<Joueur> joueurs;
    private List<Carte> millieu;
    private List<CarteRevealee> cartesRevelees;  
    private int joueurActuel;
    private int etapeJoueurActuel;  
    private Phase phaseActuelle;
    private int gagnant;

    // Constructeur : initialise l'état du plateau au début d'une manche
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

    
    // Retourne toutes les cartes actuellement révélées (joueurs + milieu)
    public List<Carte> getCarteRevelee(){
        List<Carte> liste = new ArrayList<>();
        
        
        if (joueurs != null) {
            for(Joueur j : joueurs){
                for(Carte c : j.getDeck()){
                    if(c.isRevelee()){
                        liste.add(c);
                    }
                }
            }
        }
        
        
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