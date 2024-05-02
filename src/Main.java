//Libraries for date picker GUI
import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

//Library for Java dates
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

//Libraries for GUI
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//Java Libraries
import java.util.Date;
import java.util.Properties;

public class Main {
    //Variables needed for functionality
    public static int passwordAttempts = 1;
    public static String role = "";
    public static Database database = new Database();
    public static JFrame frame = new JFrame("Welcome to the Gloucestershire Library");
    private static Date selectedDate;
    private static String selectedCourse;
    private static String selectedBook;
    private static String selectedBookType;
    private static String selectedPaymentType;

    public static void main(String[] args) {
        //Connecting to databases and displaying the login GUI
        database.connectToDatabase();
        welcome();
    }

    //Login GUI
    public static void welcome() {
        //GUI layout
        frame.setSize(500, 100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        //GUI components
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();
        JLabel pwdLabel = new JLabel("Password:");
        JPasswordField pwdField = new JPasswordField();
        JButton submitButton = new JButton("Submit");
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(pwdLabel);
        panel.add(pwdField);
        panel.add(submitButton);

        //Login button
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Get user email and password entered and try to log in
                String email = emailField.getText();
                String pwd = new String(pwdField.getPassword());
                login(email, pwd);

                //Clear inputed values
                emailField.setText("");
                pwdField.setText("");
            }
        });

        //Setup GUI style
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    //Login
    public static void login(String email, String pwd){
        //Calling the login and closing the screen
        role = database.login(email, pwd);
        closeLogin(email);
    }

    //Logout
    public static void logout(JFrame frame){
        //Resetting variables and calling the welcome window
        role = "";
        passwordAttempts = 1;
        frame.dispose();
        welcome();
    }

    //Closing the screen
    public static void closeLogin(String email){
        //If there is more than 3 attempts, lock account, and close the frame
        if(passwordAttempts >= 3){
            database.lockAccount(email);
            frame.dispose();
            passwordAttempts = 1;
        } else{
            //If the role is empty, let the user know they got the login wrong
            if (role.isEmpty()) {
                String message = "Incorrect email or password.\n" +
                        "You have " + (3 - passwordAttempts) + " attempt(s) left until the application shuts down.";
                JOptionPane.showMessageDialog(frame, message, "Login Error", JOptionPane.ERROR_MESSAGE);
                passwordAttempts++;
            } else {
                //Close frame
                frame.dispose();

                //Based on the role open up the correct GUI and if locked display message to user
                switch (role) {
                    case "student":
                        student();
                        break;
                    case "vice chancellor":
                        viceChancellor();
                        break;
                    case "income team":
                        incomeTeam();
                        break;
                    case "school head":
                        schoolHead();
                        break;
                    case "course leader":
                        courseLeader();
                        break;
                    case "head librarian":
                        headLibrarian();
                        break;
                    case "locked":
                        showMessageDialog("Your account is currently locked because of too many attempts. Try again tomorrow.");
                        break;
                }
            }
        }
    }

    //Student GUI
    public static void student(){
        //GUI setup
        JFrame studentPanel = new JFrame("Student Panel");
        studentPanel.setSize(300, 200);
        studentPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        studentPanel.setLayout(new GridLayout(5, 1));
        JButton loanBookButton = new JButton("Loan Book");
        JButton returnBookButton = new JButton("Return Book");
        JButton loanHistoryButton = new JButton("Loan History");
        JButton fineHistoryButton = new JButton("Fine History");
        JButton logoutButton = new JButton("Logout");

        //Logout function
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Call logout method from Main class and provide it with the JFrame created for this GUI
                logout(studentPanel);
            }
        });

        //Adding the buttons to the GUI and making them visible
        studentPanel.add(loanBookButton);
        studentPanel.add(returnBookButton);
        studentPanel.add(loanHistoryButton);
        studentPanel.add(fineHistoryButton);
        studentPanel.add(logoutButton);
        studentPanel.setVisible(true);
    }

    //School head GUI
    public static void schoolHead(){
        //GUI setup
        JFrame schoolHeadPanel = new JFrame("School Head Panel");
        schoolHeadPanel.setSize(500, 250);
        schoolHeadPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        schoolHeadPanel.setLayout(new GridLayout(3, 1));
        JButton takenBooks = new JButton("Most Taken Books Within a Month");
        JButton uniqueStudents = new JButton("Unique Students Within a Month");
        JButton logoutButton = new JButton("Logout");

        //Button 1 action
        takenBooks.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Date date = monthPicker();
                String taken;
                //If date is not null
                if(date != null) {
                    //See how many books were borrowed in a month and display
                    taken = database.takenBooks(date);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
                    String chosenMonth = sdf.format(date);
                    if (taken != null) {
                        showMessageDialog("The most borrowed book within " + chosenMonth +" is " + taken);
                    } else {
                        showMessageDialog("There were no books loaned in " + chosenMonth);
                    }
                }
            }
        });

        //Button 2
        uniqueStudents.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Pick date, if it is not null calculate how many students borrowed book in that month
                Date date = monthPicker();
                if(date != null) {
                    int unique = database.uniqueStudents(date);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
                    String chosenMonth = sdf.format(date);
                    showMessageDialog("There were " + unique + " students borrowing books in " + chosenMonth);
                }
            }
        });

        //Logout function
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logout(schoolHeadPanel);
            }
        });

        //Setting up buttons visibility
        schoolHeadPanel.add(takenBooks);
        schoolHeadPanel.add(uniqueStudents);
        schoolHeadPanel.add(logoutButton);
        schoolHeadPanel.setVisible(true);
    }

    //Course leader GUI
    public static void courseLeader(){
        //GUI setup
        JFrame courseLeaderPanel = new JFrame("Course Leader Panel");
        courseLeaderPanel.setSize(500, 250);
        courseLeaderPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        courseLeaderPanel.setLayout(new GridLayout(3, 1));
        JButton mostBorrowedBook = new JButton("Most Borrowed Book");
        JButton mostBorrowedBookWithinCourse = new JButton("Most Borrowed Book Within a Course");
        JButton logoutButton = new JButton("Logout");

        //Button 1
        mostBorrowedBook.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Get course
                String course = pickCourse(database.getCourseList());
                if(course != null){
                    //If course is not null, get date
                    Date date = monthPicker();
                    if(date != null) {
                        //If date is not null, get the most borrowed book in a course in a year
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        int year = cal.get(Calendar.YEAR);
                        String book = database.bookMostBorrowedByStudents(course, year);
                        if (book != null) {
                            showMessageDialog("The book most borrowed in the course " + course + " in the year " + year + " is " + book);
                        } else {
                            showMessageDialog("There were no books borrowed in the course " + course + " in the year " + year);
                        }
                    }
                }
            }
        });

        //Button 2
        mostBorrowedBookWithinCourse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Select course
                String course = pickCourse(database.getCourseList());
                if(course != null) {
                    //If course in not null, display the book most borrowed in the course
                    String book = database.bookMostBorrowedInACourse(course);
                    if(book != null) {
                        showMessageDialog("The book most borrowed in the course " + course + " is " + book);
                    } else{
                        showMessageDialog("There were no books borrowed in the course " + course);
                    }
                }
            }
        });

        //Logout button
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logout(courseLeaderPanel);
            }
        });

        //Setting up GUI
        courseLeaderPanel.add(mostBorrowedBook);
        courseLeaderPanel.add(mostBorrowedBookWithinCourse);
        courseLeaderPanel.add(logoutButton);
        courseLeaderPanel.setVisible(true);
    }

    //Head librarian GUI
    public static void headLibrarian(){
        //GUI setup
        JFrame headLibrarianPanel = new JFrame("Head Librarian Panel");
        headLibrarianPanel.setSize(500, 250);
        headLibrarianPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        headLibrarianPanel.setLayout(new GridLayout(3, 1));
        JButton booksLate = new JButton("Percentage of Books Returned Late Per Month");
        JButton outOfStock = new JButton("How Close is a Book to Being Out of Stock");
        JButton logoutButton = new JButton("Logout");

        //Button 1
        booksLate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Calculate the percentage of books returned late
                double percentage = database.percentageReturnedLate();
                showMessageDialog("The percentage of books returned late is " + percentage);
            }
        });

        //Button 2
        outOfStock.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Get a book
                String book = pickBook(database.getBookList());
                if(book != null){
                    //If book is not null get a date
                    Date date = monthPicker();
                    if(date != null) {
                        //If the date is not null, return how close the book is from being out of stock
                        int closeOutOfStock = database.closeOutOfStock(date, book);
                        showMessageDialog(book + " is " + closeOutOfStock + " copies away from being out of stock");
                    }
                }
            }
        });

        //Logout button
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logout(headLibrarianPanel);
            }
        });

        //Setting up GUI
        headLibrarianPanel.add(booksLate);
        headLibrarianPanel.add(outOfStock);
        headLibrarianPanel.add(logoutButton);
        headLibrarianPanel.setVisible(true);
    }

    //Vice Chancellor GUI
    public static void viceChancellor(){
        //GUI setup
        JFrame chancellorPanel = new JFrame("Vice Chancellor Panel");
        chancellorPanel.setSize(500, 250);
        chancellorPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chancellorPanel.setLayout(new GridLayout(4, 1));
        JButton booksBorrowed = new JButton("Number of Books Borrowed Per Month");
        JButton bookType = new JButton("Percentage Book Type Being Taken Out");
        JButton loansNum = new JButton("Total Loans Per Course in a Month");
        JButton logoutButton = new JButton("Logout");

        //Button 1
        booksBorrowed.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Pick a date
                Date date = monthPicker();
                if(date != null) {
                    //If date is not null, return the number of books borrowed in that month
                    double per = database.numberBorrowed(date);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
                    String month = sdf.format(date);
                    showMessageDialog("The number of books borrowed within " + month + " is " + per);
                }
            }
        });

        //Button 2
        bookType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Select book type
                String bookType = pickType(database.getBookTypes());
                if(bookType != null) {
                    //Return the percentage of loans of that book type
                    double type = database.bookTypeTakenOut(bookType);
                    showMessageDialog("The percentage of loans of " + bookType + " is " + type);
                }
            }
        });

        //Button 3
        loansNum.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Pick course
                String course = pickCourse(database.getCourseList());
                if(course != null){
                    //Pick date
                    Date date = monthPicker();
                    if(date != null){
                        //Return the number of loans within a course within a month
                        int loans = database.loansPerCourseInMonth(course, date);
                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
                        String month = sdf.format(date);
                        showMessageDialog("There were " + loans + " loan(s) in " + course + " within " + month);
                    }
                }
            }
        });

        //Logout button
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logout(chancellorPanel);
            }
        });

        //Setting up GUI
        chancellorPanel.add(booksBorrowed);
        chancellorPanel.add(bookType);
        chancellorPanel.add(loansNum);
        chancellorPanel.add(logoutButton);
        chancellorPanel.setVisible(true);
    }

    //Income team GUI
    public static void incomeTeam(){
        //GUI setup
        JFrame incomeTeamPanel = new JFrame("Income Team Panel");
        incomeTeamPanel.setSize(500, 250);
        incomeTeamPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        incomeTeamPanel.setLayout(new GridLayout(4, 1));
        JButton paymentMethod = new JButton("Percentage of Fines with Payment Method");
        JButton finePayment = new JButton("Time to Pay Fine");
        JButton finesPaid = new JButton("Fines Paid in a Month");
        JButton logoutButton = new JButton("Logout");

        //Button 1
        paymentMethod.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Get payment type
                String payment = pickPaymentType(database.getPaymentTypes());
                if(payment != null) {
                    //Calculate percentage of fines paid per payment method
                    double per = database.finesPerPaymentMethod(payment);
                    showMessageDialog(per + " % of fines are paid by " + payment);
                }
            }
        });

        //Button 2
        finePayment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Display the average return time for a loan
                int days = database.timeToFinePayment();
                showMessageDialog("It takes a student an average of " + days + " to pay their fines");
            }
        });

        //Button 3
        finesPaid.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Pick a date
                Date date = monthPicker();
                if(date != null) {
                    //Calculate the amount of fines paid per month
                    int amount = database.finesPaidInMonth(date);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
                    String month = sdf.format(date);
                    showMessageDialog(amount + "£ was paid in " + month + " for fines");
                }
            }
        });

        //Logout
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logout(incomeTeamPanel);
            }
        });

        //Setting up GUI
        incomeTeamPanel.add(paymentMethod);
        incomeTeamPanel.add(finePayment);
        incomeTeamPanel.add(finesPaid);
        incomeTeamPanel.add(logoutButton);
        incomeTeamPanel.setVisible(true);
    }

    //Month picker GUI
    public static Date monthPicker() {
        //Creating a GUI with a date picker
        JFrame parentFrame = new JFrame();
        JDialog dialog = new JDialog(parentFrame, "Pick Month / Year", true);
        Properties properties = new Properties();
        UtilDateModel model = new UtilDateModel();
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateComponentFormatter());
        JButton okButton = new JButton("OK");

        //Confirmation button
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Returning the date and closing the GUI
                selectedDate = (Date) datePicker.getModel().getValue();
                dialog.dispose();
            }
        });

        //Setting up GUI
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(datePicker);
        panel.add(okButton);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        //Return the date
        return selectedDate;
    }

    //Picking course GUI
    public static String pickCourse(String[] courses) {
        //GUI setup
        JFrame parentFrame = new JFrame();
        JDialog dialog = new JDialog(parentFrame, "Pick Course", true);
        JComboBox<String> courseComboBox = new JComboBox<>(courses);
        JButton okButton = new JButton("OK");

        //Confirmation Button
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Get course and close the GUI
                selectedCourse = (String) courseComboBox.getSelectedItem();
                dialog.dispose();
            }
        });

        ////Setting up GUI
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(courseComboBox);
        panel.add(okButton);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        //Return teh course
        return selectedCourse;
    }

    //Pick book GUI
    public static String pickBook(String[] books) {
        //GUI setup
        JFrame parentFrame = new JFrame();
        JDialog dialog = new JDialog(parentFrame, "Pick Book", true);
        JComboBox<String> bookComboBox = new JComboBox<>(books);
        JButton okButton = new JButton("OK");

        //Conformation Button
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Getting the book and closing GUI
                selectedBook = (String) bookComboBox.getSelectedItem();
                dialog.dispose();
            }
        });

        //Setting up GUI
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(bookComboBox);
        panel.add(okButton);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        //Return selected book
        return selectedBook;
    }

    //Book type GUI
    public static String pickType(String[] types) {
        //GUI setup
        JFrame parentFrame = new JFrame();
        JDialog dialog = new JDialog(parentFrame, "Pick Book Type", true);
        JComboBox<String> typeComboBox = new JComboBox<>(types);
        JButton okButton = new JButton("OK");

        //Conformation button
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Getting book type and closing GUI
                selectedBookType = (String) typeComboBox.getSelectedItem();
                dialog.dispose();
            }
        });

        //Setting up GUI
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(typeComboBox);
        panel.add(okButton);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        //Return selected book type
        return selectedBookType;
    }

    //Pick payment type GUI
    public static String pickPaymentType(String[] paymentTypes) {
        //GUI setup
        JFrame parentFrame = new JFrame();
        JDialog dialog = new JDialog(parentFrame, "Pick Payment Type", true);
        JComboBox<String> paymentTypeComboBox = new JComboBox<>(paymentTypes);
        JButton okButton = new JButton("OK");

        //Conformation button
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Get payment type and close GUI
                selectedPaymentType = (String) paymentTypeComboBox.getSelectedItem();
                dialog.dispose();
            }
        });

        //Setting up GUI
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(paymentTypeComboBox);
        panel.add(okButton);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        //Return selected payment
        return selectedPaymentType;
    }

    //Message GUI to show user a message
    public static void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

}