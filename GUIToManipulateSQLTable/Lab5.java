import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;

public class Lab5 extends JFrame implements ActionListener {
    private JTextArea prompt;
    private JTextField MemID;
    private JTextField FirstName;
    private JTextField LastName;
    private JTextField DOB;
    private JTextField Gender;
    private JTextField ISBN;
    private JTextField Title;
    private JTextField Author;

    private JPanel panel;
    private JButton enter;
    private JButton exit;
    private JButton ISBNb;
    private JButton Titleb;
    private JButton Authorb;
    private JButton Reset;

    JComboBox<String> books;


    private Statement stmt;


    private Lab5(Connection conn) throws SQLException{
        super("Lab5");
        stmt = conn.createStatement();
        setBounds(10,10,300,150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        add(panel);
        prompt = new JTextArea("Enter Member ID: ");
        panel.add(prompt);
        MemID = new JTextField(8);
        panel.add(MemID);
        enter = new JButton("Enter");
        enter.addActionListener(this);
        panel.add(enter);
        //pack();
    }

    private void resetPanel(){
        remove(panel);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        add(panel);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            String choice = e.getActionCommand();
            if (choice.equals("Enter")) {
                String MemIDEntered = MemID.getText();
                ResultSet rs1 = stmt.executeQuery("SELECT * FROM Member WHERE MemberID=" + MemIDEntered);
                if (rs1.next()) {
                    String Name = rs1.getString("First_Name") + " " + rs1.getString("Last_Name");
                    resetPanel();
                    prompt.setText("Welcome " + Name + "! Would you like to check out a book?");
                    panel.add(prompt);
                    panel.add(new JTextArea("ISBN Search"));
                    ISBN = new JTextField(14);
                    panel.add(ISBN);
                    ISBNb = new JButton("ISBN");
                    ISBNb.addActionListener(this);
                    panel.add(ISBNb);
                    panel.add(new JTextArea("Title Search"));
                    Title = new JTextField(60);
                    panel.add(Title);
                    Titleb = new JButton("Title");
                    Titleb.addActionListener(this);
                    panel.add(Titleb);
                    panel.add(new JTextArea("Author Search"));
                    Author = new JTextField(30);
                    panel.add(Author);
                    Authorb = new JButton("Author");
                    Authorb.addActionListener(this);
                    panel.add(Authorb);
                } else {
                    panel.remove(MemID);
                    panel.remove(enter);
                    prompt.setText("Account not found! Create new member account?");
                    panel.add(new JTextArea("First Name: "));
                    FirstName = new JTextField(30);
                    panel.add(FirstName);
                    panel.add(new JTextArea("Last Name: "));
                    LastName = new JTextField(30);
                    panel.add(LastName);
                    //Needs to be certain format
                    panel.add(new JTextArea("Date of Birth (yyyy-mm-dd): "));
                    DOB = new JTextField(11);
                    panel.add(DOB);
                    panel.add(new JTextArea("Gender (M/F) (optional): "));
                    Gender = new JTextField(1);
                    panel.add(Gender);
                    exit = new JButton("Exit");
                    exit.addActionListener(this);
                    panel.add(exit);
                    enter.setText("Register");
                    panel.add(enter);
                }
                pack();
                rs1.close();
            } else if (choice.equals("Register")) {
                    resetPanel();
                    if(isParsable(MemID.getText())){
                        if(Gender.getText().length() == 0) {
                            stmt.execute("INSERT INTO Member (MemberID, First_Name, Last_Name, DOB) VALUES ('" + MemID.getText() + "', '"  + FirstName.getText() + "', '"
                                    + LastName.getText() + "', '" + DOB.getText() + "')");
                            ActionEvent action = new ActionEvent(this, ActionEvent.ACTION_FIRST, "Enter");
                            actionPerformed(action);
                        }else if(Gender.getText().equals("M") || Gender.getText().equals("F")){
                            stmt.execute("INSERT INTO Member (MemberID, First_Name, Last_Name, DOB, Gender) VALUES ('" + MemID.getText() + "', '"  + FirstName.getText() + "', '"
                                    + LastName.getText() + "', '" + DOB.getText() + "', '" + Gender.getText() + "')");
                            ActionEvent action = new ActionEvent(this, ActionEvent.ACTION_FIRST, "Enter");
                            actionPerformed(action);
                        }else{
                            panel.add(new JTextArea("The Gender You Entered: " + Gender.getText() + "is bad, Gender Must Be A M or F!"));
                            Reset = new JButton("Reset");
                            Reset.addActionListener(this);
                            panel.add(Reset);
                            pack();
                        }
                    }else{
                        panel.add(new JTextArea("The MemberID You Entered: " + MemID.getText() + "is bad, MemberID Must Be A Number!"));
                        Reset = new JButton("Reset");
                        Reset.addActionListener(this);
                        panel.add(Reset);
                        pack();
                    }

            } else if (choice.equals("ISBN")) {
                ResultSet rs1 = stmt.executeQuery("SELECT * FROM StoredOn WHERE ISBN='" + ISBN.getText() + "'");
                resetPanel();
                int numCopies = 0;
                if (rs1.next()) {
                    numCopies += Integer.parseInt(rs1.getString("TotalCopies"));
                    if (numCopies >= 1) {
                        panel.add(new JTextArea("Your Book is At " + rs1.getString("LibName") + " On Shelf " + rs1.getString("Shelf_Number")));
                    }
                    if (rs1.next()) {
                        numCopies = 1;
                        numCopies += Integer.parseInt(rs1.getString("TotalCopies"));
                        if (numCopies > 1) {
                            panel.add(new JTextArea("Your Book is Also At " + rs1.getString("LibName") + " On Shelf " + rs1.getString("Shelf_Number")));
                            numCopies--;
                        }
                    }
                    if (numCopies == 0) {
                        panel.add(new JTextArea("We have this book in our catalog, but it is checked out everywhere :/"));
                    }
                } else {
                    panel.add(new JTextArea("We Don't Currently Have This Book In Our Catalog :("));
                }
                Reset = new JButton("Reset");
                Reset.addActionListener(this);
                panel.add(Reset);
                pack();
                rs1.close();

            } else if (choice.equals("Title")) {
                ResultSet rs1 = stmt.executeQuery("SELECT * FROM Book WHERE Title LIKE '%" + Title.getText() + "%'");
                ArrayList<String> titles1 = new ArrayList<String>();
                resetPanel();
                while (rs1.next()) {
                    titles1.add(rs1.getString("Title"));
                }
                String[] titles2 = new String[titles1.size()];
                for (int i = 0; i < titles1.size(); i++) {
                    titles2[i] = titles1.get(i);
                }
                if (titles1.size() < 1) {
                    panel.add(new JTextArea("We Don't Currently Have This Book In Our Catalog :("));
                    Reset = new JButton("Reset");
                    Reset.addActionListener(this);
                    panel.add(Reset);
                } else {
                    panel.add(new JTextArea("We Found These Books Similar To Your Input, Please Choose One"));
                    books = new JComboBox<String>(titles2);
                    books.setActionCommand("TitleSearch");
                    books.addActionListener(this);
                    panel.add(books);
                }
                pack();
                rs1.close();
            } else if (choice.equals("TitleSearch")) {
                if (e.getSource() instanceof JComboBox) {
                    JComboBox cb = (JComboBox) e.getSource();
                    String title = (String) cb.getSelectedItem();
                    ResultSet rs1 = stmt.executeQuery("SELECT * FROM Book WHERE Title='" + title.trim() + "'");
                    rs1.next();
                    ISBN.setText(rs1.getString("ISBN"));
                    ActionEvent action = new ActionEvent(this, ActionEvent.ACTION_FIRST, "ISBN");
                    actionPerformed(action);
                }

            } else if (choice.equals("Author")) {
                String Names[] = Author.getText().split(" ");
                ResultSet rs1 = null;
                if (Names.length == 1) {
                    rs1 = stmt.executeQuery("SELECT * FROM Author WHERE FirstName LIKE '%" + Names[0] + "%'");
                } else if (Names.length == 2) {
                    rs1 = stmt.executeQuery("SELECT * FROM Author WHERE FirstName LIKE '%" + Names[0] + "%' OR LastName LIKE '%" + Names[1] + "%'");
                }
                ArrayList<String> authors1 = new ArrayList<String>();
                resetPanel();
                while (rs1.next()) {
                    authors1.add(rs1.getString("FirstName") + " " + rs1.getString("LastName"));
                }
                String[] authors2 = new String[authors1.size()];
                for (int i = 0; i < authors1.size(); i++) {
                    authors2[i] = authors1.get(i);
                }
                if (authors1.size() < 1) {
                    panel.add(new JTextArea("We Didn't Find Your Author In Our Catalog :("));
                    Reset = new JButton("Reset");
                    Reset.addActionListener(this);
                    panel.add(Reset);
                } else {
                    panel.add(new JTextArea("We Found These Authors Similar To Your Input, Please Choose One"));
                    books = new JComboBox<String>(authors2);
                    books.setActionCommand("AuthorSearch");
                    books.addActionListener(this);
                    panel.add(books);
                }
                pack();
                rs1.close();

            }else if (choice.equals("AuthorSearch")) {
                    if (e.getSource() instanceof JComboBox) {
                        JComboBox cb = (JComboBox) e.getSource();
                        String[] Author = ((String) cb.getSelectedItem()).split(" ");
                        ResultSet rs1 = stmt.executeQuery("SELECT * FROM Author WHERE FirstName='" + Author[0] + "' AND LastName='" + Author[1] + "'");
                        rs1.next();
                        String authID = rs1.getString("AuthorID");
                        rs1.close();
                        rs1 = stmt.executeQuery("SELECT * FROM WrittenBy WHERE AuthorID='" + authID + "'");
                        resetPanel();
                        ArrayList<String> ISBNs1 = new ArrayList<String>();
                        while (rs1.next()) {
                            ISBNs1.add(rs1.getString("ISBN"));
                        }
                        rs1.close();
                        String[] titles2 = new String[ISBNs1.size()];
                        for (int i = 0; i < ISBNs1.size(); i++) {
                            rs1 = stmt.executeQuery("SELECT * FROM Book WHERE ISBN='" + ISBNs1.get(i) + "'");
                            rs1.next();
                            titles2[i] = rs1.getString("Title");
                            rs1.close();
                        }
                        if (titles2.length < 1) {
                            panel.add(new JTextArea("We Don't Currently Have This Book In Our Catalog :("));
                            Reset = new JButton("Reset");
                            Reset.addActionListener(this);
                            panel.add(Reset);
                        } else {
                            panel.add(new JTextArea("We Found These Books Authored By " + cb.getSelectedItem() + ", Please Choose One"));
                            books = new JComboBox<String>(titles2);
                            books.setActionCommand("TitleSearch");
                            books.addActionListener(this);
                            panel.add(books);
                        }
                        pack();
                        rs1.close();


                    }
            } else if (choice.equals("Reset")) {
                remove(panel);
                setBounds(10, 10, 300, 150);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                add(panel);
                prompt = new JTextArea("Enter Member ID: ");
                panel.add(prompt);
                MemID = new JTextField(8);
                panel.add(MemID);
                enter = new JButton("Enter");
                enter.addActionListener(this);
                panel.add(enter);
            } else if (choice.equals("Exit")) {
                System.exit(0);
            }

        } catch (SQLException s) {
            s.printStackTrace();
        }

    }



    public boolean isParsable(String input){
        try{
            Integer.valueOf(input);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }



    public static void main(String[] args) {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            //String url = "jdbc:mysql://localhost:3306/ehharris?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            String url = "jdbc:mysql://faure:3306/ehharris?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            Connection c  = DriverManager.getConnection(url,"ehharris", "ass");

            Lab5 l5 = new Lab5(c);
            l5.setVisible(true);


        }catch (ClassNotFoundException c){
            c.printStackTrace();
        }catch (SQLException s){
            s.printStackTrace();
        }




    }
}
