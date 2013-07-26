package scratch.peter.nga;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * Static utility {@link Function}s, so named so as to be distinct from the
 * Guava {@link Functions} class.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class Functions2 {

	private Functions2() {}

	/**
	 * Instance of a {@code Function} that parses a {@code String} to {@code Double}
	 * using {@link Double#valueOf(String)} throwing
	 * {@code NumberFormatException}s and {@code NullPointerException}s for
	 * invalid and {@code null} arguments.
	 */
	public static final Function<String, Double> STR_2_DBL = StringToDouble.INSTANCE;

	// enum singleton patterns
	// @formatter:off
	private enum StringToDouble implements Function<String, Double> {
		INSTANCE;
		@Override public Double apply(String s) { return Double.valueOf(s); }
	}

	// @formatter:on

	public static void main(String[] args) {
		String pp = null;
		double v = STR_2_DBL.apply(pp);
		System.out.println(v);
	}
}
