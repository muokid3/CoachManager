package coachmanager;

import java.sql.*;
import java.util.*;
import java.awt.*;
import java.awt.print.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.exceptions.COSVisitorException;


public class DataCenter {
    
    private Connection conn;
    private PreparedStatement stmt = null;
    private javax.swing.JFrame frame = null;
    
    private String [] months = {
          "Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"  
        };
    
    
    public DataCenter(Connection conn, javax.swing.JFrame f)
    {
        this.conn = conn;
        this.frame = f;
    }
    
    public String getDateTime()
    {
        
     
        Calendar cald = Calendar.getInstance();
        String mon = months[cald.get(Calendar.MONTH)];
        String minute,am_pm;
        if(cald.get(Calendar.MINUTE)<10)
        {
            minute = "0"+cald.get(Calendar.MINUTE);
        }
        else
        {
            minute = ""+cald.get(Calendar.MINUTE);
        }
        
        if(cald.get(Calendar.AM_PM) == 0)
        {
            am_pm = "AM";
        }
        else
        {
            am_pm = "PM";
        }
        
        String tmd = cald.get(Calendar.DATE)+" "+mon+" "+cald.get(Calendar.YEAR)+"     "+cald.get(Calendar.HOUR)+":"+minute+" "+am_pm;
        
        return tmd;
    }
    
    private long getTimestamp(int tomorrow)
    {
        Calendar cald = Calendar.getInstance();
        
        if(tomorrow == 1) // 1; get timestamp for next day, 0; get for today 
        {
            cald.set(Calendar.DATE, cald.get(Calendar.DATE)+1);
        }
        cald.set(Calendar.HOUR,0);
        cald.set(Calendar.MINUTE,0);
        cald.set(Calendar.SECOND,0);
        cald.set(Calendar.MILLISECOND,0);
                
        java.util.Date dt = cald.getTime();
        
        long timestamp = dt.getTime();
        
        return timestamp;
        
    }
    
    public long getTodayTrans()
    {
        long today = this.getTimestamp(0);
        long tomorrow = this.getTimestamp(1);
        
        long num = 0;
        
        String sql = "SELECT * FROM transactions WHERE timestamp >= "+today+" AND timestamp < "+tomorrow+"";
        try
        {
            this.stmt = conn.prepareStatement(sql);
            ResultSet rs = this.stmt.executeQuery();
            num = (long) rs.getFetchSize();
            
        }
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null,e.getMessage(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        }
       
        return num;
        
    }
    
    public long getTodayTotal()
    {
        long today = this.getTimestamp(0);
        long tomorrow = this.getTimestamp(1);
        long total = 0;
        
        String sql = "SELECT * FROM transactions WHERE timestamp >= "+today+" AND timestamp < "+tomorrow+"";
        
        try
        {
            this.stmt = conn.prepareStatement(sql);
            ResultSet rs = this.stmt.executeQuery();
            while(rs.next())
            {
                total += rs.getInt("price");
            }
        }
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null,e.getMessage(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        }
        
        return total;
        
    }
    
    public boolean newTransaction(String name, String from, String to, String tm_d, String tm_h, String tm_m, String price, Session sess)
    {
        
        String sql = "INSERT INTO transactions(pass_name,pass_from,pass_to,travel_date,travel_hrs,travel_min,price,timestamp,agent_id) VALUES ("+name+","+from+","+to+","+tm_d+","+tm_h+","+tm_m+","+price+",?,?)";
        
        long timestamp = new java.util.Date().getTime();
        int agent_id = sess.agent_id;
        
        if(!this.checkDate(tm_d))
        {
            return false;
        }
        
        try
        {
            this.stmt = this.conn.prepareStatement(sql);
            this.stmt.setLong(1, timestamp);
            this.stmt.setInt(2, agent_id);
            this.stmt.executeUpdate();
        }
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null,e.getMessage(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        }
        
        this.frame.setVisible(false);
        
        return true;
                   
    }
    
    private boolean checkDate(String date)
    {
        final String DATE_FORMAT = "dd/mm/yyyy";
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        try
        {
            df.setLenient(false);
            df.parse(date);
            
            return true;
        }
        catch(ParseException e)
        {
            return false;
        }
        
    }
    
    public String getTodayreport()
    {
        long today = this.getTimestamp(0);
        long tomorrow = this.getTimestamp(1);
        
        return getReport(today,tomorrow);
    }
    
    public String getTimeReport(String from)
    {
        String fname = null;
        
        if(!this.checkDate(from))
        {
            JOptionPane.showMessageDialog(frame,"Please valid dates !","Fatal Error",JOptionPane.ERROR_MESSAGE);
            return fname;
        }
        
        long since = this.getTimestamp(from);
        long until = since + (3600*24);
        
        return this.getReport(since, until);
        
    }
    
    private String getReport(long since, long until)
    {
        
        String sql = "SELECT * FROM transactions WHERE timestamp >= "+since+" AND timestamp <= "+until+"";
        String fname = null;
        try
        {
            stmt = conn.prepareStatement(sql);
            ResultSet res = stmt.executeQuery();
            
            String dirname = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
            fname = dirname+java.io.File.pathSeparator+"report"+since+".xls";
            
            LinkedList<ArrayList<Object>> list = new LinkedList<>();
            
            ArrayList<Object> frow = new ArrayList<>();
            frow.add("Passenger Name");
            frow.add("Traveling From");
            frow.add("Destination");
            frow.add("Date of Travel");
            frow.add("Time of Travel");
            frow.add("Price");
            frow.add("Date of Transaction");
            frow.add("Agent Name");
            frow.add("Agent ID Number");
            
            list.add(frow);
            
            int passnum = 0, total_cash = 0;
            
            int row = 1;
            
            passnum = res.getFetchSize();
            
            while(res.next())
            {
                ArrayList<Object> array = new ArrayList<>();
                
                array.add(row++);
                array.add(res.getString("pass_name"));
                array.add(res.getString("pass_from"));
                array.add(res.getString("pass_to"));
                array.add(this.formatDate(res.getString("travel_date")));
                array.add(res.getString("travel_hrs")+res.getString("travel_mins")+"hrs");
                array.add("KSH "+res.getInt("price"));
                array.add(this.formatDate(res.getInt("timestamp")));
                Session sess = new Session(new Agent(res.getInt("agent_id"),this.conn));
                array.add(sess.surname+", "+sess.names);
                array.add(sess.ID);
                
                list.add(array);
                
                total_cash += res.getInt("price");
            }
            
            this.writeReport(fname, list, passnum, total_cash);
            
        }
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(frame,e.getMessage(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        }
        
        return fname;
    }
    
    private void writeReport(String file, LinkedList<ArrayList<Object>> data_list, int passnum, int total_cash)
    {
        File xls = new File(file);
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Report Sheet");
        int rownum = 0;
        
        for(ArrayList<Object> array:data_list)
        {
            HSSFRow row = sheet.createRow(rownum++);
            int cellnum = 0;
            for(Object item:array)
            {
                HSSFCell cell = row.createCell(cellnum++);
                
                if(item instanceof Double)
                    cell.setCellValue((double) item);
                else if(item instanceof String)
                    cell.setCellValue((String) item);               
                
            }
            
        }
        
        String ttpl = "Passengers";
        String ttcl = "Total Amount";
        rownum += 3;
        
        HSSFRow lsrow = sheet.createRow(rownum++);
        lsrow.createCell(0).setCellValue(ttpl);
        lsrow.createCell(1).setCellValue(passnum);
        lsrow = sheet.createRow(rownum);
        lsrow.createCell(0).setCellValue(ttcl);
        lsrow.createCell(1).setCellValue(total_cash);        
        
        try
        {
            xls.createNewFile();
            FileOutputStream fout = new FileOutputStream(xls);
            workbook.write(fout);
            fout.close();            
        }
        catch(IOException e)
        {
            JOptionPane.showMessageDialog(null,e.getMessage(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        }
        
        
    }
    
    
    public String generateReceipt(String name, String from, String to, String tm_d, String tm_h, String tm_m, String price, Session sess)
    {
        long now = new java.util.Date().getTime();
        String dirname = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
        String fname = null;
        
        String header = "Easy Coach Bus Services";
        String passname_t = "Passenger Name  : ";
        String passname_l = passname_t+name;
        String from_t = "Traveling From   :";
        String from_l = from_t+from;
        String to_t = "Destination      :";
        String to_l = to_t+to;
        String day_t = "Date of Travel  :";
        String day_l = day_t+tm_d;
        String time_t = "Time of Travel : ";
        String time_l = time_t+tm_h+tm_m+"hrs";
        String price_t = "Price     :";
        String price_l = price_t+price;
        String agent_t = "Attended by "+sess.surname+" "+sess.names+" "+ new java.util.Date().toString();
        
        try
        {
            
            fname = dirname+java.io.File.pathSeparator+"reciept"+now+".pdf";
            
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            PDFont font = PDType1Font.HELVETICA_BOLD;
            PDFont font2 = PDType1Font.HELVETICA;
            try (PDPageContentStream contentStream = new PDPageContentStream(document,page)) {
                contentStream.beginText();
                contentStream.setFont(font,12);
                contentStream.moveTextPositionByAmount(100, 700);
                contentStream.drawString(header);
                contentStream.moveTextPositionByAmount((100)-header.length(), 2);
                contentStream.drawString("Travel Receipt");
                contentStream.moveTextPositionByAmount("Travel Receipt".length() - 100 , 3);
                contentStream.drawString(passname_t);
                contentStream.setFont(font2, 12);
                contentStream.drawString(name);
                contentStream.setFont(font,12);
                contentStream.moveTextPositionByAmount(passname_l.length() - 100 , 2);
                contentStream.drawString(from_t);
                contentStream.setFont(font2,12);
                contentStream.drawString(from);
                contentStream.moveTextPositionByAmount(from_l.length() - 100 , 2);
                contentStream.setFont(font,12);
                contentStream.drawString(to_t);
                contentStream.setFont(font2,12);
                contentStream.drawString(to);
                contentStream.moveTextPositionByAmount(to_l.length() - 100 , 2);
                contentStream.setFont(font,12);
                contentStream.drawString(day_t);
                contentStream.setFont(font2, 12);
                contentStream.drawString(tm_d);
                contentStream.moveTextPositionByAmount(day_l.length() - 100 , 2);
                contentStream.setFont(font,12);
                contentStream.drawString(time_t);
                contentStream.setFont(font2,12);
                contentStream.drawString(tm_h+tm_m+"hrs");
                contentStream.moveTextPositionByAmount(time_l.length() - 100 , 2);
                contentStream.setFont(font,12);
                contentStream.drawString(price_t);
                contentStream.setFont(font2,12);
                contentStream.drawString(price);
                contentStream.moveTextPositionByAmount(price_l.length() - 100 , 2);
                contentStream.drawString(agent_t);
                
                
                contentStream.endText();
            }
            
            document.save(new File(fname));
            
        
        }
        catch(IOException e)
        {
            JOptionPane.showMessageDialog(null,e.getMessage(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        }
        catch(COSVisitorException e)
        {
            JOptionPane.showMessageDialog(null,e.getMessage(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        }
        
        
        return fname;
        
    }
    
    public void printObject(javax.swing.JFrame parent, String filename)
    {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.pageDialog(job.defaultPage());
        job.setPrintable(new PrintListingPainter(filename),pf);
        
        job.setCopies(1);
        
        try
        {
            job.print();
        }
        catch(PrinterException e)
        {
            JOptionPane.showMessageDialog(parent,e.getMessage(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        }
        
        
    }
    
    
    
    
    
    private String formatDate(long timestamp)
    {
        String output = "";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        
        output += cal.get(Calendar.DATE) + " " + months[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.YEAR);
        
        return output;        
    }
    
    
    private String formatDate(String date)
    {
        String output = "";
        String[] parts = Pattern.compile("/").split(date);
        
        int day = Integer.parseInt(parts[0]);
        int mon = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        mon--;
        
        output += day+" "+months[mon]+"   "+year;
        
        return output;
    }
    
    public long getTimestamp(String date)
    {
        String[] parts = Pattern.compile("/").split(date);
        
        int day = Integer.parseInt(parts[0]);
        int mon = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        mon--;
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, day);
        cal.set(Calendar.MONTH, mon);
        cal.set(Calendar.YEAR, year);
        
        return cal.getTimeInMillis();
        
    }
    
    
    
}


class PrintListingPainter implements Printable  
{ 
  private java.io.RandomAccessFile raf;    
  private String fileName;    
  private Font fnt = new Font("Helvetica", Font.PLAIN, 10); 
  private int rememberedPageIndex = -1;    
  private long rememberedFilePointer = -1;    
  private boolean rememberedEOF = false; 
   
  public PrintListingPainter(String file)  
  {  
    fileName = file;      
    try 
    {  
      // Open file       
      raf = new java.io.RandomAccessFile(file, "r");      
    }  
    catch (Exception e) { rememberedEOF = true; }    
  } 
 
  public int print(Graphics g, PageFormat pf, int pageIndex) 
  throws PrinterException  
  { 
  try  
  {  
    // For catching IOException      
    if (pageIndex != rememberedPageIndex)  
    {  
      // First time we've visited this page 
      rememberedPageIndex = pageIndex;   
      // If encountered EOF on previous page, done  
      if (rememberedEOF) return Printable.NO_SUCH_PAGE; 
      // Save current position in input file 
      rememberedFilePointer = raf.getFilePointer(); 
    }  
    else raf.seek(rememberedFilePointer); 
    g.setColor(Color.black);      
    g.setFont(fnt);  
        int x = (int) pf.getImageableX() + 10; 
        int y = (int) pf.getImageableY() + 12;     
    // Title line      
    g.drawString("File: " + fileName + ", page: " +                                          (pageIndex+1),  x, y); 
    // Generate as many lines as will fit in imageable area 
    y += 36; 
    while (y + 12 < pf.getImageableY()+pf.getImageableHeight()) 
    { 
      String line = raf.readLine(); 
      if (line == null) 
      {  
        rememberedEOF = true; 
        break;  
                } 
        g.drawString(line, x, y);  
        y += 12;      
      } 
      return Printable.PAGE_EXISTS;     
    }  
    catch (Exception e) { return Printable.NO_SUCH_PAGE;} 
  }  
} 