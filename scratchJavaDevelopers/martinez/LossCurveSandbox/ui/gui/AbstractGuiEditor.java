package scratchJavaDevelopers.martinez.LossCurveSandbox.ui.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.TreeMap;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import scratchJavaDevelopers.martinez.LossCurveSandbox.ui.BeanEditorAPI;

/**
 * This is the base class that defines the most basic methods that an GUI Editor
 * must implement. When implementing an editor, one should be careful to 
 * remember that the underlying bean for an instance of the editor may be shared
 * between several (or composite) editors. This being the case, one should
 * have implement the editor such that it listens to every property of its
 * underlying bean that it edits. By following this model, if the bean's
 * property is changed by an external object, the editor will be informed of the
 * change and can act appropriately.
 * 
 * @author <a href="mailto:emartinez@usgs.gov">Eric Martinez</a>
 *
 */
public abstract class AbstractGuiEditor implements Serializable, BeanEditorAPI,
		PropertyChangeListener {

	/**
	 * The size of the screen. Useful for sizing and placing application windows.
	 */
	public static Dimension screenSize = 
			Toolkit.getDefaultToolkit().getScreenSize();
	
	/**
	 * See the general contract declared in the <code>BeanEditorAPI</code>
	 * interface.
	 */
	public boolean boolPrompt(String question, boolean suggestion) {
		
		// Wrap the string nicely
		question = wrapString(question);

		String [] options = new String[2];
		int YES_OPTION = 0, NO_OPTION = 1;
		options[YES_OPTION] = "Yes";
		options[NO_OPTION] = "No";
		
		int answer = JOptionPane.showOptionDialog(null, question, "Question",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				options, options[0]);
		
		return (answer == YES_OPTION);
	}
	
	/**
	 * See the general contract declared in the <code>BeanEditorAPI</code>
	 * interface.
	 */
	public void infoPrompt(String message) {
		message = wrapString(message);
		JOptionPane.showMessageDialog(null, message, "Information",
				JOptionPane.INFORMATION_MESSAGE);
		
	}
	
	/**
	 * Inserts new line characters at the nearest white-space character after
	 * each 50 characters in the string.
	 * 
	 * @param str The string to wrap.
	 * @return The wrapped string.
	 */
	protected String wrapString(String str) {
		int breakLength = 50, breakPoint = 0;
		StringBuffer buf = new StringBuffer();
		
		// Wrap the question with a new line every 50 characters.
		while(str.length() > breakLength) {
		// Find a nice wrapping point
		breakPoint = str.indexOf(" ", breakLength);
		if(breakPoint == -1) { break; /* No more spaces... */ }
		
		// Append the sub string
		buf.append(str.substring(0, breakPoint));
		buf.append("\n");
		
		// Trim the question for the next iteration.
		str = str.substring(breakPoint);
		}
		// Append the rest of the question.
		buf.append(str);
		
		return buf.toString();
	}
	
	/**
	 * <p>
	 * Retrieves a mapping of menu options that should be added when this bean
	 * editor is used. The keys of this mapping represent the name of the
	 * top-level parent menu under which the corresponding menu item should
	 * appear. Note that a <code>JMenu</code> extends a <code>JMenuItem</code>
	 * and so multi-level (or nested) menus can be created using this method.
	 * </p>
	 * <p>
	 * This method should be implemented recursively such that a parent bean need
	 * only ask its immediate descendants for their menu options and any nested
	 * beans that may be unknown to the parent will also automatically expose
	 * their menu options as well.
	 * </p>
	 * <p>
	 * It is the responsibility of the top-most parent component of the
	 * application to actually create the menu bar and add it to the application.
	 * All menu items should already have action listeners and accessibility
	 * steps taken.
	 * </p>
	 * 
	 * @return
	 */
	public abstract TreeMap<String, JMenuItem> getMenuOptions();

}
