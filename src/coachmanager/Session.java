
package coachmanager;

import java.io.Serializable;

public class Session implements Serializable
{
    public Session(Agent a)
    {
        a.reload();
        
        this.ID = a.ID;
        this.surname = a.surname;
        this.names = a.names;
        this.agent_id = a.agent_id;
    }
    
    public Session()
    {
        
    }
    
    public long ID;
    public String surname;
    public String names;
    public int agent_id;
}
