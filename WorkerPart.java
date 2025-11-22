import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class WorkerPart {
    public static void main(String []args, Connection con, Scanner scanner) throws Exception{

        int choice;
        do{
            System.out.println("\n//////////////////////////////////////////////\n1. Mettre a jour la prochaine maintenance\n2. Quitter\n");
            choice = scanner.nextInt();
            if(choice == 1){
                maintenance(con, scanner);
            }
        }while(choice != 2);
    }

    private static void maintenance(Connection con, Scanner scanner) throws Exception{
        
        System.out.println("\nDonner l'id de la machine qui viens d'avoir une maintenance :\n");
        int idMachine = scanner.nextInt();
    
        Statement generalStat = con.createStatement();

        ResultSet resOfMachine = generalStat.executeQuery("SELECT intervalMaintenanceEnJour FROM proj.machine WHERE id = " + idMachine);
        boolean isFound = resOfMachine.next();

        if(isFound == false){
            throw new IllegalArgumentException(
                "\nWorkerPart.java ERREUR: La machine avec l'id : " + idMachine + " n'a pas etait trouver. Veuillez verifier si l'id de la machine est correcte."
            );
        }

        int intervalDeMaintenance = resOfMachine.getInt(1);
        resOfMachine.close();

        LocalDate localDate = LocalDate.now().plusDays(intervalDeMaintenance); //ajoute la valeur en jour voulu dans la date actuel
        
        Date newDate = Date.valueOf(localDate); //rechange la valeur en type Date pour la requete sql

        generalStat.execute("UPDATE proj.machine SET prochMaintenance = \'" + newDate + "\' WHERE id = " + idMachine);

        //ajoute la maintenance dans la table Maintenance
        generalStat.execute("INSERT INTO proj.Maintenance(id, idMachine, dateDeMaintenance) VALUES ((SELECT COUNT(*) + 1 FROM proj.maintenance), " + idMachine + ", CURRENT_DATE);");
        System.out.println("\nLa maintenance a bien etait enregistrer.");

        generalStat.close();
    }
}
