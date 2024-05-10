-- stg_tblUser
  
-- Dropping a user table if it exists
DROP TABLE stg_tblUser CASCADE CONSTRAINTS;

-- Creating a user table to store the needed values, all of which cannot be null   
CREATE TABLE stg_tblUser
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
ALTER TABLE stg_tblUser
ADD CONSTRAINT pk_stg_tblUser PRIMARY KEY (u_id);

-- stg_tblBook

-- Dropping the book table if it exists
DROP TABLE stg_tblBook CASCADE CONSTRAINTS;

--Creating the book table
CREATE TABLE stg_tblBook
( book_id varchar2(10) NOT NULL,
  course_id varchar2(10) NOT NULL, 
  book_name varchar2(75) NOT NULL,  
  book_type varchar2(15) NOT NULL,
  stock varchar2(12) NOT NULL
);

--Partition by type?

--Setting the primary key to the book id
ALTER TABLE stg_tblBook
ADD CONSTRAINT pk_stg_tblBook PRIMARY KEY (book_id);

-- stg_tblCourse

--Dropping the course table if it exists
DROP TABLE stg_tblCourse CASCADE CONSTRAINTS;

--Creating the coruse table
CREATE TABLE stg_tblCourse
( course_id varchar2(10) NOT NULL,
  course_name varchar2(50) NOT NULL,
  course_leader varchar2(50) NOT NULL
);

-- Setting the primary key to course id
ALTER TABLE stg_tblCourse
ADD CONSTRAINT pk_stg_tblCourse PRIMARY KEY (course_id);

-- stg_tblLoan

-- Dropping the loan table if it exists
DROP TABLE stg_tblLoan CASCADE CONSTRAINTS;

--Creating the loan table
CREATE TABLE stg_tblLoan
( loan_id number(10) NOT NULL,
  book_id varchar2(10) NOT NULL,
  u_id varchar2(10) NOT NULL,
  date_borrowed date NOT NULL, 
  date_returned date, 
  fine_amount varchar2(10) NOT NULL,
  date_paid date,
  payment_type varchar2(10) 
);

--Partition by date_borrowed

--Setting the primary key to loan id
ALTER TABLE stg_tblLoan
ADD CONSTRAINT pk_stg_tblLoan PRIMARY KEY (loan_id);

  
--  Add foreign keys

-- Setting the book table's foreign key to coruse id
ALTER TABLE stg_tblBook
ADD CONSTRAINT fk_stg_tblCourse_stg_tblBook
  FOREIGN KEY (course_id)
  REFERENCES stg_tblCourse(course_id);

--Setting the loan table's foreign keys to book id and user id  
ALTER TABLE stg_tblLoan
ADD CONSTRAINT fk_stg_tblBook_stg_tblLoan
  FOREIGN KEY (book_id)
  REFERENCES stg_tblBook(book_id);  
ALTER TABLE stg_tblLoan
ADD CONSTRAINT fk_stg_tblUser_stg_tblLoan
  FOREIGN KEY (u_id)
  REFERENCES stg_tblUser(u_id);
 
-- Setting the user table's foreign key to course id
ALTER TABLE stg_tblUser
ADD CONSTRAINT fk_stg_tblCourse_stg_tblUser
  FOREIGN KEY (course_id)
  REFERENCES stg_tblCourse(course_id);