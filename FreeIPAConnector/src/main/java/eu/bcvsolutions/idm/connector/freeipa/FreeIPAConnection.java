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

import java.util.Map;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import eu.bcvsolutions.idm.connector.freeipa.httpclient.HttpClient;
import eu.bcvsolutions.idm.connector.freeipa.post.PingServicePost;
import eu.bcvsolutions.idm.connector.freeipa.security.FreeIPASessionCookie;
import eu.bcvsolutions.idm.connector.freeipa.service.response.JSONResponse;

public class FreeIPAConnection {

	private FreeIPAConfiguration config = null;
	private HttpClient wc = null;

	private static final Log log = Log.getLog(FreeIPAConnection.class);

	public FreeIPAConnection() {
	}

	public final HttpClient getWebClient() {
		// Authenticate if needed
		return wc;
	}

	public FreeIPAConnection(FreeIPAConfiguration config) {
		if (config == null) {
			log.error("[FreeIPA connector] Configuration is null");
			throw new ConnectionFailedException(
					"[FreeIPA connector] Configuration is null");
		}
		this.config = config;
		init();
	}

	public FreeIPAConfiguration getConfiguration() {
		return config;
	}

	/**
	 * Method initializes for FreeIPA.
	 */
	private final void init() {
		try {

			wc = HttpClient.create(config.getUrl());

		} catch (Throwable e) {
			throw new ConnectionFailedException(e);
		}

	}

	public void dispose() {
		this.config = null;
	}

	public void test() {
		test(true);
	}

	private void test(boolean retry) {
		PingServicePost sp = new PingServicePost(config.isDebug(), config);
		JSONResponse<Map<String, Object>> resp = sp.post(getWebClient());

		if (resp == null || resp.hasErrors()) {
			FreeIPASessionCookie.resetCookie();
			if (retry) {
				log.warn("[FreeIPA connector] Authentication failed for test() method: Cannot obtain session or session expired. Retry once again...", new Object [] {});
				test(false);
			} else {
				throw new ConnectorException(
						"[FreeIPA connector]  No data returned from FreeIPA");
			}
		}
	}
}
