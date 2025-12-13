import java.sql.*;

//to compile and execute
//javac Item.java
//java -cp "postgresql-42.7.3.jar;" item

public class InitDatabase{
    //clear and initialize all the tables
    public static void initAll(Connection con) throws Exception{
        clearAll(con);

        initMachine(con);
        initMaintenance(con);
        initAbonnement(con);
        initCoach(con);
        // Il faut forcément initialiser d'abord les abonnements et les coachs avant les clients, car les clients utilisent des clés étrangères vers abonnement et coach
        initClient(con);
        // InitAbonnementTemp doit passer apres client et abonnement
        initAbonnementTemp(con);
        // InitSessiobPrivee dois passer après Coach
        initSessionPrivee(con);
        // Table associative entre les sessions privées et les clients. Relation n-n, car un client peut avoir plusieurs sessions privées, et une session privée peut avoir plusieurs clients inscrits
        initClientSessionPrivee(con);
        // Table qui utilise les tables coach et client
        initCoachingPrivee(con);

        System.out.println("InitDatabase: All the initializations were executed without errors.");
    }

    //Init functions
    private static void initMachine(Connection con) throws Exception{
        Statement statTableCreation = con.createStatement();
        statTableCreation.execute("CREATE TABLE proj.machine (id INTEGER PRIMARY KEY, nom TEXT, prochMaintenance DATE, intervalMaintenanceEnJour INTEGER);");
        statTableCreation.close();

        PreparedStatement addToTable = con.prepareStatement(
            "INSERT INTO proj.machine(id, nom, prochMaintenance, intervalMaintenanceEnJour) VALUES (?, ?, ?, ?)"
            );

        int idCount = 1;

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Leg Extension Machine");
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 30);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Leg Curl Machine");
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 45);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Seated Calf Machine");
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 120);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Preacher Curl Machine");
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 30);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Rowing Machine");
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 60);

        addToTable.executeUpdate();

        
        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Lat Pulldown Machine");
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 30);

        addToTable.executeUpdate();

        
        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Smith Machine");
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 60);

        addToTable.executeUpdate();

        
        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Bench Press");
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 90);

        addToTable.executeUpdate();

        
        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Delt Fly Machine");
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 30);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Cable Machine");
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 30);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Declined Crunches Bench");
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 90);

        addToTable.executeUpdate();
        addToTable.close();
    }

    private static void initAbonnement(Connection con) throws Exception{
        Statement statTableCreation = con.createStatement();
        statTableCreation.execute("CREATE TABLE proj.abonnement (id INTEGER PRIMARY KEY, nom TEXT, prix DECIMAL);");
        statTableCreation.close();

        PreparedStatement addToTable = con.prepareStatement(
            "INSERT INTO proj.abonnement(id, nom, prix) VALUES (?, ?, ?)"
            );

        int idCount = 1;

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Pas abonnée");
        addToTable.setDouble(3, 0);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Basic");
        addToTable.setDouble(3, 19.99);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Premium");
        addToTable.setDouble(3, 29.99);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Premium+");
        addToTable.setDouble(3, 39.99);

        addToTable.executeUpdate();
        
        addToTable.close();
    }

    private static void initAbonnementTemp(Connection con) throws Exception{
        Statement statTableCreation = con.createStatement();
        statTableCreation.execute(
            "CREATE TABLE proj.abonnementTemp (id INTEGER PRIMARY KEY, typeAbonnement INTEGER REFERENCES proj.abonnement(id), idClient INTEGER REFERENCES proj.client(id), finAbonnement DATE);"
        );
        statTableCreation.close();
    }

    private static void initClient(Connection con) throws Exception{
        Statement statTableCreation = con.createStatement();
        statTableCreation.execute(
            "CREATE TABLE proj.client (id INTEGER PRIMARY KEY, nom TEXT, prenom TEXT, typeAbonnement INTEGER REFERENCES proj.abonnement(id), finAbonnement DATE, coachAttitrer INTEGER REFERENCES proj.coach(id), solde DECIMAL);"
        );
        statTableCreation.close();

        PreparedStatement addToTable = con.prepareStatement("INSERT INTO proj.client(id, nom, prenom, typeAbonnement, finAbonnement, coachAttitrer, solde) VALUES (?, ?, ?, ?, ?, ?, ?)");

        int idCount = 1;

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Tamby");
        addToTable.setString(3, "Sebastien");
        addToTable.setInt(4, 2);
        addToTable.setDate(5, randomDate());
        addToTable.setInt(6, 1);
        addToTable.setDouble(7,0);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Blaszkow");
        addToTable.setString(3, "Kamil");
        addToTable.setInt(4, 2);
        addToTable.setDate(5, randomDate());
        //Donne à notre table une valeur null avec comme type INTEGER, pour éviter les erreurs de type
        addToTable.setNull(6, java.sql.Types.INTEGER);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Pandion");
        addToTable.setString(3, "Brian");
        addToTable.setInt(4, 4);
        addToTable.setDate(5, randomDate());
        addToTable.setInt(6, 1);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Romano");
        addToTable.setString(3, "Caio");
        addToTable.setInt(4, 3);
        addToTable.setDate(5, randomDate());
        addToTable.setNull(6, java.sql.Types.INTEGER);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Pinto");
        addToTable.setString(3, "Sacha");
        addToTable.setInt(4, 4);
        addToTable.setDate(5, randomDate());

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Roux");
        addToTable.setString(3, "Nicolas");
        addToTable.setInt(4, 2);
        addToTable.setDate(5, randomDate());
        addToTable.setInt(6, 1);

        addToTable.executeUpdate();
        addToTable.close();
    }
    private static void initCoach(Connection con) throws Exception{
        Statement statCreationTable = con.createStatement();
        statCreationTable.execute("CREATE TABLE proj.coach(id INTEGER PRIMARY KEY, nom TEXT, prenom TEXT);");
        statCreationTable.close();

        PreparedStatement addToTable = con.prepareStatement("INSERT INTO proj.coach(id, nom, prenom) VALUES (?, ?, ?)");

        int idCount = 1;

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Chrzascik");
        addToTable.setString(3, "Kevin");

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Wojna");
        addToTable.setString(3, "Pawel");

        addToTable.executeUpdate();
        addToTable.close();
    }

    private static void initSessionPrivee(Connection con) throws Exception{
        Statement statCreationTable = con.createStatement();
        statCreationTable.execute("CREATE TABLE proj.sessionPrivee(id INTEGER PRIMARY KEY, coachAttitrer INTEGER REFERENCES proj.coach(id), dateDebut TIMESTAMP, dureeEnMin INTEGER);");
        statCreationTable.close();

        PreparedStatement addToTable = con.prepareStatement("INSERT INTO proj.sessionPrivee(id, coachAttitrer, dateDebut, dureeEnMin) VALUES (?, ?, ?, ?)");

        int idCount = 1;

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 1);
        addToTable.setTimestamp(3, randomTimestamp());
        addToTable.setInt(4, 90);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 1);
        addToTable.setTimestamp(3, randomTimestamp());
        addToTable.setInt(4, 60);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 1);
        addToTable.setTimestamp(3, randomTimestamp());
        addToTable.setInt(4, 60);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 1);
        addToTable.setTimestamp(3, randomTimestamp());
        addToTable.setInt(4, 150);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 1);
        addToTable.setTimestamp(3, randomTimestamp());
        addToTable.setInt(4, 90);

        addToTable.executeUpdate();
        addToTable.close();
    }

    //Table associative entre les clients et les sessions privées
    private static void initClientSessionPrivee(Connection con) throws Exception{
        Statement statCreationTable = con.createStatement();
        statCreationTable.execute("CREATE TABLE proj.client_sessionPrivee(idClient INTEGER REFERENCES proj.client(id), idSessionPrivee INTEGER REFERENCES proj.sessionPrivee(id));");
        statCreationTable.close();

        PreparedStatement addToTable = con.prepareStatement("INSERT INTO proj.client_sessionPrivee(idClient, idSessionPrivee) VALUES (?, ?)");

        addToTable.setInt(1, 1);
        addToTable.setInt(2, 3);

        addToTable.executeUpdate();

        addToTable.setInt(1, 1);
        addToTable.setInt(2, 4);

        addToTable.executeUpdate();

        addToTable.setInt(1, 3);
        addToTable.setInt(2, 1);

        addToTable.executeUpdate();

        addToTable.setInt(1, 5);
        addToTable.setInt(2, 2);

        addToTable.executeUpdate();

        addToTable.setInt(1, 5);
        addToTable.setInt(2, 5);

        addToTable.executeUpdate();

        addToTable.setInt(1, 6);
        addToTable.setInt(2, 3);

        addToTable.executeUpdate();

        addToTable.setInt(1, 2);
        addToTable.setInt(2, 2);

        addToTable.executeUpdate();
        addToTable.close();
    }
    // Table de sessions privées mais seulement pour un coach avec une personne
    private static void initCoachingPrivee(Connection con) throws Exception{
        Statement generalStat = con.createStatement();
        generalStat.execute(
            "CREATE TABLE proj.CoachingPrivee(id INTEGER PRIMARY KEY, idCoach INTEGER REFERENCES proj.coach(id), idClient INTEGER REFERENCES proj.client(id), dateDebut TIMESTAMP, dureeEnMin INTEGER);"
        );
        generalStat.close();

        PreparedStatement addToTable = con.prepareStatement(
            "INSERT INTO proj.CoachingPrivee(id, idCoach, idClient, dateDebut, dureeEnMin) VALUES (?, ?, ?, ?, ?);"
        );

        int idCount = 1;
        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 1);
        addToTable.setInt(3, 1);
        addToTable.setTimestamp(4, randomTimestamp());
        addToTable.setInt(5, 60);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 1);
        addToTable.setInt(3, 6);
        addToTable.setTimestamp(4, randomTimestamp());
        addToTable.setInt(5, 90);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 2);
        addToTable.setInt(3, 5);
        addToTable.setTimestamp(4, randomTimestamp());
        addToTable.setInt(5, 90);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 2);
        addToTable.setInt(3, 5);
        addToTable.setTimestamp(4, randomTimestamp());
        addToTable.setInt(5, 120);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 1);
        addToTable.setInt(3, 2);
        addToTable.setTimestamp(4, randomTimestamp());
        addToTable.setInt(5, 50);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 2);
        addToTable.setInt(3, 3);
        addToTable.setTimestamp(4, randomTimestamp());
        addToTable.setInt(5, 80);

        addToTable.executeUpdate();
        addToTable.close();
    }

    private static void initMaintenance(Connection con) throws Exception{
        Statement generalStat = con.createStatement();
        generalStat.execute("CREATE TABLE proj.Maintenance(id INTEGER PRIMARY KEY, idMachine INTEGER REFERENCES proj.machine(id), dateDeMaintenance DATE);");
        generalStat.close();
    }

    //Misc functions
    private static void clearAll(Connection con) throws Exception{
        Statement deleteAndRecreate = con.createStatement();

        deleteAndRecreate.execute("DROP SCHEMA proj CASCADE;");
        deleteAndRecreate.execute("CREATE SCHEMA proj");
    }

    private static Date randomDate(){
        int year = (int) (Math.random() * 2 + 2025);
        String month = String.valueOf((int) (Math.random() * 12 + 1));
        String day = String.valueOf((int) (Math.random() * 29 + 1));
        
        if(year == 2025){
            month = "12";
        }

        return Date.valueOf(String.valueOf(year) + "-" + month + "-" + day);
    }

    private static Timestamp randomTimestamp(){
        int year = (int) (Math.random() * 2 + 2025);
        String month = String.valueOf((int) (Math.random() * 12 + 1));
        String day = String.valueOf((int) (Math.random() * 29 + 1));

        if(year == 2025){
            month = "12";
        }

        String hour = String.valueOf((int) (Math.random() * 12 + 6));
        int min = (int)(Math.random() * 6);

        return Timestamp.valueOf(String.valueOf(year) + "-" + month + "-" + day + " " + hour + ":" + String.valueOf(min * 10) + ":00");
    }
}
