package scratchJavaDevelopers.martinez.LossCurveSandbox.ui;

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
}
