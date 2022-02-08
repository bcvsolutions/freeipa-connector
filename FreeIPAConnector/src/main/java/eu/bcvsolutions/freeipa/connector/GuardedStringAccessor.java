/**
 * CzechIdM
 * Copyright (C) 2014 BCV solutions s.r.o., Czech Republic
 * 
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License 2.1 as published by the Free Software Foundation;
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free 
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * 
 * You can contact us on website http://www.bcvsolutions.eu.
 */

package eu.bcvsolutions.freeipa.connector;

import java.util.Arrays;

import org.identityconnectors.common.security.GuardedString;

/**
 * GuardedString handle class.
 */
public class GuardedStringAccessor implements GuardedString.Accessor {

	private char[] array;

	/**
	 * Method transforms a password from GuardedString into the array of chars.
	 */
	@Override
	public void access(char[] clearChars) {
		array = new char[clearChars.length];
		System.arraycopy(clearChars, 0, array, 0, clearChars.length);
	}

	/**
	 * Return the password in a char array.
	 * 
	 * @return password
	 */
	public char[] getArray() {
		return array;
	}

	/**
	 * Method clears the char array which holds the password.
	 */
	public void clearArray() {
		Arrays.fill(array, 0, array.length, ' ');
	}

}
