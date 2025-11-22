import java.sql.*;
import java.util.Scanner;

class App{
    public static void main(String []args) throws Exception{
        Scanner scanner = new Scanner(System.in);

        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://kafka.iem/kb483753", "kb483753", "kb483753");

        System.out.print("Voulez vous reinitialiser la base de donnees ? (1 = Oui / 0 = Non) : ");
        int initChoice = scanner.nextInt();

        if(initChoice == 1){
            InitDatabase.initAll(con);
        }

        int choice = 0;
        while(choice != 3){
            System.out.println("\n//////////////////////////////////////////////\nQuel menu a ouvrir: \n1. Menu Client\n2. Menu Professionnelle\n3. Quitter\n");
            choice = scanner.nextInt();
            
            if(choice == 1){
                Inscription.main(args, con, scanner);
            }else if(choice == 2){
                WorkerPart.main(args, con, scanner);
            }
        }

        scanner.close();
    }
}