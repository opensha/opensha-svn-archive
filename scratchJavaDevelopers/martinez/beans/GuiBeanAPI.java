package scratchJavaDevelopers.martinez.beans;


public interface GuiBeanAPI {
	/**
	 * Predefined named variable for the Application type.
	 */
	public static final int APPLICATION = 0;
	/**
	 * Predefined named variable for the Applet type
	 */
	public static final int APPLET = 1;
	/**
	 * Predefined named variable for the Web type
	 */
	public static final int WEB = 2;
	
	/**
	 * Creates a visualization that can be used in any application type
	 * that returns <code>true</code> with a call to </code>isVisualizationSupported(type)</code>.
	 * 
	 * @param type An <code>int</code> defining the type of application visualization desired.
	 * @return The visualization of the GuiBean.  This might be a <code>JComponent</code> in 
	 * the case of an applet/application, but might just be an HTML <code>String</code> in the
	 * case of a web application.  Implementation can vary greatly.
	 * @throws IllegalArgumentException If <code>isVisualizationSupported(type)</code> returns false.
	 */
	public abstract Object getVisualization(int type) throws IllegalArgumentException;
	
	/**
	 * @return The fully qualified class name of the visualization object returned
	 * by <code>getVisualization</code>.
	 * 
	 * @param type The type of visualization desired.
	 */
	public abstract String getVisualizationClassName(int type);
	
	/**
	 * @param type The type of application the visualization is desired for.
	 * @return True if the visualization type is available, false otherwise.
	 */
	public abstract boolean isVisualizationSupported(int type);
	
}
