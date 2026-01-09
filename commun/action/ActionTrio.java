package commun.action;

import java.util.ArrayList;
import java.util.List;

public class ActionTrio extends Action {
    
    private static final long serialVersionUID = 1L; 
    private List<Integer> idsCartes;  
    private List<Integer> proprietaires;  
    
    public ActionTrio(int idJoueur, List<Integer> idsCartes) {
        super(idJoueur);
        this.idsCartes = idsCartes;
        this.proprietaires = new ArrayList<>();
    }
    
    public ActionTrio(int idJoueur, List<Integer> idsCartes, List<Integer> proprietaires) {
        super(idJoueur);
        this.idsCartes = idsCartes;
        this.proprietaires = proprietaires;
    }
    
    public List<Integer> getIdsCartes() {
        return idsCartes;
    }
    
    public void setIdsCartes(List<Integer> idsCartes) {
        this.idsCartes = idsCartes;
    }
    
    public List<Integer> getProprietaires() {
        return proprietaires;
    }
    
    public void setProprietaires(List<Integer> proprietaires) {
        this.proprietaires = proprietaires;
    }
}
