package commun.action;

public class ActionMin extends Action {
    private int idCible;
    private static final long serialVersionUID = 1L; 

    public ActionMin(int idJoueur, int idCible) {
        super(idJoueur);
        this.idCible = idCible;
    }

    public int getIdCible() {
        return idCible;
    }

    public void setIdCible(int idCible) {
        this.idCible = idCible;
    }
}
