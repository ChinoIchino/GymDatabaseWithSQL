import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class Inscription{
    public static void main(String []args,Connection con, Scanner scanner) throws Exception{
        int choiceOfInscription;
        do{
            System.out.println("\n//////////////////////////////////////////////\n1. Inscription a la salle de musculation\n2. Inscription a une session privee\n3. Quitter\n");
            choiceOfInscription = scanner.nextInt();

            if(choiceOfInscription == 1){
                inscriptionMusculation(con, scanner);
            }else if(choiceOfInscription == 2){
                inscriptionSessionPrivee(con, scanner);
            }else if(choiceOfInscription == 3){
                App.main(args);
            }
        }while(choiceOfInscription != 3);
    }

    private static void inscriptionMusculation(Connection con, Scanner scanner) throws Exception{
        System.out.println("Nom : ");
        String nom = scanner.next();

        System.out.println("Prenom : ");
        String prenom = scanner.next();

        int choixAbonnement;
        do{
            System.out.println("\nChoix du Forfait:\n     1.Basic : 19.99eur\n      2.Premium : 29.99eur\n       3.Premium+ : 39.99eur\n");
            choixAbonnement = scanner.nextInt();
        }while(choixAbonnement < 1 || choixAbonnement > 3);

        PreparedStatement addToTable = con.prepareStatement(
            "INSERT INTO proj.client(id, nom, prenom, typeAbonnement, finAbonnement, coachAttitrer) VALUES ((SELECT COUNT(*) + 1 FROM proj.client), ?, ?, ?, ?, ?)"
        );

        addToTable.setString(1, nom);
        addToTable.setString(2, prenom);
        addToTable.setInt(3, choixAbonnement);

        if(choixAbonnement == 3){
            System.out.println("\nVoulez vous des a present choisir un coach : ");

            Statement coachStat = con.createStatement();
            ResultSet allCoach = coachStat.executeQuery("SELECT nom, prenom FROM proj.coach");

            int count = 1;
            while(allCoach.next()){
                System.out.println(count++ + " : " + allCoach.getString(1) + " " + allCoach.getString(2));
            }
            System.out.println(count + " : Remettre a plus tard");
            coachStat.close();
            allCoach.close();

            int choiceCoach = scanner.nextInt();
            
            //si le choix est entre les coachs possible
            if(choiceCoach >= 1 &&  choiceCoach < count){
                addToTable.setInt(5, choiceCoach);
            }else{
                addToTable.setNull(5, java.sql.Types.INTEGER);
            }
        }else{
            addToTable.setNull(5, java.sql.Types.INTEGER);
        }


        LocalDate temp = LocalDate.now().plusDays(30);
        Date newDate = Date.valueOf(temp);

        addToTable.setDate(4, newDate);
        addToTable.executeUpdate();

        addToTable.close();
    }

    private static void inscriptionSessionPrivee(Connection con, Scanner scanner) throws Exception{

        System.out.println("Nom : ");
        String nom = scanner.next();

        System.out.println("Prenom : ");
        String prenom = scanner.next();

        Statement generalStat = con.createStatement();
        ResultSet resInscriptionType = generalStat.executeQuery("SELECT id, typeAbonnement FROM proj.client WHERE nom ILIKE \'" + nom + "\' AND prenom ILIKE \'" + prenom + "\'");

        //verification si il existe des donnees sur la personne entrer, si resInscriptionType.next() n'a pas de valeur il va nous renvoyer false
        boolean isFound = resInscriptionType.next();

        if(isFound == false){
            throw new IllegalArgumentException(
                "\nInsciption.java ERREUR: Le client avec le nom : " + nom + " " + prenom + " n'a pas etait trouver. Veuillez verifier si les donnees on etait ecrite correctement."
            );
        }

        int idUser = resInscriptionType.getInt(1);
        int aboType = resInscriptionType.getInt(2);
        resInscriptionType.close();

        //peut s'inscrire a une session privee
        if(aboType >= 2){
            ResultSet allPossibleSessions = generalStat.executeQuery(
                "SELECT c.nom, c.prenom, dateDebut, dureeEnMin FROM proj.sessionPrivee sp INNER JOIN proj.coach c On sp.coachAttitrer = c.id"
            );

            System.out.println();
            
            int count = 1;
            while(allPossibleSessions.next()){
                System.out.println(
                    count++ + " : Session du " + allPossibleSessions.getString(3) + " / duree : " + allPossibleSessions.getInt(4) 
                    + "min / Avec " + allPossibleSessions.getString(1) + " " + allPossibleSessions.getString(2)
                );
            }
            allPossibleSessions.close();

            int choiceOfSession = scanner.nextInt(); 
            if(choiceOfSession > 0 && choiceOfSession < count){
                generalStat.execute("INSERT INTO proj.client_sessionPrivee(idClient, idSessionPrivee) VALUES (" + idUser + ", " + choiceOfSession + ")");
            }else{
                System.out.println("Numero de session non reconnue, Veuillez reessayer ulterieurement");
            }
        }else{
            System.out.println("Malheuresement vous ne possedez pas l'abonnement necessaire pour pouvoir souscrire a une session privee");
        }

        generalStat.close();
    }
}
