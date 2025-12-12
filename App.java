import java.sql.*;
import java.util.Scanner;

class App{
    public static void main(String []args) throws Exception{
        Scanner scanner = new Scanner(System.in);

        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://kafka.iem/kb483753", "kb483753", "kb483753");

        // Collecte le nom de tout les schemas de la connection dans un ResultSet
        DatabaseMetaData metaDataOfDatabase = con.getMetaData();
        ResultSet stat = metaDataOfDatabase.getSchemas();

        boolean correctSchemaExist = false;
        // Verifie si le schema du nom de proj existe
        while(stat.next()){
            if(stat.getString(1).equals("proj")){
                correctSchemaExist = true;
                break;
            }
        }
        
        // Si il n'a pas été trouver, envoie une erreur associée
        if(!correctSchemaExist){
            // Je n'ai pas trouver une erreur adequate, donc j'ai mis SQLException.
            // Il etait possible de cree sa propre erreur avec un objet qui extend SQLException, cependant je trouvais que ca aurait etait trop hors-sujet par rapport au projet
            throw new SQLException(
                "App.java Erreur: Le schema 'proj' n'a pas été trouver, verifier si vous avez sur DBeaver dans le dossier Schemas un schema du nom de 'proj'"
            );
        }
        
        System.out.print("Voulez vous reinitialiser la base de donnees ? (1 = Oui / 0 = Non) : ");
        int initChoice = scanner.nextInt();

        if(initChoice == 1){
            InitDatabase.initAll(con);
        }

        int choice = 0;
        while(choice != 3){
            System.out.println("\n----------------Menu Principal----------------\nQuel menu a ouvrir: \n1. Menu Client\n2. Menu Professionnel\n3. Quitter"
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