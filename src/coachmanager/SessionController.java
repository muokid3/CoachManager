package coachmanager;

import java.io.*;



public class SessionController {
    
    Agent ag;
    
    private final String sess_file = "sessdat.dat";
    private Session session;
    
    public SessionController(Agent a)
    {
        this.session = new Session(a);
        this.createSessfile();
        
    }
    
    public SessionController()
    {
        
    }
    
    private void createSessfile()
    {
        ObjectOutput oout;
        OutputStream out;
        
        
        try
        {
            
            File fl = new File(this.sess_file);
            if(fl.exists())
            {
                fl.delete();
            }
           
            fl.createNewFile();
            fl.deleteOnExit();            
            
            out = new FileOutputStream(this.sess_file);
            OutputStream buffer = new BufferedOutputStream(out);
            oout = new ObjectOutputStream(buffer);                        
            
                       
           oout.writeObject(session);    
           oout.flush();
           oout.close();
            
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage()+" : SessionController.java");
            System.exit(1);
        }
    }
    
    public Session readSession()
    {
        InputStream fin;
        InputStream buffer;
        ObjectInput oos;
        Session sess = null;
        
        try
        {            
            fin = new FileInputStream(this.sess_file);
            buffer = new BufferedInputStream(fin);
            oos = new ObjectInputStream(buffer);
            sess = (Session) oos.readObject();
            
            System.out.println(sess.surname);
                        
            oos.close();
        }
        catch(FileNotFoundException e)
        {
            javax.swing.JOptionPane.showMessageDialog(null, " Fatal Error: Session unregistered!\nSystem Aborting", "Session Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            
            System.exit(1);
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage()+" :IOExcept in SessionController");
            System.exit(1);
        }
        catch(ClassNotFoundException e)
        {
            System.out.println(e.getMessage()+" :ClassNotFoundExcept in SessionController");
            System.exit(1);
        }
        
        return sess;
        
    }
    
    public static void Logout(javax.swing.JFrame frame)
    {
        int choice = javax.swing.JOptionPane.showConfirmDialog(frame, "Are you sure you want to Log Off?", "Log Off", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);
        
        if(choice == javax.swing.JOptionPane.YES_OPTION)
        {
            File f = new File("sessdat.dat");
            f.delete();
            System.exit(0);
        }
                
    }
    
    
}
