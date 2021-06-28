/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 2021 Dart Productions
 Released under the GNU General Public License version 3
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 
 McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package dartproductions.mcleodmassacre.resources;

import java.io.Serial;

/**
 * This runtime exception is somewhere between {@link IllegalStateException} and {@link IllegalArgumentException} - it signals that in a configuration, some of the values are illegal. This might mean that the configuration was not fully configured yet, or that the configured values are not valid for their uses.
 *
 * @since 0.1.0
 */
public class IllegalConfigurationException extends RuntimeException {
	@Serial private static final long serialVersionUID = -7507185525167101714L;
	
	
	/**
	 * Constructs an IllegalConfigurationException with no detail message.
	 * A detail message is a String that describes this particular exception.
	 */
	public IllegalConfigurationException() {
		super();
	}
	
	/**
	 * Constructs an IllegalConfigurationException with the specified detail
	 * message.  A detail message is a String that describes this particular
	 * exception.
	 *
	 * @param s the String that contains a detailed message
	 */
	public IllegalConfigurationException(String s) {
		super(s);
	}
	
	/**
	 * Constructs a new exception with the specified detail message and
	 * cause.
	 *
	 * <p>Note that the detail message associated with {@code cause} is
	 * <i>not</i> automatically incorporated in this exception's detail
	 * message.
	 *
	 * @param message the detail message (which is saved for later retrieval
	 *                by the {@link Throwable#getMessage()} method).
	 * @param cause   the cause (which is saved for later retrieval by the
	 *                {@link Throwable#getCause()} method).  (A {@code null} value
	 *                is permitted, and indicates that the cause is nonexistent or
	 *                unknown.)
	 * @since 1.5
	 */
	public IllegalConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructs a new exception with the specified cause and a detail
	 * message of {@code (cause==null ? null : cause.toString())} (which
	 * typically contains the class and detail message of {@code cause}).
	 * This constructor is useful for exceptions that are little more than
	 * wrappers for other throwables (for example, {@link
	 * java.security.PrivilegedActionException}).
	 *
	 * @param cause the cause (which is saved for later retrieval by the
	 *              {@link Throwable#getCause()} method).  (A {@code null} value is
	 *              permitted, and indicates that the cause is nonexistent or
	 *              unknown.)
	 * @since 1.5
	 */
	public IllegalConfigurationException(Throwable cause) {
		super(cause);
	}
}
