package scratchJavaDevelopers.martinez.LossCurveSandbox.ui;

/**
 * Classes implementing the <code>BeanEditorAPI</code> provide basic
 * functionality to serve as a bridge between the user and the underlying bean.
 * This includes accepting values from a user and then setting those values with
 * the underlying beans. When invalid values are specified from the user, the
 * editor is relied upon to alert the user. Details of how this is handled are
 * left to each implementation. Implementation depends on the intended
 * deployment of the editor (i.e. web/gui/text?).
 * 
 * @author <a href="mailto:emartinez@usgs.gov">Eric Martinez</a>
 *
 */
public interface BeanEditorAPI {
	
	/**
	 * Presents the user with the question from the application and returns
	 * the answer as a boolean (yes/no) from the user. Implementation is
	 * dependent on the type of deployment of the editor (i.e. web/gui/text?).
	 * 
	 * @param question The yes/no question to ask the user.
	 * @param suggestion The default answer to return if the user refrains from
	 * responding.
	 * 
	 * @return <code>true</code> if the user responded positively,
	 * <code>false</code> otherwise. Note: &ldquo;positively&rdquo; is
	 * implementation-dependent but should make intuitive sense.
	 */
	public boolean boolPrompt(String question, boolean suggestion);
	
	
	/**
	 * Alerts user that something has happened. These messages are informational
	 * only and require no response from user. Implementation is dependent on the
	 * type of deployment of the editor (i.e. web/gui/text?).
	 * 
	 * While no response is <em>required</em>, developers should not assume
	 * run-time execution will immediately continue without confirmation of
	 * receiving the message.
	 * 
	 * @param message The message to present to the user.
	 */
	public void infoPrompt(String message);
}
