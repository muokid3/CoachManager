package coachmanager;

import java.sql.*;


public class Login {
    
    private long idnum;
    private String pwd;
    private javax.swing.JFrame frame;
    
    public Login(javax.swing.JFrame f, String IDNum, String Password)
    {
        IDNum = IDNum.replace(";","");
        Password = Password.replace(";","");
        
        try
        {
            this.idnum = Long.parseLong(IDNum);
            this.pwd = Password;
            this.frame = f;
        }
        catch(NumberFormatException e)
        {
            javax.swing.JOptionPane.showMessageDialog(f, "Please enter data as required!", "Login Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void authenticate()
    {
        Connection conn = Connector.createConnection();
        StringBuilder buf = new StringBuilder();
        PreparedStatement stmt = null;
        
        pwd = Connector.hash(this.pwd);
        
        String sql = "SELECT * FROM agents WHERE ID_NO = ? AND password = ?";
        try
        {
           stmt = conn.prepareStatement(sql);
           stmt.setLong(1, idnum);
           stmt.setString(2, pwd);
           ResultSet res = stmt.executeQuery();
                      
           
           int rows = 0;
           if(res.last())
           {
               rows = res.getRow();
               res.beforeFirst();
           }
           
           if(rows == 0)
           {
               javax.swing.JOptionPane.showMessageDialog(this.frame, "That user doesn't exist. Please register", "Login Error", javax.swing.JOptionPane.ERROR_MESSAGE);
           }
           
           else if(res.next())
           {
               int agent_id = res.getInt(1);
               Agent ag = new Agent(agent_id,conn);
               new SessionController(ag);
               this.frame.setVisible(false);
               new Start().setVisible(true);
           }
           
           
               
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
            System.exit(1);
        }
             
        
    }
    
    
    
}
