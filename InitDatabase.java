import java.sql.*;

//to compile and execute
//javac Item.java
//java -cp "postgresql-42.7.3.jar;" item

public class InitDatabase{
    public static void main(String[] args) throws Exception{
        Class.forName("org.postgresql.Driver");

        Connection con = DriverManager.getConnection("jdbc:postgresql://kafka.iem/kb483753", "kb483753", "kb483753");

        clearAll(con);

        initMachine(con);
        initAbonnement(con);
        // Il faut forcement init dabord les abonnements avant les clients, car les clients utilise des clefs etrangere vers abonnement
        initClient(con);
        initCoach(con);
        // InitSessiobPrivee dois passer apres Coach
        initSessionPrivee(con);
        // Table associative entre les sessions privee et les clients. Relation n-n, car un client peut avoir plusieurs session privee, et une session privee peut avoir plusieur client inscrit
        initClientSessionPrivee(con);

    }

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
        addToTable.setString(2, "None");
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
    private static void initClient(Connection con) throws Exception{
        Statement statTableCreation = con.createStatement();
        statTableCreation.execute(
            "CREATE TABLE proj.client (id INTEGER PRIMARY KEY, nom TEXT, prenom TEXT, typeAbonnement INTEGER REFERENCES proj.abonnement(id), finAbonnement DATE);"
        );
        statTableCreation.close();

        PreparedStatement addToTable = con.prepareStatement("INSERT INTO proj.client(id, nom, prenom, typeAbonnement, finAbonnement) VALUES (?, ?, ?, ?, ?)");

        int idCount = 1;

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Tamby");
        addToTable.setString(3, "Sebastien");
        addToTable.setInt(4, 2);
        addToTable.setDate(5, randomDate());

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Blaszkow");
        addToTable.setString(3, "Kamil");
        addToTable.setInt(4, 2);
        addToTable.setDate(5, randomDate());

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Pandion");
        addToTable.setString(3, "Brian");
        addToTable.setInt(4, 4);
        addToTable.setDate(5, randomDate());

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Romano Castro");
        addToTable.setString(3, "Caio");
        addToTable.setInt(4, 3);
        addToTable.setDate(5, randomDate());

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Pinto Da Fonseca");
        addToTable.setString(3, "Sacha");
        addToTable.setInt(4, 4);
        addToTable.setDate(5, randomDate());

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Roux");
        addToTable.setString(3, "Nicolas");
        addToTable.setInt(4, 2);
        addToTable.setDate(5, randomDate());

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
        addToTable.close();
    }
    private static void initSessionPrivee(Connection con) throws Exception{
        Statement statCreationTable = con.createStatement();
        statCreationTable.execute("CREATE TABLE proj.sessionPrivee(id INTEGER PRIMARY KEY, coachAttitrer INTEGER REFERENCES proj.coach(id), dateDebut DATE, dureeEnMin INTEGER);");
        statCreationTable.close();

        PreparedStatement addToTable = con.prepareStatement("INSERT INTO proj.sessionPrivee(id, coachAttitrer, dateDebut, dureeEnMin) VALUES (?, ?, ?, ?)");

        int idCount = 1;

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 1);
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 90);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 1);
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 60);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 1);
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 60);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 1);
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 150);

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setInt(2, 1);
        addToTable.setDate(3, randomDate());
        addToTable.setInt(4, 90);

        addToTable.executeUpdate();
        addToTable.close();
    }
    //table associative entre les client et les sessions privee
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
}