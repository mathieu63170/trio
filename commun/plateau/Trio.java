package commun.plateau;

import java.util.List;

public class Trio {
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
