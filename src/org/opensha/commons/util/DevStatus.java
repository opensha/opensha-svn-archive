package org.opensha.commons.util;

/**
 * Class that may be used to represent different states of development. For
 * example, a particular interface or abstract class may have some
 * implementations that have been vetted and tested and are ready for use in
 * production environments while others are under development, experimental, or
 * deprecated.
 * 
 * @author Peter Powers
 * @version $Id$
 */
public enum DevStatus {

	/** Status indicating something is production ready. */
	PRODUCTION,

	/** Status indicating something is under development. */
	DEVELOPMENT,

	/** Status indicating something is deprecated. */
	DEPRECATED;
}
