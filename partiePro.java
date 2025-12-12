import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class partiePro {
    public static void main(String []args, Connection con, Scanner scanner) throws Exception{

        int choice;
        do{
            System.out.println("\n----------------Menu Pro----------------\n1. Mettre à jour la prochaine maintenance"
                            + "\n2. Donner toutes les machines qui on besoin d'une maintenance dans les 30 prochain jours\n3. Quitter\n"
                            + "----------------Menu Pro----------------"
                            );
            choice = scanner.nextInt();
            if(choice == 1){
                maintenance(con, scanner);
            }else if(choice == 2){
                maintenancesDansProch30J(con);
            }
        }while(choice != 3);
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

    private static void maintenancesDansProch30J(Connection con) throws Exception{
        Statement generalStat = con.createStatement();

        LocalDate localDateFilter = LocalDate.now().plusDays(30);
        Date dateFilter = Date.valueOf(localDateFilter);

        // Prends tout les machines dont la maintenance doit etre faite dans maximum 30 jours
        ResultSet allNextMaint = generalStat.executeQuery("SELECT id, nom, prochMaintenance FROM proj.machine WHERE prochMaintenance <= \'" + dateFilter + "\'");

        int currId;
        String currName;
        Date currProchMaint;

        while(allNextMaint.next()){
            currProchMaint = allNextMaint.getDate(3);

            // Prend les valeurs de la machine courante
            currId = allNextMaint.getInt(1);
            currName = allNextMaint.getString(2);

            System.out.println("Id = " + currId + " : " + currName + ", Derniere date pour la prochaine maintenance : " + currProchMaint);
        }
    }
}


