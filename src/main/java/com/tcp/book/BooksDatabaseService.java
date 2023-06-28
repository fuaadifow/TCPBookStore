/*
 * BooksDatabaseService.java
 *
 * The service threads for the books database server.
 * This class implements the database access service, i.e. opens a JDBC connection
 * to the database, makes and retrieves the query, and sends back the result.
 *
 * author: <1926823>
 *
 */
package com.tcp.book;
import java.io.*;
//import java.io.OutputStreamWriter;

import java.net.Socket;

import java.util.StringTokenizer;

import java.sql.*;
import javax.sql.rowset.*;
//Direct import of the classes CachedRowSet and CachedRowSetImpl will fail becuase
//these clasess are not exported by the module. Instead, one needs to impor
//javax.sql.rowset.* as above.



public class BooksDatabaseService extends Thread{

    private Socket serviceSocket = null;
    private String[] requestStr  = new String[2]; //One slot for author's name and one for library's name.
    private ResultSet outcome   = null;

    //JDBC connection
    private String USERNAME = Credentials.USERNAME;
    private String PASSWORD = Credentials.PASSWORD;
    private String URL      = Credentials.URL;

    private ObjectOutputStream outStream = null;
    private ObjectInputStream outcomeStreamReader = null;
    private CachedRowSet crs = null;


    //Class constructor
    public BooksDatabaseService(Socket aSocket) throws IOException {

        //TO BE COMPLETED
        this.serviceSocket = aSocket;
        outStream = new ObjectOutputStream (serviceSocket.getOutputStream() );
        outStream.flush(); // Flush directly after creating to avoid
        outcomeStreamReader = new ObjectInputStream (serviceSocket.getInputStream() );
    }


    //Retrieve the request from the socket
    public String[] retrieveRequest()
    {
        this.requestStr[0] = ""; //For author
        this.requestStr[1] = ""; //For library

        String tmp = "";
        try {
            //TO BE COMPLETED
            String message = (String)outcomeStreamReader.readObject();
            message = message.replaceAll("#","");
            this.requestStr = message.split(";");
            System.out.println(message);
        }catch(IOException e){
            System.out.println("Service thread " + this.getId() + ": " + e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }
        return this.requestStr;
    }


    //Parse the request command and execute the query
    public boolean attendRequest()
    {
        boolean flagRequestAttended = true;
        this.outcome = null;
        String sql = "Select b.title, b.publisher,b.genre,b.rrp, count(bc.bookid)  as noOfCopies\n" +
                "FROM book b, author a, library l, bookcopy bc\n" +
                "WHERE a.familyname = '"+this.requestStr[0]+"'\n" +
                "AND l.city = '"+this.requestStr[1]+"'\n" +
                "AND b.authorid = a.authorid\n" +
                "AND bc.libraryid = l.libraryid\n" +
                "AND bc.bookid = b.bookid\n" +
                "group by b.title, b.publisher,b.genre,b.rrp"; //TO BE COMPLETED- Update this line as needed.
        try {
            //Connet to the database
            //TO BE COMPLETED
            Connection conn = null;
            Statement stmt = null;
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(this.URL,this.USERNAME,this.PASSWORD);
            //Make the query
            //TO BE COMPLETED
            RowSetFactory aFactory = RowSetProvider.newFactory();
            crs = aFactory.createCachedRowSet();

            //Process query
            //TO BE COMPLETED -  Watch out! You may need to reset the iterator of the row set.
            stmt = conn.createStatement();
            this.outcome = stmt.executeQuery(sql);
            crs.populate(this.outcome);
            //Clean up
            //TO BE COMPLETED
            this.outcome.close();
            stmt.close();
            conn.close();
        } catch (Exception e)
        { System.out.println(e); }

        return flagRequestAttended;
    }



    //Wrap and return service outcome
    public void returnServiceOutcome(){
        try {
            //Return outcome
            //TO BE COMPLETED
            outStream.writeObject(this.crs);


            while(this.crs.next()){
                String title = this.crs.getString("title");
                String publisher = this.crs.getString("publisher");
                String genre = this.crs.getString("genre");
                double rrp = this.crs.getDouble("rrp");
                int noOfCopies = this.crs.getInt("noOfCopies");
                System.out.println(title +" | " +publisher+" | " + genre+" | " +rrp+" | " +noOfCopies+" | ");
            }
            //Terminating connection of the service socket
            //TO BE COMPLETED
            System.out.println("Service thread " + this.getId() + ": Service outcome returned; " + this.outcome);
            outStream.close();
            serviceSocket.close();

        }catch (IOException e){
            System.out.println("Service thread " + this.getId() + ": " + e);
        }catch (SQLException e){
            System.out.println("Service thread " + this.getId() + ": " + e);
        }
    }


    //The service thread run() method
    public void run()
    {
        try {
            System.out.println("\n============================================\n");
            //Retrieve the service request from the socket
            this.retrieveRequest();
            System.out.println("Service thread " + this.getId() + ": Request retrieved: "
                    + "author->" + this.requestStr[0] + "; library->" + this.requestStr[1]);

            //Attend the request
            boolean tmp = this.attendRequest();

            //Send back the outcome of the request
            if (!tmp)
                System.out.println("Service thread " + this.getId() + ": Unable to provide service.");
            this.returnServiceOutcome();

        }catch (Exception e){
            System.out.println("Service thread " + this.getId() + ": " + e);
        }
        //Terminate service thread (by exiting run() method)
        System.out.println("Service thread " + this.getId() + ": Finished service.");
    }

}
