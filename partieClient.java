import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class partieClient{
    public static void main(String []args,Connection con, Scanner scanner) throws Exception{
        int choiceOfInscription;
        do{
            System.out.println(
                "\n----------------Menu Client----------------\n1. Inscription à la salle de musculation\n2. Inscription à une session privée\n3. Changer de coach attitré\n"
                + "4. Changer l'abonnement actuel\n5. Date d'abonnement expirer\n6. Quitter\n----------------Menu Client----------------"
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
        int userId = clientExiste(con, scanner);

        int choiceTypeAbo;
        do{
            System.out.println("\n1.Abonnement Mensuel\n2.Abonnement Journalier");
            choiceTypeAbo = scanner.nextInt();
        }while(choiceTypeAbo < 1 && choiceTypeAbo > 2);

        // Partie "général" entre les deux possibilités
        Statement generalStat = con.createStatement();
        ResultSet resUserInfo = generalStat.executeQuery("SELECT typeAbonnement, solde FROM proj.client WHERE id = " + userId);
        resUserInfo.next();

        int currAbo = resUserInfo.getInt(1);
        double currSolde = resUserInfo.getDouble(2);

        // On n'a plus besoin de resTypeAbo, car tous les attributs récupérés on été sauvegardés dans des variables
        resUserInfo.close();

        //abo mensuel
        if(choiceTypeAbo == 1){
            //TODO Vérifier si un utilisateur passe de Premium+ à un autre, il faut effacer le coachAttitrer de ses attributs
            String[] allAbo = {"Pas abonnée", "Basic", "Premium", "Premium+"};
            double[] allPrice = {0, 19.99, 29.99, 39.99};
            String[] possibleChoices = new String[3];
            int count = 1;
            for(int i = 0; i < 4; i++){
                if(i != (currAbo - 1)){
                    System.out.println(count + " : " + allAbo[i] + " au prix de " + allPrice[i] + "eur");
                    possibleChoices[(count - 1)] = allAbo[i];
                    count++;
                }else{
                    System.out.println(allAbo[i] + " : (Abonnement Actuel)");
                }
            }
            System.out.println("4. Choisir une autre fois");

            int choiceNewAbo;
            do{
                choiceNewAbo = scanner.nextInt();
            }while(choiceNewAbo < 1 && choiceNewAbo > 4);

            // L'utilisateur a choisi un nouvel abonnement
            if(choiceNewAbo < 4){
                // On donne une valeur d'expiration à l'abonnement par rapport au moment de la modification + 30 jours
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
                // + 2, car les attributs abonnement commencent à partir de 2 jusqu'à 4 (2 = Basic, 3 = Premium, 4 = Premium+)
                if((i + 2) > currAbo){
                    System.out.println(count++ + " : " + allAbo[i] + " au prix de " + allPrice[i] + "eur pour la journée");
                }
            }
            System.out.println(count + " : Choisir une autre fois");

            int choiceTempAbo;
            do{
                choiceTempAbo = scanner.nextInt();
            }while(choiceTempAbo < 1 && choiceTempAbo > count);

            // L'utilisateur a choisi une option
            if(choiceTempAbo < count){
                // Prépare l'insertion d'un nouveau objet dans la table des abonnements temporaires
                PreparedStatement addToTable = con.prepareStatement(
                    "INSERT INTO proj.abonnementTemp(id, idClient, typeAbonnement, finAbonnement) VALUES ((SELECT COUNT(*) + 1 FROM proj.abonnementTemp), ?, ?, ?);"
                );

                LocalDate localDate = LocalDate.now().plusDays(1);
                Date newDate = Date.valueOf(localDate);

                // Insère les valeurs dans la future requête SQL
                addToTable.setInt(1, userId);
                // Prend l'abonnement par rapport à l'abonnement actuel + le choix de l'utilisateur
                addToTable.setInt(2, (currAbo + choiceTempAbo));
                addToTable.setDate(3, newDate);

                // Envoie la requête SQL
                addToTable.executeUpdate();

                // Modifie le solde de l'utilisateur qui vient d'acheter un abonnement temporaire
                generalStat.execute("UPDATE proj.client SET solde = " + (currSolde + allPrice[choiceTempAbo - 1] + " WHERE id = " + userId));
            }
        }
    }

    private static void peutEntrer(Connection con, Scanner scanner) throws Exception{
        int userId = clientExiste(con, scanner);

        if(aAbonnementAdequat(con, userId, 2)){
            System.out.println("Vous avez l'abonnement adéquat pour entrer dans la salle de sport.");
        }else{
            System.out.println("Vous n'avez pas l'abonnement adéquat pour entrer dans la salle de sport.");
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
            
            //Si le choix est parmi les coachs possibles
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
        Statement generalStat = con.createStatement();
        
        int userId = clientExiste(con, scanner);

        // S'il peut s'inscrire à une session privée
        if(aAbonnementAdequat(con, userId, 2)){
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
                ResultSet checkAlreadyRegistered = generalStat.executeQuery("SELECT COUNT(*) FROM proj.client_sessionPrivee WHERE idClient = "+ userId +" AND idSessionPrivee = "+ choiceOfSession);
                checkAlreadyRegistered.next();
                int countAlreadyRegistered = checkAlreadyRegistered.getInt(1);
                checkAlreadyRegistered.close();

                if(countAlreadyRegistered > 0){
                    System.out.println("Vous êtes déja inscrit à cette session privée !");
                }
                else{
                    // Si le client n'est pas déja inscrit, on l'inscrit
                    generalStat.execute("INSERT INTO proj.client_sessionPrivee(idClient, idSessionPrivee) VALUES (" + userId + ", " + choiceOfSession + ")");
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
                    ResultSet AlreadyRegistred = generalStat.executeQuery("SELECT COUNT(*) FROM proj.client_sessionPrivee WHERE idClient = "+ userId+" AND idSessionPrivee = "+ userChoice);
                    AlreadyRegistred.next();
                    int ifAlreadyRegistred = AlreadyRegistred.getInt(1);
                    AlreadyRegistred.close();
                    
                    if(ifAlreadyRegistred > 0){
                        System.out.println("Vous êtes déja inscrit à cette session privée !");
                    }
                    else{
                        // Si le client n'est pas déja inscrit, on l'inscrit et on lui ajoute 4.99 EUR à son solde
                        generalStat.execute("INSERT INTO proj.client_sessionPrivee(idClient, idSessionPrivee) VALUES (" + userId + ", " + userChoice + ")");
                        ResultSet val = generalStat.executeQuery("SELECT solde FROM proj.client WHERE id = " + userId);
                        val.next();
                        double solde = val.getDouble(1) + 4.99;
                        val.close();
                        generalStat.execute("UPDATE proj.client SET solde = "+ solde +" WHERE id = " + userId);
                        System.out.println(
                            "Vous avez bien été inscrit à cette session!\nVotre solde viens d'augmenter de 4.99 EUR!" 
                            + "\nSi vous prenez plusieurs sessions privées, réflechissez à prendre un abonnement de plus haut niveau!"
                        );
                    }
                }else{
                    System.out.println("Numéro de session non reconnu, veuillez réessayer ultérieurement");
                }
            }
        }
        generalStat.close();
    }

    private static void changerCoach(Connection con, Scanner scanner) throws Exception{
        int userId = clientExiste(con, scanner);

        if(aAbonnementAdequat(con, userId, 4)){
            Statement generalStat = con.createStatement();

            System.out.println("\nListe de tous les coachs : ");

            ResultSet allCoach = generalStat.executeQuery("SELECT nom, prenom FROM proj.coach ORDER BY id");

            int count = 1;
            while(allCoach.next()){
                System.out.println(count++ + " : " + allCoach.getString(1) + " " + allCoach.getString(2));
            }
            System.out.println(count + " : Retirer le coach actuel");
            allCoach.close();

            int choice;
            // Redemande la valeur tant qu'elle n'est pas adéquate
            do{
                choice = scanner.nextInt();
                System.out.println("\nGot the choice = " + choice);
            }while(choice < 1 || choice > count);

            // Si l'utilisateur a choisi l'option "retirer coach"
            if(choice == count){
                generalStat.execute("UPDATE proj.client SET coachAttitrer = NULL WHERE id = " + userId);
            }else{
                generalStat.execute("UPDATE proj.client SET coachAttitrer = " + choice + " WHERE id = " + userId);
            }

            generalStat.close();
        }else{
            System.out.println("Malheuresement, vous ne possédez pas l'abonnement nécessaire pour pouvoir souscrire à un coach");
        }
    }


    //Util.
    // Vérifie si l'utilisateur existe, si c'est le cas, cela nous renvoie son id. Sinon la fonction envoie une exception/erreur.
    private static int clientExiste(Connection con, Scanner scanner) throws Exception{
        System.out.println("Nom : ");
        String nom = scanner.next();

        System.out.println("Prenom : ");
        String prenom = scanner.next();

        Statement generalStat = con.createStatement();
        ResultSet resUserId = generalStat.executeQuery("SELECT id FROM proj.client WHERE nom ILIKE \'" + nom + "\' AND prenom ILIKE \'" + prenom + "\'");

        boolean isFound = resUserId.next();

        if(isFound == false){
            throw new IllegalArgumentException(
                "\npartieClient.java ERREUR: Le client avec le nom : " + nom + " " + prenom + " n'a pas été trouvé. Veuillez vérifier si les données on été écrites correctement."
            );
        }

        // On ne retourne pas directement l'id, car on a besoin de fermer le "Statment generalStat" et le "ResultSet resUserId" avant
        int userId = resUserId.getInt(1);
        
        resUserId.close();
        generalStat.close();

        return userId;
    }

    // Seulement utilisable lorsque l'utilisateur a été confirmer dans la base de données !!!
    // Car cette fonction n'a pas de verification d'erreur.
    private static boolean aAbonnementAdequat(Connection con, int userId, int idAboReq) throws Exception{
        Statement generalStat = con.createStatement();

        //TODO Les informations ne sont pas collectées correctement : resultSet ne reçois pas ce qu'il faut.
        // Récupère l'abonnement mensuel de l'utilisateur si il n'est pas expiré
        ResultSet resTypeAbo = generalStat.executeQuery(
            "SELECT typeAbonnement From proj.client WHERE id =" + userId + " AND finAbonnement >= CURRENT_DATE"
        );
        // Récupère tous les abonnements temporaires de l'utilisateur qui n'ont pas expiré.
        Statement secondGeneralStat = con.createStatement();
        ResultSet resTypeAbotemp = secondGeneralStat.executeQuery(
            "SELECT typeAbonnement FROM proj.abonnementTemp WHERE idClient = " + userId + " AND finAbonnement >= CURRENT_DATE"
        );

        // Passe avec 2 boucles tant que par toutes les réponses donner par resTypeAbo et resTypeAboTemp
        // J'ai mis resTypeAbo dans une boucle aussi alors qu'il a forcément au maximum une seule valeure, pour éviter de devoir passer par des booléens de vérification.
        while(resTypeAbo.next()){
            int curAbo = resTypeAbo.getInt(1);
            String[] allSub = {"Pas abonnée", "Basic", "Premium", "Premium+"};
            double[] allPri = {0, 19.99, 29.99, 39.99};
            System.out.println("Le client à le statut mensuel suivant : " + allSub[curAbo-1]+ " à " + allPri[curAbo-1] + " EUR.");
            if(resTypeAbo.getInt(1) >= idAboReq){
                // Si trouvé, ferme toutes les demandes et renvoie vrai
                resTypeAbo.close();
                resTypeAbotemp.close();
                secondGeneralStat.close();
                generalStat.close();

                return true;
            }
        }
        // Si l'abonnement "principal" n'est pas adéquat, on vérifie les abonnements temporaires
        while(resTypeAbotemp.next()){
            // Si trouvé, ferme toutes les demandes et renvoie vrai
            System.out.println("On a le type dans journalier = " + resTypeAbotemp.getInt(1));
            if(resTypeAbotemp.getInt(1) >= idAboReq){
                resTypeAbo.close();
                resTypeAbotemp.close();
                secondGeneralStat.close();
                generalStat.close();

                return true;
            }
        }

        resTypeAbo.close();
        resTypeAbotemp.close();
        secondGeneralStat.close();
        generalStat.close();

        return false;
    }
}
