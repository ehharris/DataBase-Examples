import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.sql.*;
import java.util.ArrayList;

public class L4 {

    //arrayLists to hold XML data
    private static ArrayList<String> checkInMemID = new ArrayList<>();
    private static ArrayList<String> checkInISBN = new ArrayList<>();
    private static ArrayList<String> checkInDate = new ArrayList<>();
    private static ArrayList<String> checkOutMemID = new ArrayList<>();
    private static ArrayList<String> checkOutISBN = new ArrayList<>();
    private static ArrayList<String> checkOutDate = new ArrayList<>();

    private static void readXML(String fileName) {
        try {
            File file = new File(fileName);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeLst = doc.getElementsByTagName("Borrowed_by");

            for (int s = 0; s < nodeLst.getLength(); s++) {

                Node fstNode = nodeLst.item(s);

                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element sectionNode = (Element) fstNode;

                    NodeList memberIdElementList = sectionNode.getElementsByTagName("MemberID");
                    Element memberIdElmnt = (Element) memberIdElementList.item(0);
                    NodeList memberIdNodeList = memberIdElmnt.getChildNodes();
                    String MemberID = ((Node) memberIdNodeList.item(0)).getNodeValue().trim();

                    NodeList secnoElementList = sectionNode.getElementsByTagName("ISBN");
                    Element secnoElmnt = (Element) secnoElementList.item(0);
                    NodeList secno = secnoElmnt.getChildNodes();
                    String ISBN = ((Node) secno.item(0)).getNodeValue().trim();

                    NodeList codateElementList = sectionNode.getElementsByTagName("Checkout_date");
                    Element codElmnt = (Element) codateElementList.item(0);
                    NodeList cod = codElmnt.getChildNodes();
                    String Checkout_date = ((Node) cod.item(0)).getNodeValue().trim();

                    NodeList cidateElementList = sectionNode.getElementsByTagName("Checkin_date");
                    Element cidElmnt = (Element) cidateElementList.item(0);
                    NodeList cid = cidElmnt.getChildNodes();
                    String Checkin_date = ((Node) cid.item(0)).getNodeValue().trim();

                    if(Checkin_date.equals("N/A")){
                        String[] badCheckout_dates = Checkout_date.split("/");
                        Checkout_date = badCheckout_dates[2] + "/" + badCheckout_dates[0] + "/" + badCheckout_dates[1];
                        checkOutMemID.add(MemberID);
                        checkOutISBN.add(ISBN);
                        checkOutDate.add(Checkout_date);
                    } else if(Checkout_date.equals("N/A")){
                        String[] badCheckin_dates = Checkin_date.split("/");
                        Checkin_date = badCheckin_dates[2] + "/" + badCheckin_dates[0] + "/" + badCheckin_dates[1];
                        checkInMemID.add(MemberID);
                        checkInISBN.add(ISBN);
                        checkInDate.add(Checkin_date);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkinBooks(Statement stmt, Statement stmt2) throws SQLException {
        ArrayList<String> sqlQueriesToExecute = new ArrayList<>();
        ArrayList<Integer> indiciesToRemove = new ArrayList<>();
        ResultSet rs1 = stmt.executeQuery("SELECT * FROM BorrowedBy WHERE CheckinDate IS NULL");
        //Check to see what can be checked in
        while (rs1.next()) {
            String MemID = rs1.getString("MemberID");
            String ISBN = rs1.getString("ISBN");
            for (int i = 0; i < checkInMemID.size(); i++) {
                if (checkInMemID.get(i).equals(MemID) && checkInISBN.get(i).equals(ISBN)) {
                    sqlQueriesToExecute.add("UPDATE BorrowedBy " +
                            "SET CheckinDate='" + checkInDate.get(i) +
                            "' WHERE MemberID='" + MemID +
                            "' AND ISBN='" + ISBN + "'");
                    //Updates number of copies of the book available
                    ResultSet rs2 = stmt2.executeQuery("SELECT * FROM StoredOn WHERE ISBN='" + ISBN + "'");
                    rs2.next();
                    String libName = rs2.getString("LibName");
                    int totalCopies = Integer.parseInt(rs2.getString("TotalCopies"));
                    totalCopies++;
                    sqlQueriesToExecute.add("UPDATE StoredOn " +
                            "SET TotalCopies=" + totalCopies +
                            " WHERE ISBN='" + ISBN +
                            "' AND LibName='" + libName + "'");
                    rs2.close();
                    indiciesToRemove.add(i);
                    System.out.println("Checked in " + ISBN + " to " + libName + "!");
                }
            }
        }
        for (String query : sqlQueriesToExecute) {
            try {
                stmt.executeUpdate(query);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error in query: " + query + " This was not successful!");
            }
        }
        //Removes checkin requests
        int count = 0;
        for(int index: indiciesToRemove){
            checkInMemID.remove(index-count);
            checkInISBN.remove(index-count);
            checkInDate.remove(index-count);
            count++;
        }
        sqlQueriesToExecute.clear();
        rs1.close();
    }

    private static void checkoutBooks(Statement stmt, Statement stmt2) throws SQLException{
        ArrayList<Integer> indiciesToRemove = new ArrayList<>();
        for(int i = 0; i < checkOutMemID.size(); i++) {
            boolean checkedOut = false;
            ResultSet rs1 = stmt.executeQuery("SELECT * FROM StoredOn WHERE TotalCopies>0");
            while (rs1.next()) {
                String ISBN = rs1.getString("ISBN");
                if(checkOutISBN.get(i).equals(ISBN) && !checkedOut){
                    checkedOut = true;

                    //Decreases number of copies in library
                    String LibName = rs1.getString("LibName");
                    int totalCopies = Integer.parseInt(rs1.getString("TotalCopies"));
                    totalCopies--;
                    try {
                        stmt2.executeUpdate("UPDATE StoredOn " +
                                "SET TotalCopies=" + totalCopies +
                                " WHERE ISBN='" + ISBN +
                                "' AND LibName='" + LibName + "'");
                    } catch (Exception e){
                        e.printStackTrace();
                        System.out.println("Couldn't update number of total copies for " + ISBN + " in " + LibName);
                    }

                    //Creates BorrowedBy record
                    try {
                        stmt2.executeUpdate("INSERT INTO BorrowedBy " +
                                "VALUES (" + Integer.parseInt(checkOutMemID.get(i)) +
                                ", '" + ISBN +
                                "', '" + checkOutDate.get(i) +
                                "', NULL)");
                    } catch (Exception e){
                        e.printStackTrace();
                        System.out.println("Couldn't create checkout record for" + checkOutMemID.get(i) + "with book " + ISBN);
                    }
                }

            }
            rs1.close();
            if(checkedOut){
                indiciesToRemove.add(i);
                System.out.println("Successfully checked out " + checkOutISBN.get(i) +
                        " to Member " + checkOutMemID.get(i) + "!");
            }
        }
        //Removes checkout requests
        int count = 0;
        for(int index: indiciesToRemove){
            checkOutMemID.remove(index-count);
            checkOutISBN.remove(index-count);
            checkOutDate.remove(index-count);
            count++;
        }
    }

    private static void checkinErrors(){
        if(checkInMemID.size() > 0){
            System.out.println("The following records were unable to be checked in because there is no record of them being checked out!");
            for(int i = 0; i < checkInMemID.size(); i++) {
                System.out.println("Record: MemID: " + checkInMemID.get(i) + " ISBN: " + checkInISBN.get(i) + " CheckIn Date: " + checkInDate.get(i));
            }
        }
    }

    private static void checkOutErrors(){
        if(checkOutMemID.size() > 0){
            System.out.println("The following records were unable to be checked out because book being checked out doesn't exist in our libraries!");
            for(int i = 0; i < checkOutMemID.size(); i++) {
                System.out.println("Record: MemID: " + checkOutMemID.get(i) + " ISBN: " + checkOutISBN.get(i) + " CheckOut Date: " + checkOutDate.get(i));
            }
        }
    }

    public static void main(String args[]){
        try {
            readXML("/s/bach/a/class/cs430dl/Current/more_assignments/LabData/Libdata.xml");
        }catch( Exception e ) {
            e.printStackTrace();

        }//end catch
//        for(int i = 0; i < checkInMemID.size(); i++){
//            System.out.println(checkInMemID.get(i));
//            System.out.println(checkInISBN.get(i));
//            System.out.println(checkInDate.get(i));
//        }
//        System.out.println();
//        for(int i = 0; i < checkOutMemID.size(); i++){
//            System.out.println(checkOutMemID.get(i));
//            System.out.println(checkOutISBN.get(i));
//            System.out.println(checkOutDate.get(i));
//        }


        Connection con = null;

        try {
            Statement stmt;
            Statement stmt2;

            // Register the JDBC driver for MySQL.
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Define URL of database server for
            // database named 'user' on the faure.
            String url =
                    "jdbc:mysql://faure:3306/ehharris?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

            // Get a connection to the database for a
            // user named 'user' with the password
            // 123456789.
            con = DriverManager.getConnection(
                    url,"ehharris", "ass");

            // Display URL and connection information
            //System.out.println("URL: " + url);
            //System.out.println("Connection: " + con);

            // Get a Statement object
            stmt = con.createStatement();
            stmt2 = con.createStatement();

            //This will checkin everything available that's already been checked out
            //then checkout everything everything else and check one more time to check in everything else.
            //If something can't be checked in/out it'll error (error's are inside the methods) and keep going
            try{
                checkinBooks(stmt, stmt2);
                checkoutBooks(stmt, stmt2);
                //Checks in again to see if any books that couldn't be checked in before can now be checked in
                checkinBooks(stmt, stmt2);

                //Checks to see if any statements couldn't be processed
                checkinErrors();
                checkOutErrors();
            }catch(Exception e){
                System.out.print(e);
                System.out.println("Something went wrong executing a query!");
            }//end catch

            con.close();
        }catch( Exception e ) {
            e.printStackTrace();

        }//end catch

    }//end main

}//end class
