package coachmanager;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



public class Connector {
    
    public static String HOST = "jdbc:mysql://localhost/";
    public static String USR = "root";
    public static String PWD = "";
    
    public static Connection createConnection()
    {
        Connection conn = null;
        Statement stmt = null;
        
        try
        {
            //load mysql driver
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(HOST, USR, PWD);
            stmt = conn.createStatement();
            
            String sql = "CREATE DATABASE IF NOT EXISTS easycoach_db";
            stmt.executeUpdate(sql);
            HOST = "jdbc:mysql://localhost/easycoach_db";
            conn = DriverManager.getConnection(HOST, USR, PWD);
            stmt = conn.createStatement();
            
            createTables(stmt);
            
                        
        }
        catch(SQLException e) //handle sql errors
        {
            System.out.println(e.getMessage());
        }
        catch(ClassNotFoundException e) //handle Class.forName()
        {
            System.out.println("Mysql DB not found");
        }
        catch(InstantiationException e)
        {
            System.out.println(e.getMessage());
        }
        catch(IllegalAccessException e)
        {
            System.out.println(e.getMessage());
        }
        
                
        return conn;
    }
    
    private static void createTables(Statement stmt)
    {
       String sql = "CREATE TABLE IF NOT EXISTS agents"
               + "(id INT(10) NOT NULL AUTO_INCREMENT PRIMARY KEY,"
               + "ID_NO INT(10) NOT NULL,"
               + "sname VARCHAR(50) NOT NULL,"
               + "names VARCHAR(100) NOT NULL,"
               + "password VARCHAR(200) NOT NULL)";
       
       try
       {
           stmt.executeUpdate(sql);
           
           sql = "CREATE TABLE IF NOT EXISTS transactions"
                   + "(id INT(10) NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                   + "pass_name VARCHAR(100) NOT NULL,"
                   + "pass_from VARCHAR(100) NOT NULL,"
                   + "pass_to VARCHAR(100) NOT NULL,"
                   + "travel_date VARCHAR(30) NOT NULL,"
                   + "travel_hrs INT(2) NOT NULL,"
                   + "travel_mins INT(2) NOT NULL,"
                   + "price INT(7) NOT NULL,"
                   + "timestamp INT(20) NOT NULL,"
                   + "agent_id INT(10) NOT NULL)";
           
           stmt.executeUpdate(sql);
           
       }
       catch(SQLException e)
       {
           System.out.println(e.getMessage());
       }
       
    }
    
    public static String hash(String key)
    {
        String salt = "thegreatpolarbear";
        key += salt;
        StringBuilder buf = new StringBuilder();
        
        
        try
        {
            MessageDigest md  = MessageDigest.getInstance("SHA-256");
            md.update(key.getBytes());
            
            byte byteData[] = md.digest();
            
            //convert bytes into hex
            
            
            for(int i = 0; i < byteData.length; ++i)
            {
                buf.append(Integer.toString((byteData[i] & 0xff) + 0x100,16).substring(1));
            }
           
        }
        catch(NoSuchAlgorithmException e)
        {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        
        String newPwd = buf.toString();
        
        return newPwd;
        
    }
               
}
