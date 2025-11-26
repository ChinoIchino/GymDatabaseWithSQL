import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class partiePro {
    public static void main(String []args, Connection con, Scanner scanner) throws Exception{

        int choice;
        do{
            System.out.println("\n|//|//|//|//|//|//|//|//|//|//|//|//|//|//|//|//|\n1. Mettre à jour la prochaine maintenance\n2. Quitter\n");
            choice = scanner.nextInt();
            if(choice == 1){
                maintenance(con, scanner);
            }
        }while(choice != 2);
    }

    private static void maintenance(Connection con, Scanner scanner) throws Exception{
        
        System.out.println("\nEntrez l'id de la machine qui vient d'avoir une maintenance :\n");
        int idMachine = scanner.nextInt();
    
        Statement generalStat = con.createStatement();

        ResultSet resOfMachine = generalStat.executeQuery("SELECT intervalMaintenanceEnJour FROM proj.machine WHERE id = " + idMachine);
        boolean isFound = resOfMachine.next();

        if(isFound == false){
            throw new IllegalArgumentException(
                "\nWorkerPart.java ERREUR: La machine avec l'id : " + idMachine + " n'a pas été trouvée. Veuillez vérifier si l'id de la machine est correct."
            );
        }

        int intervalDeMaintenance = resOfMachine.getInt(1);
        resOfMachine.close();

        LocalDate localDate = LocalDate.now().plusDays(intervalDeMaintenance); //ajoute la valeur en jours souhaité dans la date actuelle
        
        Date newDate = Date.valueOf(localDate); //reconvertit la valeur en type Date pour la requête SQL

        generalStat.execute("UPDATE proj.machine SET prochMaintenance = \'" + newDate + "\' WHERE id = " + idMachine);

        //Ajoute la maintenance dans la table Maintenance
        generalStat.execute("INSERT INTO proj.Maintenance(id, idMachine, dateDeMaintenance) VALUES ((SELECT COUNT(*) + 1 FROM proj.maintenance), " + idMachine + ", CURRENT_DATE);");
        System.out.println("\nLa maintenance a bien été enregistrée.");

        generalStat.close();
    }
}


