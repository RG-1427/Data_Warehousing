import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Properties;

public class Main {
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
        database.connectToDatabase();
        welcome();
    }

    public static void welcome() {
        //OUTPUT GUI
        // Create a JFrame for the login window
        frame.setSize(500, 100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create a panel for the login components
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        // Add components to the panel
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

        // Add action listener to the submit button
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call login method with user input
                String email = emailField.getText();
                String pwd = new String(pwdField.getPassword());
                login(email, pwd);

                // Clear input fields after login attempt
                emailField.setText("");
                pwdField.setText("");
            }
        });

        // Add the panel to the frame and display the window
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public static void login(String email, String pwd){
        role = database.login(email, pwd);
        closeLogin(email);
    }

    public static void logout(JFrame frame){
        role = "";
        passwordAttempts = 1;
        frame.dispose();
        welcome();
    }

    public static void closeLogin(String email){
        if(passwordAttempts >= 3){
            database.lockAccount(email);
            frame.dispose();
            passwordAttempts = 1;
        } else{
            if (role.isEmpty()) {
                String message = "Incorrect email or password.\n" +
                        "You have " + (3 - passwordAttempts) + " attempt(s) left until the application shuts down.";
                JOptionPane.showMessageDialog(frame, message, "Login Error", JOptionPane.ERROR_MESSAGE);
                passwordAttempts++;
            } else {
                frame.dispose();
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

    public static void student(){
        JFrame studentPanel = new JFrame("Student Panel");
        studentPanel.setSize(300, 200);
        studentPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        studentPanel.setLayout(new GridLayout(5, 1));

        // Add buttons to the panel
        JButton loanBookButton = new JButton("Loan Book");
        JButton returnBookButton = new JButton("Return Book");
        JButton loanHistoryButton = new JButton("Loan History");
        JButton fineHistoryButton = new JButton("Fine History");
        JButton logoutButton = new JButton("Logout");

        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Call logout method from Main class and provide it with the JFrame created for this GUI
                logout(studentPanel);
            }
        });

        studentPanel.add(loanBookButton);
        studentPanel.add(returnBookButton);
        studentPanel.add(loanHistoryButton);
        studentPanel.add(fineHistoryButton);
        studentPanel.add(logoutButton);

        // Set frame visibility to true
        studentPanel.setVisible(true);
    }

    public static void schoolHead(){
        JFrame schoolHeadPanel = new JFrame("School Head Panel");
        schoolHeadPanel.setSize(500, 250);
        schoolHeadPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        schoolHeadPanel.setLayout(new GridLayout(3, 1));

        // Add buttons to the panel
        JButton takenBooks = new JButton("Most Taken Books Within a Month");
        JButton uniqueStudents = new JButton("Unique Students Within a Month");
        JButton logoutButton = new JButton("Logout");

        takenBooks.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Date date = monthPicker();
                String taken;
                if(date != null) {
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

        uniqueStudents.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Date date = monthPicker();
                if(date != null) {
                    int unique = database.uniqueStudents(date);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
                    String chosenMonth = sdf.format(date);
                    showMessageDialog("There were " + unique + " students borrowing books in " + chosenMonth);
                }
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Call logout method from Main class and provide it with the JFrame created for this GUI
                logout(schoolHeadPanel);
            }
        });

        schoolHeadPanel.add(takenBooks);
        schoolHeadPanel.add(uniqueStudents);
        schoolHeadPanel.add(logoutButton);

        // Set frame visibility to true
        schoolHeadPanel.setVisible(true);
    }

    public static void courseLeader(){
        JFrame courseLeaderPanel = new JFrame("Course Leader Panel");
        courseLeaderPanel.setSize(500, 250);
        courseLeaderPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        courseLeaderPanel.setLayout(new GridLayout(3, 1));

        // Add buttons to the panel
        JButton mostBorrowedBook = new JButton("Most Borrowed Book");
        JButton mostBorrowedBookWithinCourse = new JButton("Most Borrowed Book Within a Course");
        JButton logoutButton = new JButton("Logout");

        mostBorrowedBook.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String course = pickCourse(database.getCourseList());
                if(course != null){
                    Date date = monthPicker();
                    if(date != null) {
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

        mostBorrowedBookWithinCourse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String course = pickCourse(database.getCourseList());
                if(course != null) {
                    String book = database.bookMostBorrowedInACourse(course);
                    if(book != null) {
                        showMessageDialog("The book most borrowed in the course " + course + " is " + book);
                    } else{
                        showMessageDialog("There were no books borrowed in the course " + course);
                    }
                }
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Call logout method from Main class and provide it with the JFrame created for this GUI
                logout(courseLeaderPanel);
            }
        });

        courseLeaderPanel.add(mostBorrowedBook);
        courseLeaderPanel.add(mostBorrowedBookWithinCourse);
        courseLeaderPanel.add(logoutButton);

        // Set frame visibility to true
        courseLeaderPanel.setVisible(true);
    }

    public static void headLibrarian(){
        JFrame headLibrarianPanel = new JFrame("Head Librarian Panel");
        headLibrarianPanel.setSize(500, 250);
        headLibrarianPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        headLibrarianPanel.setLayout(new GridLayout(3, 1));

        // Add buttons to the panel
        JButton booksLate = new JButton("Percentage of Books Returned Late Per Month");
        JButton outOfStock = new JButton("How Close is a Book to Being Out of Stock");
        JButton logoutButton = new JButton("Logout");

        booksLate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                double percentage = database.percentageReturnedLate();
                showMessageDialog("The percentage of books returned late is " + percentage);
            }
        });

        outOfStock.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String book = pickBook(database.getBookList());
                if(book != null){
                    Date date = monthPicker();
                    if(date != null) {
                        int closeOutOfStock = database.closeOutOfStock(date, book);
                        showMessageDialog(book + " is " + closeOutOfStock + " copies away from being out of stock");
                    }
                }
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Call logout method from Main class and provide it with the JFrame created for this GUI
                logout(headLibrarianPanel);
            }
        });

        headLibrarianPanel.add(booksLate);
        headLibrarianPanel.add(outOfStock);
        headLibrarianPanel.add(logoutButton);

        // Set frame visibility to true
        headLibrarianPanel.setVisible(true);
    }

    public static void viceChancellor(){
        JFrame chancellorPanel = new JFrame("Vice Chancellor Panel");
        chancellorPanel.setSize(500, 250);
        chancellorPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chancellorPanel.setLayout(new GridLayout(4, 1));

        // Add buttons to the panel
        JButton booksBorrowed = new JButton("Number of Books Borrowed Per Month");
        JButton bookType = new JButton("Percentage Book Type Being Taken Out");
        JButton loansNum = new JButton("Total Loans Per Course in a Month");
        JButton logoutButton = new JButton("Logout");

        booksBorrowed.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Date date = monthPicker();
                if(date != null) {
                    double per = database.numberBorrowed(date);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
                    String month = sdf.format(date);
                    showMessageDialog("The number of books borrowed within " + month + " is " + per);
                }
            }
        });

        bookType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String bookType = pickType(database.getBookTypes());
                if(bookType != null) {
                    double type = database.bookTypeTakenOut(bookType);
                    showMessageDialog("The percentage of loans of " + bookType + " is " + type);
                }
            }
        });

        loansNum.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String course = pickCourse(database.getCourseList());
                if(course != null){
                    Date date = monthPicker();
                    if(date != null){
                        int loans = database.loansPerCourseInMonth(course, date);
                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
                        String month = sdf.format(date);
                        showMessageDialog("There were " + loans + " loan(s) in " + course + " within " + month);
                    }
                }
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Call logout method from Main class and provide it with the JFrame created for this GUI
                logout(chancellorPanel);
            }
        });

        chancellorPanel.add(booksBorrowed);
        chancellorPanel.add(bookType);
        chancellorPanel.add(loansNum);
        chancellorPanel.add(logoutButton);

        // Set frame visibility to true
        chancellorPanel.setVisible(true);
    }

    public static void incomeTeam(){
        JFrame incomeTeamPanel = new JFrame("Income Team Panel");
        incomeTeamPanel.setSize(500, 250);
        incomeTeamPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        incomeTeamPanel.setLayout(new GridLayout(4, 1));

        // Add buttons to the panel
        JButton paymentMethod = new JButton("Percentage of Fines with Payment Method");
        JButton finePayment = new JButton("Time to Pay Fine");
        JButton finesPaid = new JButton("Fines Paid in a Month");
        JButton logoutButton = new JButton("Logout");

        paymentMethod.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String payment = pickPaymentType(database.getPaymentTypes());
                if(payment != null) {
                    double per = database.finesPerPaymentMethod(payment);
                    showMessageDialog(per + " % of fines are paid by " + payment);
                }
            }
        });

        finePayment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int days = database.timeToFinePayment();
                showMessageDialog("It takes a student an average of " + days + " to pay their fines");
            }
        });

        finesPaid.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Date date = monthPicker();
                if(date != null) {
                    int amount = database.finesPaidInMonth(date);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
                    String month = sdf.format(date);
                    showMessageDialog(amount + "£ was paid in " + month + " for fines");
                }
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Call logout method from Main class and provide it with the JFrame created for this GUI
                logout(incomeTeamPanel);
            }
        });

        incomeTeamPanel.add(paymentMethod);
        incomeTeamPanel.add(finePayment);
        incomeTeamPanel.add(finesPaid);
        incomeTeamPanel.add(logoutButton);

        // Set frame visibility to true
        incomeTeamPanel.setVisible(true);
    }

    public static Date monthPicker() {
        JFrame parentFrame = new JFrame();
        JDialog dialog = new JDialog(parentFrame, "Pick Month / Year", true);

        // Create the date picker
        Properties properties = new Properties();
        UtilDateModel model = new UtilDateModel();
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateComponentFormatter());

        // Create the "OK" button
        JButton okButton = new JButton("OK");

        // Add action listener to the "OK" button
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Get the selected date from the date picker
                selectedDate = (Date) datePicker.getModel().getValue();

                // Close the dialog
                dialog.dispose();
            }
        });

        // Layout components
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(datePicker);
        panel.add(okButton);

        // Add panel to the dialog
        dialog.add(panel);

        // Set dialog properties
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        // Return the selected date
        return selectedDate;
    }

    public static String pickCourse(String[] courses) {
        JFrame parentFrame = new JFrame();
        JDialog dialog = new JDialog(parentFrame, "Pick Course", true);

        // Create the JComboBox with the list of courses
        JComboBox<String> courseComboBox = new JComboBox<>(courses);

        // Create the "OK" button
        JButton okButton = new JButton("OK");

        // Add action listener to the "OK" button
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Get the selected course from the combo box
                selectedCourse = (String) courseComboBox.getSelectedItem();

                // Close the dialog
                dialog.dispose();
            }
        });

        // Layout components
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(courseComboBox);
        panel.add(okButton);

        // Add panel to the dialog
        dialog.add(panel);

        // Set dialog properties
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        // Return the selected course
        return selectedCourse;
    }

    public static String pickBook(String[] books) {
        JFrame parentFrame = new JFrame();
        JDialog dialog = new JDialog(parentFrame, "Pick Book", true);

        // Create the JComboBox with the list of courses
        JComboBox<String> bookComboBox = new JComboBox<>(books);

        // Create the "OK" button
        JButton okButton = new JButton("OK");

        // Add action listener to the "OK" button
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Get the selected course from the combo box
                selectedBook = (String) bookComboBox.getSelectedItem();

                // Close the dialog
                dialog.dispose();
            }
        });

        // Layout components
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(bookComboBox);
        panel.add(okButton);

        // Add panel to the dialog
        dialog.add(panel);

        // Set dialog properties
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        // Return the selected course
        return selectedBook;
    }

    public static String pickType(String[] types) {
        JFrame parentFrame = new JFrame();
        JDialog dialog = new JDialog(parentFrame, "Pick Book Type", true);

        // Create the JComboBox with the list of courses
        JComboBox<String> typeComboBox = new JComboBox<>(types);

        // Create the "OK" button
        JButton okButton = new JButton("OK");

        // Add action listener to the "OK" button
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Get the selected course from the combo box
                selectedBookType = (String) typeComboBox.getSelectedItem();

                // Close the dialog
                dialog.dispose();
            }
        });

        // Layout components
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(typeComboBox);
        panel.add(okButton);

        // Add panel to the dialog
        dialog.add(panel);

        // Set dialog properties
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        // Return the selected course
        return selectedBookType;
    }

    public static String pickPaymentType(String[] paymentTypes) {
        JFrame parentFrame = new JFrame();
        JDialog dialog = new JDialog(parentFrame, "Pick Payment Type", true);

        // Create the JComboBox with the list of courses
        JComboBox<String> paymentTypeComboBox = new JComboBox<>(paymentTypes);

        // Create the "OK" button
        JButton okButton = new JButton("OK");

        // Add action listener to the "OK" button
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Get the selected course from the combo box
                selectedPaymentType = (String) paymentTypeComboBox.getSelectedItem();

                // Close the dialog
                dialog.dispose();
            }
        });

        // Layout components
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(paymentTypeComboBox);
        panel.add(okButton);

        // Add panel to the dialog
        dialog.add(panel);

        // Set dialog properties
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        // Return the selected course
        return selectedPaymentType;
    }

    public static void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

}