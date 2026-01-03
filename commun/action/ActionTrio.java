package commun.action;

public class ActionTrio extends Action {
    
    private static final long serialVersionUID = 1L; 
    private int numeroTrio;
    
    public ActionTrio(int idJoueur, int numeroTrio) {
        super(idJoueur);
        this.numeroTrio = numeroTrio;
    }
    
    public int getNumeroTrio() {
        return numeroTrio;
    }
    
    public void setNumeroTrio(int numeroTrio) {
        this.numeroTrio = numeroTrio;
    }
}
