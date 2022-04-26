import java.sql.* ;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.*;

public class GoBabbyApp {
    // Helper function to check if user input is numeric
    public static boolean isNumeric(String str){
        try{
            Double.parseDouble(str);
            return true;
        }
        catch(NumberFormatException e){
            return false;
        }
    }
    public static void main ( String [ ] args ) throws SQLException
    {
        // Unique table names.  Either the user supplies a unique identifier as a command line argument, or the program makes one up.
        String tableName = "";
        int sqlCode=0;      // Variable to hold SQLCODE
        String sqlState="00000";  // Variable to hold SQLSTATE

        if ( args.length > 0 )
            tableName += args [ 0 ] ;
        else
            tableName += "exampletbl";

        // Register the driver.  You must register the driver before you can use it.
        try { DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ; }
        catch (Exception cnfe){ System.out.println("Class not found"); }

        // This is the url you must use for DB2.
        //Note: This url may not valid now ! Check for the correct year and semester and server name.
        String url = "jdbc:db2://winter2022-comp421.cs.mcgill.ca:50000/cs421";

        // TODO: REMEMBER to remove your user id and password before submitting your code!!
        String your_userid = "thoang17";
        String your_password = "jkc&?mOv";
        //AS AN ALTERNATIVE, you can just set your password in the shell environment in the Unix (as shown below) and read it from there.
        //$  export SOCSPASSWD=yoursocspasswd
        if(your_userid == null && (your_userid = System.getenv("SOCSUSER")) == null)
        {
            System.err.println("Error!! do not have a password to connect to the database!");
            System.exit(1);
        }
        if(your_password == null && (your_password = System.getenv("SOCSPASSWD")) == null)
        {
            System.err.println("Error!! do not have a password to connect to the database!");
            System.exit(1);
        }
        Connection con = DriverManager.getConnection (url,your_userid,your_password) ;
        Statement statement = con.createStatement ( ) ;

        // START OF ASSIGNMENT
        outer: while(true){
            System.out.println("Please enter your practitioner id [E] to exit:");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if(input.equals("E")){
                break;
            }
            else{
                ResultSet rs = statement.executeQuery("SELECT pracid, name FROM Midwife WHERE pracid=" + input);
                if(rs.next()){
                    dateLoop: while(true) {
                        System.out.println("Please enter the date for appointment list [E] to exit:");
                        String input2 = scanner.nextLine();
                        if (input2.equals("E")) {
                            break outer;
                        }
                        else {
                            appointLoop: while(true) {
                                ResultSet rs2 = statement.executeQuery("SELECT time, backup, name, mhealthid, appointid FROM Mother JOIN (SELECT Pregnancy.coupleid, Appointment.time, Appointment.backup, Appointment.appointid FROM Appointment JOIN Pregnancy ON Appointment.pregid = Pregnancy.pregid WHERE Appointment.pracid = " + input + " AND Appointment.date= " + "'" + input2 + "'" + ")t1 ON Mother.coupleid = t1.coupleid ORDER BY time");
                                HashMap<Integer, String> hmap = new HashMap<>();
                                HashMap<Integer, Integer> appointmentMap = new HashMap<>();
                                int count = 1;
                                boolean flag = false;
                                while (rs2.next()) {
                                    flag = true;
                                    String time = rs2.getString("time");
                                    Boolean backup = rs2.getBoolean("backup");
                                    String backupString;
                                    if (!backup) {
                                        backupString = "P";
                                    } else {
                                        backupString = "B";
                                    }
                                    String motherName = rs2.getString("name");
                                    String mhealthid = rs2.getString("mhealthid");
                                    System.out.println(count + ": " + time + " " + backupString + " " + motherName + " " + mhealthid);
                                    hmap.put(count, motherName + " " + mhealthid);
                                    appointmentMap.put(count, rs2.getInt("appointid"));
                                    count++;
                                }
                                if (!flag){
                                    continue dateLoop;
                                }
                                System.out.println("Enter the appointment number that you would like to work on. [E] to exit [D] to go back to another date :");
                                String input3 = scanner.nextLine();
                                if (input3.equals("E")) {
                                    break outer;
                                } else if (input3.equals("D")) {
                                    continue dateLoop;
                                } else if (isNumeric(input3)) {
                                    while (true) {
                                        System.out.println("For " + hmap.get(Integer.parseInt(input3)));
                                        System.out.println("");
                                        System.out.println("1. Review notes");
                                        System.out.println("2. Review tests");
                                        System.out.println("3. Add a note");
                                        System.out.println("4. Prescribe a test");
                                        System.out.println("5. Go back to the appointments");
                                        System.out.println("");
                                        System.out.println("Enter your choice:");
                                        String input4 = scanner.nextLine();
                                        // 1. Review notes
                                        int appointid = appointmentMap.get(Integer.parseInt(input3));
                                        ResultSet rpreg = statement.executeQuery("SELECT pregid FROM Appointment WHERE appointid = " + appointid);
                                        int pregid = -1;
                                        while (rpreg.next()) {
                                            pregid = rpreg.getInt("pregid");
                                        }
                                        if (Integer.parseInt(input4) == 1) {
                                            // Loop through observation
                                            ResultSet rs3 = statement.executeQuery("SELECT date, time, text FROM Observation JOIN (SELECT appointid FROM Appointment WHERE pregid= " + pregid + ")t1 ON Observation.appointid = t1.appointid ORDER BY date, time DESC");
                                            while (rs3.next()) {
                                                String date = rs3.getString("date");
                                                String time = rs3.getString("time");
                                                String text = rs3.getString("text");
                                                System.out.println(date + " " + time + " " + text);
                                            }
                                        }
                                        // 2. Review tests
                                        else if (Integer.parseInt(input4) == 2) {
                                            ResultSet rTest = statement.executeQuery("SELECT prescdate, type, result FROM (SELECT testid FROM TestReceived WHERE pregid= " + pregid + ")t1 JOIN Test ON t1.testid = Test.testid ORDER BY prescdate DESC");
                                            while (rTest.next()) {
                                                String date = rTest.getString("prescdate");
                                                String type = rTest.getString("type");
                                                String result = rTest.getString("result");
//                                                System.out.println(result);
                                                if (result == null) {
                                                    result = "PENDING";
                                                }
                                                System.out.println(date + " [" + type + "] " + result);
                                            }
                                        }
                                        // 3. Add a note
                                        else if (Integer.parseInt(input4) == 3) {
                                            System.out.println("Please type your observation:");
                                            String input5 = scanner.nextLine();
                                            // Current date
                                            ResultSet rDate = statement.executeQuery("SELECT current date FROM sysibm.sysdummy1");
                                            String date = "";
                                            while (rDate.next()) {
                                                date = rDate.getString("1");
//                                                System.out.println(date);
                                            }
                                            ResultSet rTime = statement.executeQuery("SELECT current time FROM sysibm.sysdummy1");
                                            String time = "";
                                            while (rTime.next()) {
                                                time = rTime.getString("1");
//                                                System.out.println(time);
                                            }
                                            statement.executeUpdate("INSERT INTO Observation VALUES (" + appointid + ", \'" + time + "\', \'" + date + "\', \'" + input5 + "\')");
                                        }
                                        // 4. Prescribe a test
                                        else if (Integer.parseInt(input4) == 4) {
                                            System.out.println("Please enter the type of test:");
                                            String typeInput = scanner.nextLine();
                                            // Current date
                                            ResultSet rDate = statement.executeQuery("SELECT current date FROM sysibm.sysdummy1");
                                            String date = "";
                                            while (rDate.next()) {
                                                date = rDate.getString("1");
                                            }
                                            // Find latest test id to assign
                                            ResultSet rTest = statement.executeQuery("SELECt testid FROM Test");
                                            int testid = 1;
                                            while (rTest.next()) {
                                                testid++;
                                            }
                                            // Insert into Test
                                            statement.executeUpdate("INSERT INTO Test VALUES (" + testid + ", \'" + date + "\', NULL, \'" + date + "\', NULL, \'" + typeInput + "\', NULL)");
                                            // Insert into TestPrescribed
                                            statement.executeUpdate("INSERT INTO TestPrescribed VALUES (" + appointid + ", " + testid + ")");
                                            // Insert into TestReceived
                                            statement.executeUpdate("INSERT INTO TestReceived VALUES (" + pregid + ", " + testid + ")");
                                        }
                                        else if (Integer.parseInt(input4) == 5) {
                                            continue appointLoop;
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
                System.out.println("The practitioner id is not valid.");
            }

        }

        // Finally but importantly close the statement and connection
        statement.close ( ) ;
        con.close ( ) ;
    }
}
