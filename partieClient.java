import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class partieClient{
    public static void main(String []args,Connection con, Scanner scanner) throws Exception{
        int choiceOfInscription;
        do{
            System.out.println(
                "\n|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|\n1. Inscription à la salle de musculation\n2. Inscription a une session privée\n3. Changer de coach attitrée\n"
                + "4. Validité de l'abonnement\n5. Quitter\n"
                );
            choiceOfInscription = scanner.nextInt();

            if(choiceOfInscription == 1){
                inscriptionMusculation(con, scanner);
            }else if(choiceOfInscription == 2){
                inscriptionSessionPrivee(con, scanner);
            }else if(choiceOfInscription == 3){
                changerCoach(con, scanner);
            }else if(choiceOfInscription == 4){
                peutEntrer(con, scanner);
            }
        }while(choiceOfInscription != 5);
    }

    private static void peutEntrer(Connection con, Scanner scanner) throws Exception{
        System.out.println("Nom : ");
        String nom = scanner.next();


        System.out.println("Prénom : ");
        String prenom = scanner.next();

        Statement generalStat = con.createStatement();
        ResultSet resTypeAbo = generalStat.executeQuery("SELECT typeAbonnement FROM proj.client WHERE prenom ILIKE \'" + prenom + "\' AND nom ILIKE \'" + nom + "\'");

        boolean isFound = resTypeAbo.next();

        if(isFound == false){
            throw new IllegalArgumentException(
                "\npartieClient.java ERREUR: Le client avec le nom : " + nom + " " + prenom + " n'a pas été trouvé. Veuillez vérifier si les données on été écrites correctement."
            );
        }

        int typeAbo = resTypeAbo.getInt(1);

        if(typeAbo > 1){
            System.out.println("Le client " + nom + " " + prenom + " peut entrer dans la salle");
        }else{
            System.out.println("Le client " + nom + " " + prenom + " ne peut pas entrer dans la salle");
        }
    }

    private static void inscriptionMusculation(Connection con, Scanner scanner) throws Exception{
        System.out.println("Nom : ");
        String nom = scanner.next();

        System.out.println("Prénom : ");
        String prenom = scanner.next();

        int choixAbonnement;
        do{
            System.out.println("\nChoix du Forfait:\n     1.Basic : 19.99 EUR\n      2.Premium : 29.99 EUR\n       3.Premium+ : 39.99 EUR\n");
            choixAbonnement = scanner.nextInt();
        }while(choixAbonnement < 1 || choixAbonnement > 3);

        PreparedStatement addToTable = con.prepareStatement(
            "INSERT INTO proj.client(id, nom, prenom, typeAbonnement, finAbonnement, coachAttitrer, solde) VALUES ((SELECT COUNT(*) + 1 FROM proj.client), ?, ?, ?, ?, ?, ?)"
        );

        addToTable.setString(1, nom);
        addToTable.setString(2, prenom);
        addToTable.setInt(3, ++choixAbonnement);

        if(choixAbonnement == 4){
            System.out.println("\nVoulez-vous dès à présent choisir un coach : ");

            Statement coachStat = con.createStatement();
            ResultSet allCoach = coachStat.executeQuery("SELECT nom, prenom FROM proj.coach ORDER BY id");

            int count = 1;
            while(allCoach.next()){
                System.out.println(count++ + " : " + allCoach.getString(1) + " " + allCoach.getString(2));
            }
            System.out.println(count + " : Remettre à plus tard");
            coachStat.close();
            allCoach.close();

            int choiceCoach = scanner.nextInt();
            
            //si le choix est parmi les coachs possibles
            if(choiceCoach >= 1 &&  choiceCoach < count){
                addToTable.setInt(5, choiceCoach);
            }else{
                addToTable.setNull(5, java.sql.Types.INTEGER);
            }
        }else{
            addToTable.setNull(5, java.sql.Types.INTEGER);
        }

        addToTable.setDouble(6, 0);

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

        //vérification s'il existe des données sur la personne entrée, si resInscriptionType.next() n'a pas de valeur, il va nous renvoyer false
        boolean isFound = resInscriptionType.next();

        if(isFound == false){
            throw new IllegalArgumentException(
                "\npartieClient.java ERREUR: Le client avec le nom : " + nom + " " + prenom + " n'a pas été trouvé. Veuillez vérifier si les données on été écrites correctement."
            );
        }

        int idUser = resInscriptionType.getInt(1);
        int aboType = resInscriptionType.getInt(2);
        resInscriptionType.close();

        
        //peut s'inscrire à une session privée
        if(aboType >= 3){
            ResultSet allPossibleSessions = generalStat.executeQuery(
                "SELECT c.nom, c.prenom, dateDebut, dureeEnMin FROM proj.sessionPrivee sp INNER JOIN proj.coach c On sp.coachAttitrer = c.id"
            );

            System.out.println();
            
            int count = 1;
            while(allPossibleSessions.next()){
                System.out.println(
                    count++ + " : Session du " + allPossibleSessions.getString(3) + " / durée : " + allPossibleSessions.getInt(4) 
                    + "min / avec " + allPossibleSessions.getString(1) + " " + allPossibleSessions.getString(2)
                );
            }
            allPossibleSessions.close();

            int choiceOfSession = scanner.nextInt(); 
            if(choiceOfSession > 0 && choiceOfSession < count){
                // Vérification pour savoir si le client est déja inscrit à la session
                ResultSet checkAlreadyRegistred = generalStat.executeQuery("SELECT COUNT(*) FROM proj.client_sessionPrivee WHERE idClient = "+ idUser +" AND idSessionPrivee = "+ choiceOfSession);
                checkAlreadyRegistred.next();
                int countAlreadyRegistered = checkAlreadyRegistred.getInt(1);
                checkAlreadyRegistred.close();

                if(countAlreadyRegistered > 0){
                    System.out.println("Vous êtes déja inscrit à cette session privée !");
                }
                else{
                    // Si le client n'est pas déja inscrit, on l'inscrit
                    generalStat.execute("INSERT INTO proj.client_sessionPrivee(idClient, idSessionPrivee) VALUES (" + idUser + ", " + choiceOfSession + ")");
                    System.out.println("Vous avez bien été inscrit à cette session!");
                }
            }else{
                System.out.println("Numéro de session non reconnu, veuillez réessayer ultérieurement");
            }
        }else{
            System.out.println("Malheuresement, vous ne possédez pas l'abonnement nécessaire pour pouvoir souscrire à une session privée."
                                +"\nCependant vous pouvez participer à une session privée à condition que vous payer un supplément de 4.99 EUR."
            );
            
            int rep;
            do{
                System.out.println("\nSouhaitez-vous accepter cette offre ? (1 = OUI, 0 = NON) : ");
                rep = scanner.nextInt();
            }while(rep != 0 && rep != 1);
            
            if(rep == 1){
                ResultSet choice = generalStat.executeQuery("SELECT c.nom, c.prenom, dateDebut, dureeEnMin FROM proj.sessionPrivee sp INNER JOIN proj.coach c On sp.coachAttitrer = c.id");
                System.out.println();
            
                int count = 1;
                while(choice.next()){
                    System.out.println(
                        count++ + " : Session du " + choice.getString(3) + " / durée : " + choice.getInt(4) 
                        + "min / avec " + choice.getString(1) + " " + choice.getString(2)
                    );
                }
                choice.close();
                int userChoice = scanner.nextInt(); 
                if(userChoice > 0 && userChoice < count){
                    // Vérification si le client à déjà été inscrit à une session
                    ResultSet AlreadyRegistred = generalStat.executeQuery("SELECT COUNT(*) FROM proj.client_sessionPrivee WHERE idClient = "+ idUser+" AND idSessionPrivee = "+ userChoice);
                    AlreadyRegistred.next();
                    int ifAlreadyRegistred = AlreadyRegistred.getInt(1);
                    AlreadyRegistred.close();
                    
                    if(ifAlreadyRegistred > 0){
                        System.out.println("Vous êtes déja inscrit à cette session privée !");
                    }
                    else{
                        // Si le client n'est pas déja inscrit, on l'inscrit et on lui ajoute 4.99 EUR à son solde
                        generalStat.execute("INSERT INTO proj.client_sessionPrivee(idClient, idSessionPrivee) VALUES (" + idUser + ", " + userChoice + ")");
                        ResultSet val = generalStat.executeQuery("SELECT solde FROM proj.client WHERE id = " + idUser);
                        val.next();
                        double solde = val.getDouble(1) + 4.99;
                        val.close();
                        generalStat.execute("UPDATE proj.client SET solde = "+ solde +" WHERE id = " + idUser);
                        System.out.println("Vous avez bien été inscrit à cette session!");
                        System.out.println("Votre solde viens d'augmenter de 4.99 EUR!");
                        System.out.println("Si vous prenez plusieurs sessions privées, réflechissez à prendre un abonnement de plus haut niveau! ");

                    }
                }else{
                    System.out.println("Numéro de session non reconnu, veuillez réessayer ultérieurement");
                }
            }
        }
        generalStat.close();
    }

    private static void changerCoach(Connection con, Scanner scanner) throws Exception{
        System.out.println("Nom : ");
        String nom = scanner.next();

        System.out.println("Prenom : ");
        String prenom = scanner.next();

        Statement generalStat = con.createStatement();
        ResultSet resTypeAbo = generalStat.executeQuery("SELECT typeAbonnement FROM proj.client WHERE nom ILIKE \'" + nom + "\' AND prenom ILIKE \'" + prenom + "\'");

        boolean isFound = resTypeAbo.next();

        if(isFound == false){
            throw new IllegalArgumentException(
                "\npartieClient.java ERREUR: Le client avec le nom : " + nom + " " + prenom + " n'a pas été trouvé. Veuillez vérifier si les données on été écrites correctement."
            );
        }

        int typeAbo = resTypeAbo.getInt(1);

        if(typeAbo == 4){
            System.out.println("\nListe de tout les coachs : ");

            ResultSet allCoach = generalStat.executeQuery("SELECT nom, prenom FROM proj.coach ORDER BY id");

            int count = 1;
            while(allCoach.next()){
                System.out.println(count++ + " : " + allCoach.getString(1) + " " + allCoach.getString(2));
            }
            System.out.println(count + " : Retirer le coach actuel");
            allCoach.close();

            int choice;
            //redemande la valeur tant qu'elle n'est pas adequate
            do{
                choice = scanner.nextInt();
                System.out.println("\nGot the choice = " + choice);
            }while(choice < 1 || choice > count);

            if(choice == count){
                //UPDATE proj.machine SET prochMaintenance = \'" + newDate + "\' WHERE id = " + idMachine
                generalStat.execute("UPDATE proj.client SET coachAttitrer = NULL WHERE nom ILIKE \'" + nom + "\' AND prenom ILIKE \'" + prenom + "\'");
            }else{
                generalStat.execute("UPDATE proj.client SET coachAttitrer = " + choice + " WHERE nom ILIKE \'" + nom + "\' AND prenom ILIKE \'" + prenom + "\'");
            }
        }else{
            System.out.println("Malheuresement, vous ne possédez pas l'abonnement nécessaire pour pouvoir souscrire à un coach");
        }

        generalStat.close();
    }
}

