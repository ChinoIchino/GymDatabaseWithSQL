import java.sql.*;
import java.util.Scanner;

class App{
    public static void main(String []args) throws Exception{
        Scanner scanner = new Scanner(System.in);

        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://kafka.iem/st655935", "st655935", "st655935");

        // Collecte le nom de tous les schémas de la connexion dans un ResultSet
        DatabaseMetaData metaDataOfDatabase = con.getMetaData();
        ResultSet stat = metaDataOfDatabase.getSchemas();

        boolean correctSchemaExist = false;
        // Vérifie si le schéma nommé "proj" existe
        while(stat.next()){
            if(stat.getString(1).equals("proj")){
                correctSchemaExist = true;
                break;
            }
        }
        
        // S'il n'a pas été trouvé, envoie une erreur associée
        if(!correctSchemaExist){
            // Je n'ai pas trouvé d'erreur adequate, donc j'ai mis SQLException.
            // Il était possible de crée sa propre erreur avec un objet qui étend SQLException, cependant, je trouvais que ça aurait été trop hors-sujet par rapport au projet.
            throw new SQLException(
                "App.java Erreur: Le schéma 'proj' n'a pas été trouvé, vérifiez si vous avez sur DBeaver dans le dossier Schemas un schéma nommé 'proj'"
            );
        }
        
        System.out.print("Voulez-vous réinitialiser la base de données ? (1 = Oui / 0 = Non) : ");
        int initChoice = scanner.nextInt();

        if(initChoice == 1){
            InitDatabase.initAll(con);
        }

        int choice = 0;
        while(choice != 3){
            System.out.println("\n----------------Menu Principal----------------\nQuel menu ouvrir: \n1. Menu Client\n2. Menu Professionnel\n3. Quitter"
                                +"\n----------------Menu Principal----------------\n");
            choice = scanner.nextInt();
            
            if(choice == 1){
                partieClient.main(args, con, scanner);
            }else if(choice == 2){
                partiePro.main(args, con, scanner);
            }
        }

        scanner.close();
    }
}
