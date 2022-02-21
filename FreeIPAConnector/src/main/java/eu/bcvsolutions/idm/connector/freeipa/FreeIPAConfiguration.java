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

package eu.bcvsolutions.idm.connector.freeipa;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

public class FreeIPAConfiguration extends AbstractConfiguration {

	/**
	 * URL address to FreeIPA
	 */
	private String url;

	/**
	 * User name to FreeIPA
	 */
	private String username;

	/**
	 * User password
	 */
	private GuardedString password;

	/**
	 * FreeIPA target to managed
	 */
	private String connectionType;

	/**
	 * If true, then requests and responses will be displayed in log
	 */
	private boolean debug;
	
	/**
	 * Time limits in seconds. If time to process query in FreeIPA is longer than this,
	 * "truncated" attribute in result will be set to true and only subset of records will
	 * be returned
	 */
	private int timeLimit;
	
	/**
	 * If size of result set is bugger than this, "truncated" attribute in result 
	 * will be set to true and only subset of this size of records will be returned
	 */
	private int sizeLimit;

	public FreeIPAConfiguration() {
	}

	@ConfigurationProperty(order = 1, displayMessageKey = "IPA_URL", helpMessageKey = "IPA_URL_HELP", required = true)
	public String getUrl() {
		return url;
	}

	@ConfigurationProperty(order = 2, displayMessageKey = "IPA_USERNAME", helpMessageKey = "IPA_USERNAME_HELP", required = true)
	public String getUsername() {
		return username;
	}

	@ConfigurationProperty(order = 3, displayMessageKey = "IPA_PASSWD", helpMessageKey = "IPA_PASSWD_HELP", confidential = true)
	public GuardedString getPassword() {
		return password;
	}

	@ConfigurationProperty(order = 4, displayMessageKey = "IPA_CONNECTION_TYPE", helpMessageKey = "IPA_CONNECTION_TYPE_HELP", required = true)
	public String getConnectionType() {
		return connectionType;
	}

	@ConfigurationProperty(order = 5, displayMessageKey = "IPA_DEBUG", helpMessageKey = "IPA_DEBUG_HELP", required = true)
	public boolean isDebug() {
		return debug;
	}
	
	@ConfigurationProperty(order = 6, displayMessageKey = "IPA_TIME_LIMIT", helpMessageKey = "IPA_TIME_LIMIT_HELP", required = true)
	public int getTimeLimit() {
		return timeLimit;
	}
	
	@ConfigurationProperty(order = 7, displayMessageKey = "IPA_SIZE_LIMIT", helpMessageKey = "IPA_SIZE_LIMIT_HELP", required = true)
	public int getSizeLimit() {
		return sizeLimit;
	}

	public void setSizeLimit(int sizeLimit) {
		this.sizeLimit = sizeLimit;
	}
	
	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Returns a message for given key.
	 * 
	 * @param key
	 *            name of message from "Message.properties" file
	 * @return message
	 */
	public String getMessage(String key) {
		return getConnectorMessages().format(key, key);
	}

	/**
	 * Returns a message for given key.
	 * 
	 * @param key
	 *            name of message from "Message.properties" file
	 * @param objects
	 *            parameters
	 * @return message
	 */
	public String getMessage(String key, Object... objects) {
		return getConnectorMessages().format(key, key, objects);
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(GuardedString password) {
		this.password = password;
	}

	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}

	@Override
	public void validate() {
		if (StringUtil.isBlank(getUrl())) {
			throw new ConfigurationException(
					"URL to FreeIPA endpoint must be set");
		}
		if (StringUtil.isBlank(getUsername())) {
			throw new ConfigurationException("Username parameter must be set");
		}
		if (StringUtil.isBlank(getConnectionType())) {
			throw new ConfigurationException(
					"Target connection type muset be set");
		}
		
		if (getSizeLimit() < 0) {
			throw new ConfigurationException("Size limit must be greater than zero");
		}
		
		if (getTimeLimit() < 0) {
			throw new ConfigurationException("Time limit must be greater than zero");
		}

	}

}