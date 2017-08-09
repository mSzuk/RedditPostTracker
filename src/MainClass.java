import java.io.IOException;
import javax.swing.SwingUtilities;


public class MainClass {
	public static void main(String[] args) throws IOException{	
		SwingUtilities.invokeLater(
			new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					WindowClass mainFrame = new WindowClass();
					@SuppressWarnings("unused")
					TaskManager taskThread = new TaskManager(mainFrame, 10);
				}
			} );		
	}
}
