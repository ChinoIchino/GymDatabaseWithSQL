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

    }

    private static void initMachine(Connection con) throws Exception{
        Statement statTableCreation = con.createStatement();
        statTableCreation.execute("CREATE TABLE proj.machine (id INTEGER, nom TEXT, prochMaintenance DATE);");

        PreparedStatement addToTable = con.prepareStatement(
            "INSERT INTO proj.machine(id, nom, prochMaintenance) VALUES (?, ?, ?)"
            );

        int idCount = 1;

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Leg Extension Machine");
        addToTable.setDate(3, randomDate());

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Leg Curl Machine");
        addToTable.setDate(3, randomDate());

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Seated Calf Machine");
        addToTable.setDate(3, randomDate());

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Preacher Curl Machine");
        addToTable.setDate(3, randomDate());

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Rowing Machine");
        addToTable.setDate(3, randomDate());

        addToTable.executeUpdate();

        
        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Lat Pulldown Machine");
        addToTable.setDate(3, randomDate());

        addToTable.executeUpdate();

        
        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Smith Machine");
        addToTable.setDate(3, randomDate());

        addToTable.executeUpdate();

        
        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Bench Press");
        addToTable.setDate(3, randomDate());

        addToTable.executeUpdate();

        
        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Delt Fly Machine");
        addToTable.setDate(3, randomDate());

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Cable Machine");
        addToTable.setDate(3, randomDate());

        addToTable.executeUpdate();

        addToTable.setInt(1, idCount++);
        addToTable.setString(2, "Declined Crunches Bench");
        addToTable.setDate(3, randomDate());

        addToTable.executeUpdate();

        statTableCreation.close();
        addToTable.close();
    }
    private static void initAbonnement(Connection con) throws Exception{
        Statement statTableCreation = con.createStatement();
        statTableCreation.execute("CREATE TABLE proj.abonnement (id INTEGER, nom TEXT, prix DECIMAL);");

        PreparedStatement addToTable = con.prepareStatement(
            "INSERT INTO proj.abonnement(id, nom, prix) VALUES (?, ?, ?)"
            );

        int idCount = 1;

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