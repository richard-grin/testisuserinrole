package fr.rgrin.projetqcm.jsf.admin;

import fr.rgrin.login.entite.Login;
import fr.rgrin.projetqcm.ejb.login.LoginFacade;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;

/**
 * Backing bean pour la page qui permet à un administrateur de supprimer
 * un utilisateur.
 * Les questions et questionnaires créés par cet utilisateur sont attribués
 * à un utilisateur "bidon" pour ne pas les perdre.
 * @author grin
 */
@Named(value = "supprimerUtilisateur")
@RequestScoped
public class SupprimerUtilisateurController {
  private List<Login> logins;

  @EJB
  private LoginFacade loginFacade;

  public SupprimerUtilisateurController() {
  }
  
  /**
   * Retourne tous les logins, sauf celui de l'utilisateur actuel.
   * @return 
   */
  public List<Login> getAllLoginsSaufLoginActuel() {
    return loginFacade.findAllLoginSaufLoginActuel();
  }

  public List<Login> getLogins() {
    return logins;
  }

  public void setLogins(List<Login> logins) {
    this.logins = logins;
  }
  
  public void supprimerLogin(Login login) {
    loginFacade.remove(login);
  }
  
}
