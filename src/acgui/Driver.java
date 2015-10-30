package acgui;

import java.lang.reflect.Field;
import java.util.Arrays;

import javax.swing.UIManager;

import org.sbml.libsbml.libsbml;

public class Driver {

	/**
	* Adds the specified path to the java library path
	*
	* @param pathToAdd the path to add
	* @throws Exception
	*/
	private static void addLibraryPath(String pathToAdd) throws Exception{
		final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
		usrPathsField.setAccessible(true);
	 
		//get array of paths
		final String[] paths = (String[])usrPathsField.get(null);
	 
		//check if the path to add is already present
		for(String path : paths) {
			if(path.equals(pathToAdd)) {
				return;
			}
		}
	 
		//add the new path
		final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
		newPaths[newPaths.length-1] = pathToAdd;
		usrPathsField.set(null, newPaths);
	}
	
	/**
	 * Create the GUI and show it.
	 */
	private static void createAndShowGUI() {
		/*
        //Create and set up the window.
        JFrame frame = new JFrame("HelloWorldSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add the ubiquitous "Hello World" label.
        JLabel label = new JLabel("Hello World");
        frame.getContentPane().add(label);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        */
		final AC_GUI currentGUI = new AC_GUI();
		//currentGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		currentGUI.setSize(900, 800);
		// make the frame full screen
		// currentGUI.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
	
	/**
	 * Start the tool.
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			addLibraryPath("..\\lib");
			addLibraryPath(".\\lib");
			System.loadLibrary("sbmlj");
			System.out.println("Using LibSBML: " + libsbml.getLibSBMLDottedVersion());
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

	}
}
