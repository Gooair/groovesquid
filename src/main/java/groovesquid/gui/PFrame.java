package groovesquid.gui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class PFrame extends JFrame implements MouseMotionListener, MouseListener {

    private Point start_drag;        
    private  Point start_loc;   
    private  Point precedent_loc;  
    private  int precedent_width;  
    private  int precedent_height;  
    Toolkit toolkit =  Toolkit.getDefaultToolkit ();   
    //private int minWidth = getSize().width;
    //private int minHeight = getSize().height;
    private int minWidth = 750;
    private int minHeight = 500;
    int cursorArea = 5;    
    Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();  
    private boolean dragging;

    private Rectangle maxBounds;

    public PFrame() {
        super();
        maxBounds = null;
        Init();  
    }
    
    @Override
    public Rectangle getMaximizedBounds() {
        return (maxBounds);
    }

    @Override
    public synchronized void setMaximizedBounds(Rectangle maxBounds) {
        this.maxBounds = maxBounds;
        super.setMaximizedBounds(maxBounds);
    }

    @Override
    public synchronized void setExtendedState(int state) {
        if (maxBounds == null
                && (state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            Insets screenInsets = getToolkit().getScreenInsets(getGraphicsConfiguration());
            Rectangle screenSize = getGraphicsConfiguration().getBounds();
            Rectangle maxBoundsNew = new Rectangle(screenInsets.left + screenSize.x,
                    screenInsets.top + screenSize.y,
                    screenSize.x + screenSize.width - screenInsets.right - screenInsets.left,
                    screenSize.y + screenSize.height - screenInsets.bottom - screenInsets.top);
            super.setMaximizedBounds(maxBoundsNew);
        }

        super.setExtendedState(state);
    }
     
    private void Init() {        
        addMouseMotionListener(this);        
        addMouseListener(this);        
    }    
  
    public static Point getScreenLocation(MouseEvent e, JFrame frame) {  
        Point cursor = e.getPoint();  
        Point view_location = frame.getLocationOnScreen();  
        return new Point((int) (view_location.getX() + cursor.getX()),  
                (int) (view_location.getY() + cursor.getY()));  
    }  
      
    @Override       
    public void mouseDragged(MouseEvent e) {        
        moveOrFullResizeFrame(e);        
    }        
       
    @Override       
    public void mouseMoved(MouseEvent e) {        
        Point cursorLocation = e.getPoint();           
        int xPos = cursorLocation.x;           
        int yPos = cursorLocation.y;        
                
        if(xPos >= cursorArea && xPos <= getWidth()-cursorArea && yPos >= getHeight()-cursorArea)        
            setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));        
        else if(xPos >= getWidth()-cursorArea && yPos >= cursorArea && yPos <= getHeight()-cursorArea)        
            setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));        
        else if(xPos <= cursorArea && yPos >= cursorArea && yPos <= getHeight()-cursorArea)        
            setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));        
        else if(xPos >= cursorArea && xPos <= getWidth()-cursorArea && yPos <= cursorArea)        
            setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));        
        else if(xPos <= cursorArea && yPos <= cursorArea)        
            setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));        
        else if(xPos >= getWidth() - cursorArea && yPos <= cursorArea)        
            setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));        
        else if(xPos >= getWidth()-cursorArea && yPos >= getHeight()-cursorArea)        
            setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));        
        else if(xPos <= cursorArea && yPos >= getHeight()-cursorArea)        
            setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));        
        else       
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }          
            
    @Override       
    public void mouseClicked(MouseEvent e) {        
        Object sourceObject=e.getSource();        
        if(sourceObject instanceof JPanel)        
        {        
            if (e.getClickCount() == 2)         
            {       
                if(getCursor().equals(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)))        
                    headerDoubleClickResize();        
            }        
        }        
    }       
            
    private void moveOrFullResizeFrame(MouseEvent e) {        
        Object sourceObject = e.getSource();
        Point current = getScreenLocation(e, this);     
        Point offset = new Point((int)current.getX()- (int)start_drag.getX(), (int)current.getY()- (int)start_drag.getY());     
             
        if(getCursor().equals(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))) { // && sourceObject instanceof JPanel
            setLocation((int) (start_loc.getX() + offset.getX()), (int) (start_loc.getY() + offset.getY()));         
        } else if(!getCursor().equals(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))) {           
            int oldLocationX = (int)getLocation().getX();        
            int oldLocationY = (int)getLocation().getY();        
            int newLocationX = (int) (this.start_loc.getX() + offset.getX());        
            int newLocationY = (int) (this.start_loc.getY() + offset.getY());           
            boolean N_Resize = getCursor().equals(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));        
            boolean NE_Resize = getCursor().equals(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));        
            boolean NW_Resize = getCursor().equals(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));        
            boolean E_Resize = getCursor().equals(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));        
            boolean W_Resize = getCursor().equals(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));        
            boolean S_Resize = getCursor().equals(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));        
            boolean SW_Resize = getCursor().equals(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));        
            boolean setLocation = false;        
            int newWidth = e.getX();        
            int newHeight = e.getY();        
                    
            if(NE_Resize)           
            {             
                newHeight = getHeight() - (newLocationY - oldLocationY);         
                newLocationX = (int)getLocation().getX();        
                setLocation = true;        
            }        
            else if(E_Resize)        
                newHeight = getHeight();         
            else if(S_Resize)        
                newWidth = getWidth();                      
            else if(N_Resize)        
            {           
                newLocationX = (int)getLocation().getX();        
                newWidth = getWidth();        
                newHeight = getHeight() - (newLocationY - oldLocationY);        
                setLocation = true;        
            }        
            else if(NW_Resize)        
            {        
                newWidth = getWidth() - (newLocationX - oldLocationX);        
                newHeight = getHeight() - (newLocationY - oldLocationY);        
                setLocation = true;        
            }           
            else if(NE_Resize)          
            {             
                newHeight = getHeight() - (newLocationY - oldLocationY);        
                newLocationX = (int)getLocation().getX();          
            }        
            else if(SW_Resize)        
            {           
                newWidth = getWidth() - (newLocationX - oldLocationX);        
                newLocationY = (int)getLocation().getY();                       
                setLocation =true;        
            }        
            if(W_Resize)        
            {           
                newWidth = getWidth() - (newLocationX - oldLocationX);        
                newLocationY = (int)getLocation().getY();           
                newHeight = getHeight();        
                setLocation =true;        
            }        
                                    
            if(newWidth >= (int)toolkit.getScreenSize().getWidth() || newWidth <= minWidth)        
            {        
                newLocationX = oldLocationX;
                newWidth = getWidth();
                //setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }        
                    
            if(newHeight >= (int)toolkit.getScreenSize().getHeight() - 30 || newHeight <= minHeight)        
            {        
                newLocationY = oldLocationY; 
                newHeight = getHeight();
                //setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }        
                    
            if(newWidth != getWidth() || newHeight != getHeight())        
            {        
                this.setSize(newWidth, newHeight);        
                            
                if(setLocation)        
                    this.setLocation(newLocationX, newLocationY);                 
            }        
        }        
    }        
             
    private void headerDoubleClickResize() {              
        if(getWidth() < screen.getWidth() || getHeight() < screen.getHeight())  
        {  
            this.setSize((int)screen.getWidth(),(int)screen.getHeight());    
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();        
            Dimension frameSize = this.getSize();       
            this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);   
              
        }  
        else     
        {     
            this.setSize(precedent_width, precedent_height);   
            this.setLocation(precedent_loc);  
        }     
    }        
         
    @Override       
    public void mousePressed(MouseEvent e) {        
        this.start_drag = getScreenLocation(e, this);        
        this.start_loc = this.getLocation();       
           
            
        if(getWidth() < screen.getWidth() || getHeight() < screen.getHeight())  
        {  
            precedent_loc = this.getLocation();   
            precedent_width = getWidth();  
            precedent_height = getHeight();  
        }  
    }        
            
    @Override       
    public void mouseEntered(MouseEvent e) {}        
       
    @Override       
    public void mouseExited(MouseEvent e) {}        
            
    @Override       
    public void mouseReleased(MouseEvent e) {}    
      

}