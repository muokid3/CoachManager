package coachmanager;

import java.sql.*;
import javax.swing.JOptionPane;


public class Register {
    public String surname,names,password1,password2,password,tmp_ID;
    public long ID;
    
    
    
    public Register(String sname, String names, String ID, String pass1, String pass2)
    {
        this.surname = sname;
        this.names = names;
        this.password1 = pass1;
        this.password2 = pass2;
        this.tmp_ID = ID;
    }
    
    public void register(RegPanel reg)
    {
        
        if(this.surname.trim().isEmpty())
        {
            JOptionPane.showMessageDialog(reg,"Surname required","Error",JOptionPane.ERROR_MESSAGE);
        }
        
                
        else if(this.names.trim().isEmpty())
        {
            JOptionPane.showMessageDialog(reg,"Names required","Error",JOptionPane.ERROR_MESSAGE);
        }
        
                
        else if(this.tmp_ID.trim().isEmpty())
        {
            JOptionPane.showMessageDialog(reg,"ID Number required","Error",JOptionPane.ERROR_MESSAGE);
        }
        
        
        else if(this.password1.trim().isEmpty())
        {
            JOptionPane.showMessageDialog(reg,"Password required","Error",JOptionPane.ERROR_MESSAGE);
        }
        
                
        else if(this.password2.trim().isEmpty())
        {
            JOptionPane.showMessageDialog(reg,"Confirmation Password required","Error",JOptionPane.ERROR_MESSAGE);
        }
        
        else if(!this.password1.trim().equals(this.password2))
        {
            JOptionPane.showMessageDialog(reg,"Unmatching Passwords","Error",JOptionPane.ERROR_MESSAGE);
        }
        
        else
        {
            this.password = Connector.hash(this.password1);
                        
            
            Connection conn = Connector.createConnection();
            PreparedStatement stmt;           
            
            
            int agent_id;
            
            try
            {
                this.ID = Long.parseLong(this.tmp_ID);
                
                String sql = "INSERT INTO agents (ID_NO,sname,names,password) VALUES (?,?,?,?)";
                stmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
                stmt.setLong(1, this.ID);
                stmt.setString(2, this.surname);
                stmt.setString(3, this.names);
                stmt.setString(4, this.password);
                stmt.executeUpdate();
                
                ResultSet res = stmt.getGeneratedKeys();
                
                if(res.next())
                {
                    agent_id = res.getInt(1);                
                    Agent ag = new Agent(agent_id,conn);             
                    new SessionController(ag);                
                    reg.setVisible(false);                
                    new Start().setVisible(true);
                    
                }
                else
                {
                    System.out.println("Error getting insert id");
                    System.exit(1);
                }
                
            }
            catch(SQLException e)
            {
                JOptionPane.showMessageDialog(reg,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            } 
            catch(NumberFormatException e)
            {
                JOptionPane.showMessageDialog(reg,"Please enter the ID Number in correct format","Error",JOptionPane.ERROR_MESSAGE);
            }
            
            
        }       
         
    }
    
    
    
    
}
