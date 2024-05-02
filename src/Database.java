//Libraries for database
import java.time.ZoneId;
import java.util.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Database {

    //Connection variables
    private String jdbcUrl = "jdbc:oracle:thin:@//oracle.glos.ac.uk:1521/orclpdb.chelt.local";
    private String username = "s4109300_DW";
    private String password = "s4109300_DW!";
    private Connection connection = null;

    //Connecting to database
    public void connectToDatabase(){
        //Attempt connecting to the database, display it works, and close the connection
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            if (connection != null) {
                System.out.println("Connected to the database!");
            } else {
                System.out.println("Failed to connect to the database!");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database!");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Login query
    public String login(String email, String pwd) {
        //If user is locked exit
        if (isAccountLocked(email)) {
            return "locked";
        }
        //Attempt to return a login if the user exists
        String loginQuery = "SELECT role FROM tblUser WHERE email = ? AND pwd = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = conn.prepareStatement(loginQuery)) {
            statement.setString(1, email);
            statement.setString(2, pwd);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    //Is account locked check
    private boolean isAccountLocked(String email) {
        //Get date, and compare it to the locked parameter in the database, if they are the same, return that the account is locked
        LocalDate currentDate = LocalDate.now();
        String query = "SELECT locked FROM tblUser WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Date lockedDate = resultSet.getDate("locked");
                    if (lockedDate != null) {
                        java.util.Date utilLockedDate = new java.util.Date(lockedDate.getTime());
                        LocalDate lockedLocalDate = utilLockedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        return lockedLocalDate.isEqual(currentDate);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Account is not locked
        return false;
    }

    //Lock account
    public void lockAccount(String email) {
        //If the email entered exists in the database, set locked to today's date
        LocalDate currentDate = LocalDate.now();
        String updateQuery = "UPDATE tblUser SET locked = ? WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = conn.prepareStatement(updateQuery)) {
            statement.setDate(1, java.sql.Date.valueOf(currentDate));
            statement.setString(2, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Retrieve book list
    public String[] getBookList(){
        //Get book list for book picker GUI
        List<String> bookList = new ArrayList<>();
        String query = "SELECT book_name FROM tblBook";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String bookName = rs.getString("book_name");
                if(bookName != null && !bookList.contains(bookName)) {
                    bookList.add(bookName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bookList.toArray(new String[0]);
    }

    //Get course list
    public String[] getCourseList(){
        //Get course list for the course picker GUI
        List<String> courseList = new ArrayList<>();
        String query = "SELECT course_name FROM tblCourse";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String courseName = rs.getString("course_name");
                if(courseName != null && !courseList.contains(courseName)) {
                    courseList.add(courseName);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courseList.toArray(new String[0]);
    }

    //Get book types
    public String[] getBookTypes(){
        //Get book types list for the book type picker GUI
        List<String> bookTypes = new ArrayList<>();
        String query = "SELECT book_type FROM tblBook";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String bookType = rs.getString("book_type");
                if(bookType != null && !bookTypes.contains(bookType)) {
                    bookTypes.add(bookType);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bookTypes.toArray(new String[0]);
    }

    //Get payment types
    public String[] getPaymentTypes(){
        //get payment types for payment type picker GUI
        List<String> paymentTypes = new ArrayList<>();
        String query = "SELECT payment_type FROM tblFine";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String paymentType = rs.getString("payment_type");
                if(paymentType != null && !paymentTypes.contains(paymentType)) {
                    paymentTypes.add(paymentType);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return paymentTypes.toArray(new String[0]);
    }

    //Taken books function
    public String takenBooks(Date date){
        String mostBorrowedBookId = null;
        String mostBorrowedBookName = null;

        //Get the most borrowed book in a month
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            String query = "SELECT book_id " +
                    "FROM tblLoan " +
                    "WHERE TO_CHAR(date_borrowed, 'YYYY-MM') = ? " +
                    "GROUP BY book_id " +
                    "ORDER BY COUNT(*) DESC " +
                    "FETCH FIRST 1 ROWS ONLY";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String monthYear = sdf.format(date);
                statement.setString(1, monthYear);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        mostBorrowedBookId = resultSet.getString("book_id");
                    }
                }
            }

            //If the book id is not ull, get the book name from the id
            if(mostBorrowedBookId != null){
                mostBorrowedBookName = getBookName(mostBorrowedBookId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Return most borrowed book name
        return mostBorrowedBookName;
    }

    //Unique borrowers per month
    public int uniqueStudents(Date date){
        int uniqueStudentsCount = 0;

        //Get the amount of unique students that have borrowed books in a month
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            String query = "SELECT COUNT(DISTINCT u_id) AS unique_students_count " +
                    "FROM tblLoan " +
                    "WHERE TO_CHAR(date_borrowed, 'YYYY-MM') = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String monthYear = sdf.format(date);
                statement.setString(1, monthYear);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        uniqueStudentsCount = resultSet.getInt("unique_students_count");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Return the amount of unique students that have borrowed books in a month
        return uniqueStudentsCount;
    }

    public String bookMostBorrowedByStudents(String course, int year){
        String mostBorrowedBookName = null;
        String mostBorrowedBookId = null;

        int courseId = getCourseId(course);

        // Connect to the database
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Create a PreparedStatement to execute the SQL query
            String query = "SELECT book_id " +
                    "FROM tblLoan " +
                    "WHERE TO_CHAR(date_borrowed, 'YYYY') = ? " +
                    "AND EXISTS (SELECT 1 FROM tblBook WHERE tblBook.book_id = tblLoan.book_id AND tblBook.course_id = ?) " +
                    "GROUP BY book_id " +
                    "ORDER BY COUNT(DISTINCT u_id) DESC " +
                    "FETCH FIRST 1 ROWS ONLY";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Set the parameters for the year and course in the SQL query
                statement.setInt(1, year);
                statement.setInt(2, courseId);

                // Execute the query
                try (ResultSet resultSet = statement.executeQuery()) {
                    // Check if there are any results
                    if (resultSet.next()) {
                        // Get the book_id of the most borrowed book
                        mostBorrowedBookId = resultSet.getString("book_id");
                    }
                }
            }

            // Retrieve the book name based on the most borrowed book ID
            if (mostBorrowedBookId != null) {
                mostBorrowedBookName = getBookName(mostBorrowedBookId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your application's requirements
        }

        // Return the name of the most borrowed book
        return mostBorrowedBookName;
    }

    //Most borrowed book in a course
    public String bookMostBorrowedInACourse(String course){
        //Get the course id based on teh course name
        String mostBorrowedBookName = null;
        int courseId = getCourseId(course);

        //Get the most borrowed book in the course
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            String query = "SELECT tblBook.book_name " +
                    "FROM tblLoan " +
                    "INNER JOIN tblBook ON tblLoan.book_id = tblBook.book_id " +
                    "WHERE EXISTS (SELECT 1 FROM tblBook WHERE tblBook.book_id = tblLoan.book_id AND tblBook.course_id = ?) " +
                    "GROUP BY tblBook.book_id, tblBook.book_name " +
                    "ORDER BY COUNT(*) DESC " +
                    "FETCH FIRST 1 ROWS ONLY";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, courseId);

                // Execute the query
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        mostBorrowedBookName = resultSet.getString("book_name");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Return the most borrowed book in the course
        return mostBorrowedBookName;
    }

    //Late returns percentage
    public double percentageReturnedLate(){
        //Queries for the total number of books and total number of late returns
        String totalBorrowedQuery = "SELECT COUNT(*) FROM tblLoan";
        String returnedLateQuery = "SELECT COUNT(*) FROM tblLoan l1 " +
                "INNER JOIN tblLoan l2 ON l1.loan_id = l2.loan_id " +
                "WHERE l2.date_returned - l1.date_borrowed > 7";

        //Get the number of books and number of books returned late, calculate what percentage of books were returned late
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = conn.createStatement()) {
            try (ResultSet totalBorrowedResult = statement.executeQuery(totalBorrowedQuery)) {
                if (totalBorrowedResult.next()) {
                    int totalBorrowed = totalBorrowedResult.getInt(1);
                    try (ResultSet returnedLateResult = statement.executeQuery(returnedLateQuery)) {
                        if (returnedLateResult.next()) {
                            int returnedLate = returnedLateResult.getInt(1);
                            if (totalBorrowed > 0) {
                                return ((double) returnedLate / totalBorrowed) * 100;
                            } else {
                                return 0.0;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Return 0 if there is an error
        return 0.0;
    }

    //How close is a book to being out of stock
    public int closeOutOfStock(Date date, String bookName){
        //Calculate the number of times the book was borrowed in a month
        String query = "SELECT COUNT(*) AS borrowed_copies, " +
                "tblBook.stock AS total_stock " +
                "FROM tblLoan " +
                "INNER JOIN tblBook ON tblLoan.book_id = tblBook.book_id " +
                "WHERE TO_CHAR(date_borrowed, 'YYYY-MM') = ? " +
                "AND tblBook.book_name = ? " +
                "GROUP BY tblBook.stock";

        //Execute query
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = conn.prepareStatement(query)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String monthYear = sdf.format(date);
            statement.setString(1, monthYear);
            statement.setString(2, bookName);

            //If there is borrowed copies, reduce that from total stock, otherwise return the total stock
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int borrowedCopies = resultSet.getInt("borrowed_copies");
                    int totalStock = resultSet.getInt("total_stock");

                    if (borrowedCopies == 0) {
                        return totalStock;
                    }
                    return totalStock - borrowedCopies;
                } else {
                    query = "SELECT stock FROM tblBook WHERE book_name = ?";

                    try (Connection conn1 = DriverManager.getConnection(jdbcUrl, username, password);
                        PreparedStatement statement1 = conn1.prepareStatement(query)) {
                        statement1.setString(1, bookName);

                        try (ResultSet resultSet1 = statement1.executeQuery()) {
                            if (resultSet1.next()) {
                                return resultSet1.getInt("stock");
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Return -1 when there is an error
        return -1;
    }

    //Return number of borrowed books in a month
    public int numberBorrowed(Date date) {
        //Calculate the number of books borrowed in a month
        String query = "SELECT COUNT(DISTINCT tblLoan.book_id) AS borrowed_books " +
                "FROM tblLoan " +
                "INNER JOIN tblBook ON tblLoan.book_id = tblBook.book_id " +
                "WHERE TO_CHAR(date_borrowed, 'YYYY-MM') = ? ";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = conn.prepareStatement(query)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String monthYear = sdf.format(date);
            statement.setString(1, monthYear);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int borrowedBooks = resultSet.getInt("borrowed_books");
                    return borrowedBooks;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Return 0 if there is an error
        return 0;
    }

    //Book types taken out
    public double bookTypeTakenOut(String type){
        //Calculate the percentage of books taken out by the selected type and return it
        String query = "SELECT (COUNT(CASE WHEN tb.book_type = ? THEN 1 END) / COUNT(*)) * 100 AS percentage " +
                "FROM tblLoan tl INNER JOIN tblBook tb ON tl.book_id = tb.book_id";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, type);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double percentage = resultSet.getDouble("percentage");
                    return percentage;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Return 0.0 if there is an error
        return 0.0;
    }

    //Loans in a course in a given month
    public int loansPerCourseInMonth(String course, Date date){

        //Get course id from its name
        int courseId = getCourseId(course);

        //Calculate total loans in a month by course and return that
        String query = "SELECT COUNT(*) AS total_loans " +
                "FROM tblLoan tl INNER JOIN tblBook tb ON tl.book_id = tb.book_id " +
                "WHERE tb.course_id = ? AND TO_CHAR(tl.date_borrowed, 'YYYY-MM') = TO_CHAR(?, 'YYYY-MM')";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, courseId);
            statement.setDate(2, new java.sql.Date(date.getTime()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int totalLoans = resultSet.getInt("total_loans");
                    return totalLoans;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Return 0 if there are no loans
        return 0;
    }

    //Payment method most used for fine
    public double finesPerPaymentMethod(String paymentMethod){
        //Calculate the total fine amount that is paid by a specific payment_type
        double percentage = 0;
        String query = "SELECT SUM(fine_amount) AS total_fines FROM tblFine WHERE payment_type = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, paymentMethod);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double finesForPaymentMethod = resultSet.getDouble("total_fines");
                    double totalFinesOverall = getTotalFinesOverall();
                    if (totalFinesOverall > 0) {
                        percentage = (finesForPaymentMethod / totalFinesOverall) * 100;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Returning the percentage
        return percentage;
    }

    //Total fines paid
    private double getTotalFinesOverall() throws SQLException {
        //Calculate the total fines paid and return that
        double totalFines = 0;
        String query = "SELECT SUM(fine_amount) AS total_fines FROM tblFine";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                totalFines = resultSet.getDouble("total_fines");
            }
        }

        //Return total fines
        return totalFines;
    }

    //Time to pay fine payments
    public int timeToFinePayment(){
        //Calculate the average days that it takes to pay the fines and return it
        int averageDays = 0;
        String query = "SELECT AVG(tf.date_paid - tl.date_returned) AS average_days " +
                "FROM tblFine tf " +
                "INNER JOIN tblLoan tl ON tf.fine_id = tl.fine_id " +
                "WHERE tf.date_paid IS NOT NULL AND tl.date_returned IS NOT NULL";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                averageDays = resultSet.getInt("average_days");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Return the average days
        return averageDays;
    }

    //Fines paid in a specific momth
    public int finesPaidInMonth(Date date){
        //Calculate the amount of fines money paid in a month and return it
        int totalFines = 0;
        String query = "SELECT SUM(fine_amount) AS total_fines " +
                "FROM tblFine " +
                "WHERE EXTRACT(MONTH FROM date_paid) = EXTRACT(MONTH FROM TO_DATE(?, 'YYYY-MM')) " +
                "AND EXTRACT(YEAR FROM date_paid) = EXTRACT(YEAR FROM TO_DATE(?, 'YYYY-MM'))";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String monthYear = sdf.format(date);
            statement.setString(1, monthYear);
            statement.setString(2, monthYear);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    totalFines = resultSet.getInt("total_fines");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Return total fines amount
        return totalFines;
    }

    //Get course id from course name
    public int getCourseId(String course){
        //Finding the course name from a given course id and returning it
        String query = "SELECT course_id FROM tblCourse WHERE course_name = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, course);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("course_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Returning course id
        return 0;
    }

    //Get book name
    public String getBookName(String id){
        //Get book name from a book id and return it
        String bookName = "";
        if (id != null) {
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                String bookNameQuery = "SELECT book_name " +
                        "FROM tblBook " +
                        "WHERE book_id = ?";
                try (PreparedStatement nameStatement = connection.prepareStatement(bookNameQuery)) {
                    nameStatement.setString(1, id);
                    try (ResultSet nameResultSet = nameStatement.executeQuery()) {
                        if (nameResultSet.next()) {
                            bookName = nameResultSet.getString("book_name");
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        //Returning book name
        return bookName;
    }

}
