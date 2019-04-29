package fr.rgrin.projetqcm.jsf.login;

import fr.rgrin.login.entite.Groupe;
import fr.rgrin.projetqcm.ejb.login.LoginFacade;
import java.security.Principal;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.Password;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.security.enterprise.AuthenticationStatus.SEND_CONTINUE;
import static javax.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import static javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters.withParams;
import javax.servlet.ServletException;

/**
 * Backing bean pour la page de login avec un formulaire personnalisé (custom).
 *
 * @author grin
 */
@Named(value = "loginBean")
@RequestScoped
public class LoginBean {

  @Inject
  private SecurityContext securityContext;
  @Inject
  private FacesContext facesContext;
  @Inject
  private ExternalContext externalContext;
//  @Inject
//  private AccueilController accueilController;
  @Inject
  private Principal principal;
  @Inject
  private HttpServletRequest request;

  @EJB
  private LoginFacade loginFacade;

//  @Inject
//  private LoginFacade loginFacade;
  private String nom;
  private String motDePasse;

  /**
   * Get the value of motDePasse
   *
   * @return the value of motDePasse
   */
  public String getMotDePasse() {
    return motDePasse;
  }

  /**
   * Set the value of motDePasse
   *
   * @param motDePasse new value of motDePasse
   */
  public void setMotDePasse(String motDePasse) {
    this.motDePasse = motDePasse;
  }

  /**
   * Get the value of nom
   *
   * @return the value of nom
   */
  public String getNom() {
    return nom;
  }

  /**
   * Set the value of nom
   *
   * @param nom new value of nom
   */
  public void setNom(String nom) {
    this.nom = nom;
  }

  public void login() {
    Credential credential
            = new UsernamePasswordCredential(nom, new Password(motDePasse));
    AuthenticationStatus status = securityContext.authenticate(
            (HttpServletRequest) externalContext.getRequest(),
            (HttpServletResponse) externalContext.getResponse(),
            withParams().credential(credential).rememberMe(true));
    if (status.equals(SEND_CONTINUE)) { // ??**??
      // Récupère les infos sur le login et le range dans le bean de porté
      // session accueilController.
//      String nomUtilisateur = nom;
//              = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
      // Récupère le login et la personne associée (mode eager pour
      // les associations N-1).
//      if (nomUtilisateur != null) {
//      Login login = loginFacade.find(nom);
//      accueilController.setLogin(login);
      facesContext.responseComplete();
    } else if (status.equals(SEND_FAILURE)) {
      addError(facesContext, "Nom / mot de passe incorrects");
    }
  }

  public String deconnect() {
    try {
      request.logout();
    } catch (ServletException ex) {
      Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
    }
    request.getSession().invalidate();
//    System.out.println("********DECONNEXION !!!!");
//    try {
//      ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).logout();
//    } catch (ServletException ex) {
//      Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
//    }
    return "/index";
  }

  public String getNomUtilisateur() {
    return principal.getName();
  }

  /**
   * Ajoute une erreur à afficher dans la page de login.
   *
   * @param facesContext
   * @param authentication_failed
   */
  private void addError(FacesContext facesContext,
          String message) {
    facesContext.addMessage(
            null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    message,
                    null));
  }

  public List<Groupe> getGroupesUtilisateur() {
    return loginFacade.findGroupesUtilisateur(principal.getName());
  }

  public boolean isInAnyRole(HttpServletRequest request, String... roles) {
    System.out.println("0000000000000000000000 IsAnyInRole :: REQUEST : " + request);
    System.out.println("USER : " + request.getUserPrincipal());
    if (request.getUserPrincipal() == null) {
      return false;
    }
    for (String role : roles) {
      System.out.println("Nom du role ===== " + role);
      System.out.println("NOM du USER : " + request.getUserPrincipal().getName());
      if (request.isUserInRole(role)) {
        System.out.println("request.isUserInRole(" + role + ") retourne true");
        return true;
      } else {
        System.out.println("request.isUserInRole(" + role + ") retourne false");
      }
    }
    System.out.println("IsAnyInRole retourne false");
    return false;
  }

//  public boolean isAdmin(HttpServletRequest request) {
//    System.out.println("0000000000000000000000 IsAnyInRole :: REQUEST : " + request);
//    System.out.println("USER : " + request.getUserPrincipal());
//    if (request.getUserPrincipal() == null) {
//      return false;
//    }
//    if (request.isUserInRole("admin")) {
//        System.out.println("request.isUserInRole(admin) retourne true");
//        return true;
//      } else {
//        System.out.println("request.isUserInRole(admin) retourne false");
//        return false;
//      }
//    
////    return isInAnyRole(request, "admin");
//  }
  public boolean isAdmin3(HttpServletRequest request) {
    return request.isUserInRole("admin");
  }

  public boolean isAdmin4() {
    return request.isUserInRole("admin");
  }

  public boolean isAdmin() {
    List<String> nomsGroupesUtilisateur
            = loginFacade.findNomsGroupesUtilisateur(principal.getName());
    return nomsGroupesUtilisateur.contains("admin");
  }

  public boolean isAdmin2() {
    System.out.println("les noms des groupes de l'utilisateur avec isAdmin 2 :");
    System.out.println(loginFacade.findNomsGroupesUtilisateur(principal.getName()));
    List<String> groupesUtilisateur
            = loginFacade.findGroupesUtilisateur(principal.getName())
                    .stream()
                    .map(g -> g.getNom())
                    .collect(Collectors.toList());
    System.out.println("les noms des groupes de l'utilisateur avec isAdmin :");
    System.out.println(groupesUtilisateur);
    return groupesUtilisateur.contains("admin");
//    return isInAnyRole(request, "admin");
  }

//  /**
//   * Exécuté quand l'utilisateur a oublié son mot de passe.
//   *
//   * @return
//   */
//  public String oubliMotDePasse() {
//    return "/login/oubliMotDePasse";
//  }
//
//  public String inscription() {
//    return "/login/inscription?faces-redirect=true";
//  }
}
