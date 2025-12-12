package commun.plateau;

import java.io.Serializable;
import java.util.List;

public class Trio implements Serializable {
    private static final long serialVersionUID = 1L; 
    private List<Carte> cartes;
    private int numero;

    public Trio(List<Carte> cartes, int numero) {
        this.cartes = cartes;
        this.numero = numero;
    }

    public List<Carte> getCartes() {
        return cartes;
    }

    public void setCartes(List<Carte> cartes) {
        this.cartes = cartes;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }
}
