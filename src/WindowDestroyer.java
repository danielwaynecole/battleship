import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WindowDestroyer extends WindowAdapter
{
    public void windowClosing(WindowEvent e) 
    {
    	//Main.vMain.shutDown();
        System.exit(0);
    }
}


