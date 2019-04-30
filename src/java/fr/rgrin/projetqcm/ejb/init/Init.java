package fr.rgrin.projetqcm.ejb.init;

import fr.rgrin.projetqcm.util.HashMdp;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.annotation.sql.DataSourceDefinitions;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.mail.MailSessionDefinition;
import javax.sql.DataSource;

/**
 * Initialise les 2 bases de données avec des utilisateurs et des données pour
 * les questions et les questionnaires.
 *
 * @author richard
 */
@DataSourceDefinition(
        className = "org.apache.derby.jdbc.ClientDataSource",
        name = "java:app/jdbc/login_javaEE8",
        serverName = "localhost",
        portNumber = 1527,
        user = "xxx",
        password = "yyy",
        databaseName = "db"
)
@Singleton
@Startup
public class Init {

//  @PersistenceContext(unitName = "loginPU")
//  private EntityManager emLogin;
  @Resource(lookup = "java:app/jdbc/login_javaEE8")
  private DataSource dataSourceLogin;

  @Inject
  // Pour coder le mot de passe
  private HashMdp passwordHash;

  /**
   * Ajoute des logins s'il n'y en a pas déjà.
   */
  @PostConstruct
//  @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
  public void init() {
    System.out.println("************************INIT !!!!!!!!!!!!!");
    initLogins();
  }

//  @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
  /**
   * Création des tables pour conserver les login et mots de passe. Créer les
   * tables et les données pour les logins et les tables et les données pour les
   * QCM.
   */
//  private void initLogins() {
////    creationTablesAvecJPA();
//    creationTablesEtLoginsAvecJDBC();
//  }
  /**
   * Crée les tables et les données pour les logins, s'ils n'existent pas déjà.
   */
  private void initLogins() {
    // Création des tables login, groupe et login_groupe
    // Regarde si les tables existent déjà
    String creationTableLoginSql
            = "create table login( "
            + "login varchar(50) primary key, "
            + "mot_de_passe varchar(160), "
            + "email varchar(255), "
            + "statut varchar(10), "
            + "code varchar(15))";
    String creationTableGroupeSql
            = "create table groupe( "
            + "id integer primary key, "
            + "nom varchar(50))";
    String creationTableLoginGroupeSql
            = "create table login_groupe( "
            + "login varchar(50) references login, "
            + "id_groupe integer references groupe, "
            + "primary key(login, id_groupe))";
    String creationTableSequenceSql
            = "create table sequence( "
            + "seq_name varchar(50) primary key, "
            + "seq_count integer)";
    try (Connection c = dataSourceLogin.getConnection()) {
      // Si la table des logins n'existe pas déjà, créer les tables
      if (!existe(c, "LOGIN")) { // Attention, la casse compte !!!
        System.out.println("Création des tables");
        // Remarque : la table SEQUENCE est créée automatiquement si unité de persistance en mode "create" ; pas besoin de la créer
        execute(c, creationTableLoginSql);
        execute(c, creationTableGroupeSql);
        execute(c, creationTableLoginGroupeSql);
        execute(c, creationTableSequenceSql);
      } else {
        System.out.println("Tables existent déjà");
      }

      // Si la table LOGIN n'est pas vide, ne rien faire
      if (vide(c, "login")) {
        System.out.println("============ Table login vide ; initialisation des données dans ls tables");
        // Le mot de passe haché :
        String hashMdp = passwordHash.generate("toto");
//      System.out.println("******===== TAILLE mot de passe haché : " + hashMdp.length());

        // Initialiation de la table SEQUENCE
        execute(c, "INSERT INTO SEQUENCE(SEQ_NAME, SQQ_COUNT) VALUES('SEQ_GEN', 1)");

        // ric appartient aux groupes enseignant et etudiant (pour pouvoir tester)
        execute(c, "INSERT INTO login (LOGIN, MOT_DE_PASSE, email, statut) VALUES('ric', '"
                + hashMdp + "', 'grin@unice.fr', 'ok')");
        execute(c, "INSERT INTO groupe(id, nom) VALUES(1, 'enseignant')");
        execute(c, "INSERT INTO groupe(id, nom)  VALUES(2, 'etudiant')");
        execute(c, "INSERT INTO groupe(id, nom)  VALUES(3, 'admin')");
        execute(c, "INSERT INTO login_groupe(login, id_groupe) VALUES('ric', 1)");
        execute(c, "INSERT INTO login_groupe(login, id_groupe) VALUES('ric', 2)");

        // toto appartient seulement au groupe etudiant
//        execute(c, "INSERT INTO personne (id, nom, prenom) VALUES(2, 'Bernard', 'Pierre')");
        execute(c, "INSERT INTO login (LOGIN, MOT_DE_PASSE, email, statut) VALUES('toto', '"
                + hashMdp + "', 'grin@unice.fr', 'ok')");
        execute(c, "INSERT INTO login_groupe(login, id_groupe) VALUES('toto', 2)");
//      execute(c, "INSERT INTO caller_groups VALUES('juneau', 'group2')");

        // admin appartient seulement au groupe des administrateurs
        execute(c, "INSERT INTO login (LOGIN, MOT_DE_PASSE, email, statut) VALUES('admin', '"
                + hashMdp + "', 'grin@unice.fr', 'ok')");
        execute(c, "INSERT INTO login_groupe(login, id_groupe) VALUES('admin', 3)");
      }
//      if (vide(c, "formation")) {
//        System.out.println("Initialisation des données pour les formations");
//        initQcms();
//      }
    } catch (SQLException e) {
      // Pour les logs du serveur d'application
      e.printStackTrace();
    }
  }

  /**
   * Exécute une requête SQL de création ou suppression de table ou d'insertion
   * de données.
   *
   * @param c connexion à la base de données
   * @param query texte de la requête SQL
   */
  private void execute(Connection c, String query) {
    try (PreparedStatement stmt = c.prepareStatement(query)) {
      stmt.executeUpdate();
    } catch (SQLException e) {
      // Pour les logs du serveur d'application
      e.printStackTrace();
    }
  }

  /**
   * Exécute des requêtes SQL de création ou suppression de table ou d'insertion
   * de données.
   *
   * @param c connexion à la base de données
   * @param query texte de la requête SQL
   */
  private void execute(Connection c, List<String> queries) {
    for (String query : queries) {
      execute(c, query);
    }
  }

  /**
   * Teste si une table existe déjà.
   *
   * @param connection
   * @param nomTable nom de la table (attention, la casse compte).
   * @return true ssi la table existe.
   * @throws SQLException
   */
  private static boolean existe(Connection connection, String nomTable)
          throws SQLException {
    boolean existe;
    DatabaseMetaData dmd = connection.getMetaData();
    try (ResultSet tables = dmd.getTables(connection.getCatalog(), null, nomTable, null)) {
      existe = tables.next();
    }
    return existe;
  }

  /**
   * @param nomTable nom de la table SQL
   * @return true ssi la table est vide.
   */
  private boolean vide(Connection c, String nomTable) throws SQLException {
    Statement stmt = c.createStatement();
    ResultSet rset = stmt.executeQuery("select count(1) from " + nomTable);
    rset.next();
    int nb = rset.getInt(1);
    return nb == 0;
  }

}
