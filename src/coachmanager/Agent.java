package coachmanager;

import java.sql.*;

public class Agent {
    
    public int agent_id;
    private PreparedStatement stmt = null;
    private Connection conn = null;
    
    public String surname,names;
    public long ID;
    
    public Agent(int id, Connection conn)
    {
        this.agent_id = id;
        this.conn = conn;
    }
    
    public void reload()
    {
        try
        {
            String sql = "SELECT * FROM agents WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, agent_id);
            ResultSet res = stmt.executeQuery();
            res.next();
            this.surname = res.getString("sname");
            this.names = res.getString("names");
            this.ID = res.getLong("ID_NO");
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage()+": at Agent");
            System.exit(1);
        }        
        
    }
    
    
    
    
}
