-- tblUser
  
-- Dropping a user table if it exists
DROP TABLE tblUser CASCADE CONSTRAINTS;

-- Creating a user table to store the needed values, all of which cannot be null   
CREATE TABLE tblUser
( u_id varchar2(10) NOT NULL,
  course_id varchar2(10) NOT NULL, 
  pwd varchar2(25) NOT NULL, 
  first_name varchar2(25) NOT NULL, 
  last_name varchar2(25) NOT NULL, 
  email varchar2(50) NOT NULL,
  role varchar2(50) NOT NULL,
  locked date
);

-- Setting the primary key to user id
ALTER TABLE tblUser
ADD CONSTRAINT pk_tblUser PRIMARY KEY (u_id);

-- tblBook

-- Dropping the book table if it exists
DROP TABLE tblBook CASCADE CONSTRAINTS;

--Creating the book table
CREATE TABLE tblBook
( book_id varchar2(10) NOT NULL,
  course_id varchar2(10) NOT NULL, 
  book_name varchar2(75) NOT NULL,  
  book_type varchar2(15) NOT NULL,
  stock varchar2(12) NOT NULL
);
--Partition by type

--Setting the primary key to the book id
ALTER TABLE tblBook
ADD CONSTRAINT pk_tblBook PRIMARY KEY (book_id);

-- tblCourse

--Dropping the course table if it exists
DROP TABLE tblCourse CASCADE CONSTRAINTS;

--Creating the coruse table
CREATE TABLE tblCourse
( course_id varchar2(10) NOT NULL,
  course_name varchar2(50) NOT NULL,
  course_leader varchar2(50) NOT NULL
);

-- Setting the primary key to course id
ALTER TABLE tblCourse
ADD CONSTRAINT pk_tblCourse PRIMARY KEY (course_id);

-- Fine Table

--Dropping the fine table if it exists
DROP TABLE tblFine CASCADE CONSTRAINTS;

--Creating the fine table
CREATE TABLE tblFine
( fine_id varchar2(10) NOT NULL,
  fine_amount varchar2(10) NOT NULL,
  date_paid date,
  payment_type varchar2(10) 
);

--Partition by Date Paid?

-- Setting the primary key to fine id
ALTER TABLE tblFine
ADD CONSTRAINT pk_tblFine PRIMARY KEY (fine_id);

  
-- tblLoan

-- Dropping the loan table if it exists
DROP TABLE tblLoan CASCADE CONSTRAINTS;

--Creating the loan table
CREATE TABLE tblLoan
( loan_id number(10) NOT NULL,
  book_id varchar2(10) NOT NULL,
  u_id varchar2(10) NOT NULL,
  fine_id varchar2(10),
  date_borrowed date NOT NULL, 
  date_returned date 
);

--Partition by date_borrowed?

--Setting the primary key to loan id
ALTER TABLE tblLoan
ADD CONSTRAINT pk_tblLoan PRIMARY KEY (loan_id);

--  Add foreign keys

-- Setting the book table's foreign key to coruse id
ALTER TABLE tblBook
ADD CONSTRAINT fk_tblCourse_tblBook
  FOREIGN KEY (course_id)
  REFERENCES tblCourse(course_id);

--Setting the loan table's foreign keys to book id, user id and fine id
ALTER TABLE tblLoan
ADD CONSTRAINT fk_tblBook_tblLoan
  FOREIGN KEY (book_id)
  REFERENCES tblBook(book_id);  
ALTER TABLE tblLoan
ADD CONSTRAINT fk_tblUser_tblLoan
  FOREIGN KEY (u_id)
  REFERENCES tblUser(u_id);
ALTER TABLE tblLoan
ADD CONSTRAINT fk_tblFine_tblLoan
  FOREIGN KEY (fine_id)
  REFERENCES tblFine(fine_id);
 
-- Setting the user table's foreign key to course id
ALTER TABLE tblUser
ADD CONSTRAINT fk_tblCourse_tblUser
  FOREIGN KEY (course_id)
  REFERENCES tblCourse(course_id);
