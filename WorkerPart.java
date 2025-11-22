import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class WorkerPart {
    public static void main(String []args) throws Exception{
        Scanner scanner = new Scanner(System.in);
        Class.forName("org.postgresql.Driver");

        Connection con = DriverManager.getConnection("jdbc:postgresql://kafka.iem/kb483753", "kb483753", "kb483753");

        InitDatabase.initAll(con);

        while(true){
            maintenance(con, scanner);
        }

        scanner.close();
    }

    private static void maintenance(Connection con, Scanner scanner) throws Exception{
        
        System.out.println("Donner l'id de la machine qui viens d'avoir une maintenance : ");
        int idMachine = scanner.nextInt();
        
        // scanner.close();
    
        Statement generalStat = con.createStatement();

        ResultSet resOfMachine = generalStat.executeQuery("SELECT intervalMaintenanceEnJour FROM proj.machine WHERE id = " + idMachine);
        resOfMachine.next();
        int intervalDeMaintenance = resOfMachine.getInt(1);
        // System.out.println("oldDate = " + oldDate + " // inter = " + intervalDeMaintenance);
        resOfMachine.close();

        LocalDate localDate = LocalDate.now().plusDays(intervalDeMaintenance); //ajoute la valeur en jour voulu dans la date actuel
        
        Date newDate = Date.valueOf(localDate); //rechange la valeur en type Date pour la requete sql

        generalStat.execute("UPDATE proj.machine SET prochMaintenance = \'" + newDate + "\' WHERE id = " + idMachine);

        //ajoute la maintenance dans la table Maintenance
        generalStat.execute("INSERT INTO proj.Maintenance(id, idMachine, dateDeMaintenance) VALUES ((SELECT COUNT(*) + 1 FROM proj.maintenance), " + idMachine + ", CURRENT_DATE);");

        generalStat.close();
    }
}
