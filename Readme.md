Final project for CS166 at University of Califonia, Riverside.

PLEASE NOTE: PostgreSQL needs to be installed on the machine to run this program

Contributers:
Anthony Mejia
Jonathan Quach

Installing and Running

1. Start server, create database and shutdown server
 
  		source postgresql/startPostgreSQL.sh
  		source postgresql/createPostgreDB.sh

2. Run the application

		cd java/
		source compile.sh
		source run.sh <dbname> <port> <user>
		example: run.sh flightDB 5432 user

3. Shutdown the PostgreSQL server

		cd ../
		source postgresql/stopPostgreDB.sh
		NOTE: Make sure to stop the server when done with the application
