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

import java.rmi.RemoteException;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.SyncOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;

@ConnectorClass(displayNameKey = "FREEIPA_CONNECTOR", configurationClass = FreeIPAConfiguration.class)
public class FreeIPAConnector implements Connector, CreateOp, DeleteOp,
		SearchOp<String>, UpdateOp, SchemaOp, TestOp, SyncOp {

	private FreeIPAConfiguration config;
	private FreeIPAConnection connection;

	// Facade
	private FreeIPAObjectOperations facade;

	private static final Log log = Log.getLog(FreeIPAConnector.class);

	public FreeIPAConnector() {
	}

	@Override
	public Configuration getConfiguration() {
		return config;
	}

	/**
	 * Init connection
	 * 
	 * @see org.identityconnectors.framework.spi.Connector#init(org.identityconnectors.framework.spi.Configuration)
	 */
	@Override
	public void init(Configuration cfg) {
		config = (FreeIPAConfiguration) cfg;
		connection = new FreeIPAConnection(config);
		facade = getFacadeForOperations();
	}

	/**
	 * Dispose connection
	 * 
	 * @see org.identityconnectors.framework.spi.Connector#dispose()
	 */
	@Override
	public void dispose() {
		if (connection != null) {
			connection.dispose();
		}

		connection = null;
	}

	/**
	 * Test connection.
	 */
	@Override
	public void test() {
		connection.test();
	}

	private FreeIPAObjectOperations getFacadeForOperations() {
		if (this.config.getConnectionType().equals(
				ConnectionTarget.USERS.toString()))
			return new FreeIPAUsersOps(connection, config.isDebug(), config.getSizeLimit(), config.getTimeLimit());
		if (this.config.getConnectionType().equals(
				ConnectionTarget.GROUPS.toString()))
			return new FreeIPAGroupsOps(connection, config.isDebug(), config.getSizeLimit(), config.getTimeLimit());

		throw new ConfigurationException(
				"[FreeIPA connector] Bad connection type");
	}

	@Override
	public Uid create(ObjectClass objClass, Set<Attribute> attrs,
			OperationOptions options) {
		checkClassType(objClass);
		Uid uid = null;

		try {
			uid = facade.createObject(objClass, attrs, options);
		} catch (Exception ex) {
			throw new ConnectorException(ex);
		}

		return uid;
	}

	@Override
	public Uid update(ObjectClass obj, Uid uid, Set<Attribute> attrs,
			OperationOptions options) {
		checkClassType(obj);
		Uid newUid = null;

		try {
			newUid = facade.update(obj, uid, attrs, options);
		} catch (Exception ex) {
			throw new ConnectorException(ex);
		}

		return newUid;
	}

	@Override
	public void delete(ObjectClass objClass, Uid uid, OperationOptions options) {
		checkClassType(objClass);

		try {
			facade.delete(objClass, uid, options);
		} catch (Exception ex) {
			throw new ConnectorException(ex);
		}
	}

	@Override
	public Schema schema() {
		try {
			return facade.getSchema();
		} catch (Exception ex) {
			throw new ConnectorException(ex);
		}
	}

	/**
	 *
	 */
	private void checkClassType(ObjectClass oclass) throws ConnectorException {
		if (!oclass.is(ObjectClass.ACCOUNT_NAME)) {
			log.error("[FreeIPA connector] Bad exception type: {0}", oclass);
			throw new ConnectorException("Bad object type.");
		}
	}

	@Override
	public void sync(ObjectClass objClass, SyncToken token,
			SyncResultsHandler handler, OperationOptions options) {
		try {
			facade.sync(objClass, token, handler, options);
		} catch (RemoteException e) {
			log.error("[FreeIPA connector] Error while sync operation");
			e.printStackTrace();
		} catch (Exception e) {
			log.error("[FreeIPA connector] Error while sync operation");
			e.printStackTrace();
		}

	}

	@Override
	public SyncToken getLatestSyncToken(ObjectClass objClass) {
		try {
			return facade.getLatestSyncToken(objClass);
		} catch (RemoteException e) {
			log.error("[FreeIPA connector] Error while getLatestSyncToken operation");
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public FilterTranslator<String> createFilterTranslator(ObjectClass oclass,
			OperationOptions options) {
		return new AbstractFilterTranslator<String>() {

			@Override
			protected String createEqualsExpression(EqualsFilter filter,
					boolean not) {

				if (not) {
					throw new UnsupportedOperationException(
							"This type of equals expression is not allowed for now.");
				}

				Attribute attr = filter.getAttribute();
				if (attr == null || (!attr.is(Uid.NAME) && !attr.is(Name.NAME))) {
					throw new IllegalArgumentException(
							"[FreeIPA connector] Attribute is null or not UID or NAME attribute.");
				}

				if (attr.is(Uid.NAME))
					return ((Uid) attr).getUidValue();
				if (attr.is(Name.NAME))
					return ((Name) attr).getNameValue();

				return null;
			}

		};
	}

	@Override
	public void executeQuery(ObjectClass oclass, String query,
			ResultsHandler handler, OperationOptions options) {
		checkClassType(oclass);

		try {
			// Search object by uid
			facade.getObject(oclass, query, handler);
		} catch (Exception ex) {
			throw new ConnectorException(ex);
		}
	}
}
