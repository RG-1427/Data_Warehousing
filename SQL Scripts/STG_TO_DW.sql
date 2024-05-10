-- Create database link between the staging database and datawarehouse
CREATE PUBLIC DATABASE LINK s4109300_STG_DW
CONNECT TO s4109300_STG IDENTIFIED BY "s4109300_STG!"
USING '(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=localhost)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=orclpdb.chelt.local)))';

-- Load tblCourse into data warehouse
INSERT INTO tblCourse
SELECT * FROM stg_tblCourse@s4109300_STG_DW stg_course
WHERE NOT EXISTS (
    SELECT 1 FROM tblCourse d
    WHERE d.course_id = stg_course.course_id
);

-- Load tblUser into data warehouse
INSERT INTO tblUser
SELECT *
FROM stg_tblUser@s4109300_STG_DW stg_user
WHERE NOT EXISTS (
    SELECT 1
    FROM tblUser d
    WHERE d.u_id = stg_user.u_id
);

-- Load tblBook into data warehouse
INSERT INTO tblBook
SELECT * FROM stg_tblBook@s4109300_STG_DW stg_book
WHERE NOT EXISTS (
    SELECT 1 FROM tblBook d
    WHERE d.book_id = stg_book.book_id
);

-- Update stock if book ID exists but stock is different
UPDATE tblBook
SET stock = (
    SELECT stg_book.stock
    FROM stg_tblBook@s4109300_STG_DW stg_book
    WHERE tblBook.book_id = stg_book.book_id
)
WHERE EXISTS (
    SELECT 1
    FROM stg_tblBook@s4109300_STG_DW stg_book
    WHERE tblBook.book_id = stg_book.book_id
    AND tblBook.stock <> stg_book.stock
);

-- Insert loans from staging to data warehouse if they do not already exist
INSERT INTO tblLoan (loan_id, book_id, u_id, fine_id, date_borrowed, date_returned)
SELECT s.loan_id, s.book_id, s.u_id, NULL, s.date_borrowed, s.date_returned
FROM stg_tblLoan@s4109300_STG_DW s
WHERE NOT EXISTS (
    SELECT 1
    FROM tblLoan d
    WHERE d.loan_id = s.loan_id
);

-- Update return date if loan exists and new data has a return date
UPDATE tblLoan
SET date_returned = (
    SELECT stg_loan.date_returned
    FROM stg_tblLoan@s4109300_STG_DW stg_loan
    WHERE tblLoan.loan_id = stg_loan.loan_id
)
WHERE EXISTS (
    SELECT 1
    FROM stg_tblLoan@s4109300_STG_DW stg_loan
    WHERE tblLoan.loan_id = stg_loan.loan_id
    AND tblLoan.date_returned IS NULL
    AND stg_loan.date_returned IS NOT NULL
);

-- If there is a fine, insert it into tblFine and update loan with fine id
DROP SEQUENCE fines;

CREATE SEQUENCE fines START WITH 1 INCREMENT BY 1;
INSERT INTO tblFine (fine_id, fine_amount, date_paid, payment_type)
SELECT fines.nextval, f.fine_amount, f.date_paid, f.payment_type
FROM (
    SELECT loan_id, fine_amount, date_paid, payment_type
    FROM stg_tblLoan@s4109300_STG_DW
    WHERE fine_amount > 0
) f
WHERE NOT EXISTS (
    SELECT 1
    FROM tblFine
    WHERE tblFine.fine_amount = f.fine_amount
);

-- Update loan with fine id
UPDATE tblLoan
SET fine_id = (
    SELECT tf.fine_id
    FROM (
        SELECT tf.fine_id
        FROM tblFine tf
        JOIN stg_tblLoan@s4109300_STG_DW stg_loan ON tf.fine_amount = stg_loan.fine_amount
        WHERE tblLoan.loan_id = stg_loan.loan_id
        AND tf.date_paid = stg_loan.date_paid
        AND tf.payment_type = stg_loan.payment_type
        AND ROWNUM = 1
    ) tf
)
WHERE EXISTS (
    SELECT 1
    FROM stg_tblLoan@s4109300_STG_DW stg_loan
    WHERE tblLoan.loan_id = stg_loan.loan_id
    AND stg_loan.fine_amount > 0
    AND tblLoan.fine_id IS NULL
);

-- Handling fines that are paid in opperational but not in data warehouse
UPDATE tblFine
SET date_paid = (
    SELECT stg_loan.date_paid
    FROM stg_tblLoan@s4109300_STG_DW stg_loan
    JOIN tblLoan d ON tblFine.fine_id = d.fine_id AND d.loan_id = stg_loan.loan_id
    WHERE stg_loan.date_paid IS NOT NULL
),
payment_type = (
    SELECT stg_loan.payment_type
    FROM stg_tblLoan@s4109300_STG_DW stg_loan
    JOIN tblLoan d ON tblFine.fine_id = d.fine_id AND d.loan_id = stg_loan.loan_id
    WHERE stg_loan.payment_type IS NOT NULL
)
WHERE EXISTS (
    SELECT 1
    FROM stg_tblLoan@s4109300_STG_DW stg_loan
    JOIN tblLoan d ON tblFine.fine_id = d.fine_id AND d.loan_id = stg_loan.loan_id
    WHERE (tblFine.date_paid IS NULL AND stg_loan.date_paid IS NOT NULL) OR (tblFine.date_paid <> stg_loan.date_paid)
       OR (tblFine.payment_type IS NULL AND stg_loan.payment_type IS NOT NULL) OR (tblFine.payment_type <> stg_loan.payment_type)
);