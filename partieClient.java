import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class partieClient{
public static void main(String []args,Connection con, Scanner scanner) throws Exception{
    int choiceOfInscription;
    do{
        System.out.println(
            "\n|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|/|\n1. Inscription à la salle de musculation\n2. Inscription a une session privée\n3. Changer de coach attitrée\n"
            + "4. Changer l'abonnement actuel\n5. Date d'abonnement expirer\n6. Quitter\n"
        );
        choiceOfInscription = scanner.nextInt();

        if(choiceOfInscription == 1){
            inscriptionMusculation(con, scanner);
        }else if(choiceOfInscription == 2){
            inscriptionSessionPrivee(con, scanner);
        }else if(choiceOfInscription == 3){
            changerCoach(con, scanner);
        }else if(choiceOfInscription == 4){
            changerAbo(con, scanner);
        }else if(choiceOfInscription == 5){
            peutEntrer(con, scanner);
        }
    }while(choiceOfInscription != 6);
}

private static void changerAbo(Connection con, Scanner scanner) throws Exception{
    System.out.println("Nom : ");
    String nom = scanner.next();

    System.out.println("Prénom : ");
    String prenom = scanner.next();

    Statement generalStat = con.createStatement();
    // resTypeAbo est utiliser pour verifier si l'utilisateur existe, mais aussi on collecte toutes les informations neccessaire pour le changement d'abonnement
    ResultSet resTypeAbo = generalStat.executeQuery("SELECT id, typeAbonnement, solde FROM proj.client WHERE prenom ILIKE \'" + prenom + "\' AND nom ILIKE \'" + nom + "\'");

    boolean isFound = resTypeAbo.next();

    if(isFound == false){
        throw new SQLSyntaxErrorException(
            "\npartieClient.java ERREUR: Le client avec le nom : " + nom + " " + prenom + " n'a pas été trouvé. Veuillez vérifier si les données on été écrites correctement."
        );
    }

    int choiceTypeAbo;
    do{
        System.out.println("\n1.Abonnement Mensuel\n2.Abonnement Journalier");
        choiceTypeAbo = scanner.nextInt();
    }while(choiceTypeAbo < 1 && choiceTypeAbo > 2);

    // Partie "general" entre les deux possibilité
    int userId = resTypeAbo.getInt(1);
    int currAbo = resTypeAbo.getInt(2);
    double currSolde = resTypeAbo.getDouble(3);

    //abo mensuel
    if(choiceTypeAbo == 1){
        //TODO Verifier si un utilisateur passe de premium+ a autre, il faut effacer le coachAttitrer de ces attributs
        String[] allAbo = {"Pas abonnée", "Basic", "Premium", "Premium+"};
        double[] allPrice = {0, 19.99, 29.99, 39.99};
        String[] possibleChoices = new String[3];
        int count = 1;
        for(int i = 0; i < 4; i++){
            if(i != (currAbo - 1)){
                System.out.println(count + " : " + allAbo[i] + " au prix de " + allPrice[i] + "eur");
                possibleChoices[(count - 1)] = allAbo[i];
                count++;
            }
        }
        System.out.println("4. Choisir une autre fois");

        int choiceNewAbo;
        do{
            choiceNewAbo = scanner.nextInt();
        }while(choiceNewAbo < 1 && choiceNewAbo > 4);

        // L'utilisateur a choisis un nouveau abonnement
        if(choiceNewAbo < 4){
            // On n'a plus besoin de resTypeAbo, car toutes les attributs recuperer on etait sauvegarder dans des variables
            resTypeAbo.close();

            // On donne une valeur d'expiration a l'abonnement par rapport au moment de la modification + 30 jour
            LocalDate localDate = LocalDate.now().plusDays(30);
            Date newDate = Date.valueOf(localDate);

            ResultSet resNewTypeAbo = generalStat.executeQuery("SELECT id FROM proj.abonnement WHERE nom ILIKE \'" + possibleChoices[choiceNewAbo - 1] + "\'");
            resNewTypeAbo.next();
            generalStat.execute(
                "UPDATE proj.client SET typeAbonnement = " + resNewTypeAbo.getInt(1) + ", finAbonnement = \'" + newDate + "\', solde = " + (currSolde + allPrice[choiceNewAbo - 1])
                + " WHERE id = " + userId
            );
        }
    }else{
        String[] allAbo = {"Basic", "Premium", "Premium+"};
        double[] allPrice = {4.99, 7.99, 9.99};

        int count = 1;
        for(int i = 0; i < 3; i++){
            // + 2, car les attributs abonnement commence a partir de 2 jusqu'a 4 (2 = Basic, 3 = Premium, 4 = Premium+)
            if((i + 2) > currAbo){
                System.out.println(count++ + " : " + allAbo[i] + " au prix de " + allPrice[i] + "eur pour la journée");
            }
        }
        System.out.println(count + " : Choisir une autre fois");

        int choiceTempAbo;
        do{
            choiceTempAbo = scanner.nextInt();
        }while(choiceTempAbo < 1 && choiceTempAbo > count);

        // L'utilisateur a choisis une option
        if(choiceTempAbo < count){
            // Prepare l'insertion d'un nouveau objet dans la table des abonnements temporaire
            PreparedStatement addToTable = con.prepareStatement(
                "INSERT INTO proj.abonnementTemp(id, idClient, typeAbonnement, finAbonnement) VALUES ((SELECT COUNT(*) + 1 FROM proj.abonnementTemp), ?, ?, ?);"
            );

            LocalDate localDate = LocalDate.now().plusDays(1);
            Date newDate = Date.valueOf(localDate);

            // Insere les valeurs dans la futur requete sql
            addToTable.setInt(1, userId);
            // Prend l'abonnement par rapport a l'abonnement actuel + le choix de l'utilisateur
            addToTable.setInt(2, (currAbo + choiceTempAbo));
            addToTable.setDate(3, newDate);

            // Envoie la requete sql
            addToTable.executeUpdate();

            // Modifie les soldes de l'utilisateur qui viens d'acheter un abonnement temporaire
            generalStat.execute("UPDATE proj.client SET solde = " + (currSolde + allPrice[choiceTempAbo - 1] + " WHERE id = " + userId));
        }
    }
}
private static void peutEntrer(Connection con, Scanner scanner) throws Exception{
        System.out.println("Nom : ");
        String nom = scanner.next();


        System.out.println("Prénom : ");
        String prenom = scanner.next();

        Statement generalStat = con.createStatement();
        // Prend seulement l'id pour verifier si l'utilisateur avec les donnes entrer existe
        ResultSet resIdUser = generalStat.executeQuery("SELECT id FROM proj.client WHERE prenom ILIKE \'" + prenom + "\' AND nom ILIKE \'" + nom + "\'");

        boolean isFound = resIdUser.next();

        if(isFound == false){
            throw new IllegalArgumentException(
                "\npartieClient.java ERREUR: Le client avec le nom : " + nom + " " + prenom + " n'a pas été trouvé. Veuillez vérifier si les données on été écrites correctement."
            );
        }

        int userId = resIdUser.getInt(1);
        resIdUser.close();
        // Recolte l'abonnement mensuel de l'utilisateur si il n'est pas expirer 
        ResultSet resTypeAbo = generalStat.executeQuery(
            "SELECT typeAbonnement From proj.client WHERE id =" + userId + " AND finAbonnement > CURRENT_DATE"
        );
        // Recolte tout les abonnements temporaire de l'utilisateur qui n'ont pas expirer
        Statement secondGeneralStat = con.createStatement();
        ResultSet resTypeAbotemp = secondGeneralStat.executeQuery(
            "SELECT typeAbonnement FROM proj.abonnementTemp WHERE idClient = " + userId + " AND finAbonnement > CURRENT_DATE"
        );

        // Passe avec 2 boucles tant que par toutes les reponse donner par resTypeAbo et resTypeAboTemp
        // J'ai mis resTypeAbo dans une boucle aussi alors qu'il a forcement au maximum une seul valeur, pour eviter de devoir passer par des boolean de verification
        boolean aboValid = false;
        while(resTypeAbo.next()){
            if(resTypeAbo.getInt(1) > 1){
                aboValid = true;
            }
        }
        // Si l'abonnement "principal" n'est pas adequat, on verifie les abonnements temporaire
        if(aboValid == false){
            while(resTypeAbotemp.next()){
                if(resTypeAbotemp.getInt(1) > 1){
                    aboValid = true;
                }
            }
        }

        resTypeAbo.close();
        resTypeAbotemp.close();
        secondGeneralStat.close();
        generalStat.close();

        if(aboValid){
            System.out.println("Le client " + nom + " " + prenom + " peut entrer dans la salle");
        }else{
            System.out.println("Le client " + nom + " " + prenom + " ne peut pas entrer dans la salle");
        }
    }

    // TODO Ajouter le nouveau moyen de verification d'abonnement que dans la fonction "peutEntrer"
    // TODO a mettre encore dans les fonctions: inscriptionMusculation, inscriptionSessionPrivee, changerCoach
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
