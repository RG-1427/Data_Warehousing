import java.time.ZoneId;
import java.util.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Database {

    // JDBC URL, username, and password
    private String jdbcUrl = "jdbc:oracle:thin:@//oracle.glos.ac.uk:1521/orclpdb.chelt.local";
    private String username = "s4109300_DW";
    private String password = "s4109300_DW!";
    private Connection connection = null;

    public void connectToDatabase(){

        try {
            // Register the JDBC driver
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Create a connection
            connection = DriverManager.getConnection(jdbcUrl, username, password);

            // Check if the connection is successful
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
            // Close the connection
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String login(String email, String pwd) {
        if (isAccountLocked(email)) {
            System.out.println("Account locked.");
            return "locked";
        }
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

    private boolean isAccountLocked(String email) {
        // Get today's date
        LocalDate currentDate = LocalDate.now();

        // SQL query to retrieve the locked_date for the specified email
        String query = "SELECT locked FROM tblUser WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, email); // Set the email parameter

            // Execute the query
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Date lockedDate = resultSet.getDate("locked");
                    if (lockedDate != null) {
                        java.util.Date utilLockedDate = new java.util.Date(lockedDate.getTime());

                        // Convert java.util.Date to LocalDate
                        LocalDate lockedLocalDate = utilLockedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                        // Check if the locked date is equal to today's date
                        return lockedLocalDate.isEqual(currentDate);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Account is not locked or not found
        return false;
    }

    public void lockAccount(String email) {
        // Get today's date
        LocalDate currentDate = LocalDate.now();

        // SQL update query to set the locked_date to today's date
        String updateQuery = "UPDATE tblUser SET locked = ? WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = conn.prepareStatement(updateQuery)) {
            statement.setDate(1, java.sql.Date.valueOf(currentDate)); // Set the locked_date parameter to today's date
            statement.setString(2, email); // Set the email parameter

            // Execute the update query
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Account locked successfully.");
            } else {
                System.out.println("Failed to lock account.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String[] getBookList(){
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
            // Handle the exception according to your application's requirements
        }

        return bookList.toArray(new String[0]);
    }

    public String[] getCourseList(){
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
            // Handle the exception according to your application's requirements
        }

        return courseList.toArray(new String[0]);
    }

    public String[] getBookTypes(){
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
            // Handle the exception according to your application's requirements
        }

        return bookTypes.toArray(new String[0]);
    }

    public String[] getPaymentTypes(){
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
            // Handle the exception according to your application's requirements
        }

        return paymentTypes.toArray(new String[0]);
    }

    public String takenBooks(Date date){
        String mostBorrowedBookId = null;
        String mostBorrowedBookName = null;

        // Connect to the database
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Create a PreparedStatement to execute the SQL query to find the most borrowed book ID
            String query = "SELECT book_id " +
                    "FROM tblLoan " +
                    "WHERE TO_CHAR(date_borrowed, 'YYYY-MM') = ? " +
                    "GROUP BY book_id " +
                    "ORDER BY COUNT(*) DESC " +
                    "FETCH FIRST 1 ROWS ONLY";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Set the parameter for the month in the SQL query
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String monthYear = sdf.format(date);
                statement.setString(1, monthYear);

                // Execute the query to find the most borrowed book ID
                try (ResultSet resultSet = statement.executeQuery()) {
                    // Check if there are any results
                    if (resultSet.next()) {
                        // Get the book_id of the most borrowed book
                        mostBorrowedBookId = resultSet.getString("book_id");
                    }
                }
            }

            // If a most borrowed book ID is found, retrieve its name
            if (mostBorrowedBookId != null) {
                // Create a PreparedStatement to execute the SQL query to retrieve the book name
                String bookNameQuery = "SELECT book_name " +
                        "FROM tblBook " +
                        "WHERE book_id = ?";
                try (PreparedStatement nameStatement = connection.prepareStatement(bookNameQuery)) {
                    // Set the book ID parameter in the SQL query
                    nameStatement.setString(1, mostBorrowedBookId);

                    // Execute the query to retrieve the book name
                    try (ResultSet nameResultSet = nameStatement.executeQuery()) {
                        // Check if there are any results
                        if (nameResultSet.next()) {
                            // Get the book name of the most borrowed book
                            mostBorrowedBookName = nameResultSet.getString("book_name");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your application's requirements
        }

        // Return the book name of the most borrowed book
        return mostBorrowedBookName;
    }

    public int uniqueStudents(Date date){
        int uniqueStudentsCount = 0;

        // Connect to the database
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Create a PreparedStatement to execute the SQL query
            String query = "SELECT COUNT(DISTINCT u_id) AS unique_students_count " +
                    "FROM tblLoan " +
                    "WHERE TO_CHAR(date_borrowed, 'YYYY-MM') = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Set the parameter for the month in the SQL query
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String monthYear = sdf.format(date);
                statement.setString(1, monthYear);

                // Execute the query
                try (ResultSet resultSet = statement.executeQuery()) {
                    // Check if there are any results
                    if (resultSet.next()) {
                        // Get the count of unique students borrowing books in the month
                        uniqueStudentsCount = resultSet.getInt("unique_students_count");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your application's requirements
        }

        // Return the count of unique students borrowing books in the month
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
                String queryBookName = "SELECT book_name FROM tblBook WHERE book_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(queryBookName)) {
                    statement.setString(1, mostBorrowedBookId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            mostBorrowedBookName = resultSet.getString("book_name");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your application's requirements
        }

        // Return the name of the most borrowed book
        return mostBorrowedBookName;
    }

    public String bookMostBorrowedInACourse(String course){
        String mostBorrowedBookName = null;
        int courseId = getCourseId(course);

        // Connect to the database
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Create a PreparedStatement to execute the SQL query
            String query = "SELECT tblBook.book_name " +
                    "FROM tblLoan " +
                    "INNER JOIN tblBook ON tblLoan.book_id = tblBook.book_id " +
                    "WHERE EXISTS (SELECT 1 FROM tblBook WHERE tblBook.book_id = tblLoan.book_id AND tblBook.course_id = ?) " +
                    "GROUP BY tblBook.book_id, tblBook.book_name " +
                    "ORDER BY COUNT(*) DESC " +
                    "FETCH FIRST 1 ROWS ONLY";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Set the parameter for the course in the SQL query
                statement.setInt(1, courseId);

                // Execute the query
                try (ResultSet resultSet = statement.executeQuery()) {
                    // Check if there are any results
                    if (resultSet.next()) {
                        // Get the book name of the most borrowed book in the course
                        mostBorrowedBookName = resultSet.getString("book_name");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your application's requirements
        }

        // Return the name of the most borrowed book in the course
        return mostBorrowedBookName;
    }

    public double percentageReturnedLate(){
        // Query to count the total number of books borrowed
        String totalBorrowedQuery = "SELECT COUNT(*) FROM tblLoan";

        // Query to count the number of books returned late
        String returnedLateQuery = "SELECT COUNT(*) FROM tblLoan l1 " +
                "INNER JOIN tblLoan l2 ON l1.loan_id = l2.loan_id " +
                "WHERE l2.date_returned - l1.date_borrowed > 7";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = conn.createStatement()) {
            // Execute the query to count the total number of books borrowed
            try (ResultSet totalBorrowedResult = statement.executeQuery(totalBorrowedQuery)) {
                if (totalBorrowedResult.next()) {
                    int totalBorrowed = totalBorrowedResult.getInt(1);

                    // Execute the query to count the number of books returned late
                    try (ResultSet returnedLateResult = statement.executeQuery(returnedLateQuery)) {
                        if (returnedLateResult.next()) {
                            int returnedLate = returnedLateResult.getInt(1);

                            // Calculate the percentage of books returned late
                            if (totalBorrowed > 0) {
                                return ((double) returnedLate / totalBorrowed) * 100;
                            } else {
                                return 0.0; // If no books were borrowed, return 0%
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return 0 if there's any error
        return 0.0;
    }

    public int closeOutOfStock(Date date, String bookName){
        String query = "SELECT COUNT(*) AS borrowed_copies, " +
                "tblBook.stock AS total_stock " +
                "FROM tblLoan " +
                "INNER JOIN tblBook ON tblLoan.book_id = tblBook.book_id " +
                "WHERE TO_CHAR(date_borrowed, 'YYYY-MM') = ? " +
                "AND tblBook.book_name = ? " +
                "GROUP BY tblBook.stock";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = conn.prepareStatement(query)) {
            // Set the parameters for the book name and month in the SQL query
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String monthYear = sdf.format(date);
            statement.setString(1, monthYear);
            statement.setString(2, bookName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Get the number of copies borrowed and the total stock
                    int borrowedCopies = resultSet.getInt("borrowed_copies");
                    int totalStock = resultSet.getInt("total_stock");

                    // If no copies are borrowed, return the total stock
                    if (borrowedCopies == 0) {
                        return totalStock;
                    }

                    // Calculate how close the borrowed copies are to the total stock
                    // and return the number of copies away from being out of stock
                    return totalStock - borrowedCopies;
                } else {
                    // No loans found, return total stock
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
                        // Handle the exception according to your application's requirements
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your application's requirements
        }

// Return -1 if any error occurs
        return -1;
    }



    public int numberBorrowed(Date date) {
        String query = "SELECT COUNT(DISTINCT tblLoan.book_id) AS borrowed_books " +
                "FROM tblLoan " +
                "INNER JOIN tblBook ON tblLoan.book_id = tblBook.book_id " +
                "WHERE TO_CHAR(date_borrowed, 'YYYY-MM') = ? ";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = conn.prepareStatement(query)) {
            // Set the parameters for the book name and month in the SQL query
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String monthYear = sdf.format(date);
            statement.setString(1, monthYear);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Get the number of books borrowed
                    int borrowedBooks = resultSet.getInt("borrowed_books");

                    // Return the number of books borrowed
                    return borrowedBooks;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Return 0 if there's any error
        return 0;
    }

    public double bookTypeTakenOut(String type){
        // Query to calculate the percentage of loans that include the specified type of book
        String query = "SELECT (COUNT(CASE WHEN tb.book_type = ? THEN 1 END) / COUNT(*)) * 100 AS percentage " +
                "FROM tblLoan tl INNER JOIN tblBook tb ON tl.book_id = tb.book_id";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = conn.prepareStatement(query)) {
            // Set the parameter for the book type in the SQL query
            statement.setString(1, type);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Get the percentage of loans that include the specified type of book
                    double percentage = resultSet.getDouble("percentage");
                    return percentage;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return 0.0 if any error occurs
        return 0.0;
    }

    public int loansPerCourseInMonth(String course, Date date){

        int courseId = getCourseId(course);

        // Query to calculate the total loans for the specified course in the given month
        String query = "SELECT COUNT(*) AS total_loans " +
                "FROM tblLoan tl INNER JOIN tblBook tb ON tl.book_id = tb.book_id " +
                "WHERE tb.course_id = ? AND TO_CHAR(tl.date_borrowed, 'YYYY-MM') = TO_CHAR(?, 'YYYY-MM')";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = conn.prepareStatement(query)) {
            // Set the parameters for the course and date in the SQL query
            statement.setInt(1, courseId);
            statement.setDate(2, new java.sql.Date(date.getTime()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Get the total loans for the specified course in the given month
                    int totalLoans = resultSet.getInt("total_loans");
                    return totalLoans;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return 0 if there's any error
        return 0;
    }

    public double finesPerPaymentMethod(String paymentMethod){
        double percentage = 0;
        String query = "SELECT SUM(fine_amount) AS total_fines FROM tblFine WHERE payment_type = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, paymentMethod);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double finesForPaymentMethod = resultSet.getDouble("total_fines");

                    // Retrieve the total fines paid overall
                    double totalFinesOverall = getTotalFinesOverall();

                    // Calculate the percentage of fines paid by the specified payment method
                    if (totalFinesOverall > 0) {
                        percentage = (finesForPaymentMethod / totalFinesOverall) * 100;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your application's requirements
        }

        return percentage;
    }

    private double getTotalFinesOverall() throws SQLException {
        double totalFines = 0;
        String query = "SELECT SUM(fine_amount) AS total_fines FROM tblFine";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                totalFines = resultSet.getDouble("total_fines");
            }
        }

        return totalFines;
    }

    public int timeToFinePayment(){
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
            // Handle the exception according to your application's requirements
        }

        return averageDays;
    }

    public int finesPaidInMonth(Date date){
        int totalFines = 0;
        String query = "SELECT SUM(fine_amount) AS total_fines " +
                "FROM tblFine " +
                "WHERE EXTRACT(MONTH FROM date_paid) = EXTRACT(MONTH FROM TO_DATE(?, 'YYYY-MM')) " +
                "AND EXTRACT(YEAR FROM date_paid) = EXTRACT(YEAR FROM TO_DATE(?, 'YYYY-MM'))";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            // Set the parameter for the month and year in the SQL query
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
            // Handle the exception according to your application's requirements
        }

        return totalFines;
    }

    public int getCourseId(String course){
        // SQL query to retrieve the course_id based on course_name
        String query = "SELECT course_id FROM tblCourse WHERE course_name = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            // Set the parameter for the course_name in the SQL query
            statement.setString(1, course);

            // Execute the query
            try (ResultSet resultSet = statement.executeQuery()) {
                // Check if there is a result
                if (resultSet.next()) {
                    // Retrieve the course_id from the result set
                    return resultSet.getInt("course_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your application's requirements
        }
        return 0;
    }

}
