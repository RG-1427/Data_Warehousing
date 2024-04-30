import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

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

    public String[] getBookList(){
        List<String> bookList = new ArrayList<>();

        String query = "SELECT book_name FROM tblBook";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String bookName = rs.getString("book_name");
                bookList.add(bookName);
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
                String bookName = rs.getString("course_name");
                courseList.add(bookName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your application's requirements
        }

        return courseList.toArray(new String[0]);
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

        int courseId = 0;
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
                    courseId = resultSet.getInt("course_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your application's requirements
        }

        // Connect to the database
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Create a PreparedStatement to execute the SQL query
            query = "SELECT book_id " +
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
        int courseId = 0;
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
                    courseId = resultSet.getInt("course_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your application's requirements
        }

        // Connect to the database
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Create a PreparedStatement to execute the SQL query
            query = "SELECT tblBook.book_name " +
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

    public int percentageReturnedLate(){
        //HD QUERY 1
        return 0;
    }

    public int closeOutOfStock(){
        //HD QUERY 2
        return 0;
    }

    public int percentageBorrowed(){
        //VC QUERY 1
        return 0;
    }
    public int bookTypeTakenOut(){
        //VC QUERY 2
        return 0;
    }
    public int loansPerCourseInMonth(){
        //VC QUERY 3
        return 0;
    }

    public int finesPerPaymentMethod(){
        //IT QUERY 1
        return 0;
    }
    public int timeToFinePayment(){
        //IT QUERY 2
        return 0;
    }
    public int finesPaidInMonth(){
        //IT QUERY 3
        return 0;
    }

}
