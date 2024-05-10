-- Create database link between the opperational database and staging database
CREATE PUBLIC DATABASE LINK s4109300_OP_T_STG
CONNECT TO s4109300_OP IDENTIFIED BY "s4109300_OP!"
USING '(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=localhost)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=orclpdb.chelt.local)))';

-- Insert all data from operational database into the staging database
INSERT INTO stg_tblCourse
SELECT * FROM tblCourse@s4109300_OP_T_STG;

INSERT INTO stg_tblBook
SELECT * FROM tblBook@s4109300_OP_T_STG;

INSERT INTO stg_tblUser
SELECT * FROM tblUser@s4109300_OP_T_STG;

INSERT INTO stg_tblLoan
SELECT * FROM tblLoan@s4109300_OP_T_STG;

-- Clear staging tables after loading into data warehouse (AFTER THE PROCESS OF MOVING THE STAGING DATA INTO THE DATA WAREHOUSE IS DONE)
DELETE FROM stg_tblLoan;
DELETE FROM stg_tblUser;
DELETE FROM stg_tblBook;
DELETE FROM stg_tblCourse;