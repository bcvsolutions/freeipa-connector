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

import static eu.bcvsolutions.idm.connector.freeipa.FreeIPAConstants.CN_ATTRIBUTE;
import static eu.bcvsolutions.idm.connector.freeipa.FreeIPAConstants.FREE_IPA_CN_ATTRIBUTE;
import static eu.bcvsolutions.idm.connector.freeipa.FreeIPAConstants.LOGIN_ATRIBUTE;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;

import eu.bcvsolutions.idm.connector.freeipa.post.group.AddGroupServicePost;
import eu.bcvsolutions.idm.connector.freeipa.post.group.DeleteGroupServicePost;
import eu.bcvsolutions.idm.connector.freeipa.post.group.FindGroupServicePost;
import eu.bcvsolutions.idm.connector.freeipa.post.group.UpdateGroupServicePost;
import eu.bcvsolutions.idm.connector.freeipa.service.response.JSONResponse;
import eu.bcvsolutions.idm.connector.freeipa.service.response.result.FindResponseResult;

/**
 * Implements connector framework methods for FreeIPA groups.
 */
public class FreeIPAGroupsOps implements FreeIPAObjectOperations {

	private FreeIPAConnection connection;

	private static final Log log = Log.getLog(FreeIPAGroupsOps.class);

	private final boolean debug;
	
	/**
	 * Time limits in seconds. If time to process query in FreeIPA is longer than this,
	 * "truncated" attribute in result will be set to true and only subset of records will
	 * be returned
	 */
	private final int timeLimit;
	
	/**
	 * If size of result set is bugger than this, "truncated" attribute in result 
	 * will be set to true and only subset of this size of records will be returned
	 */
	private final int sizeLimit;

	public FreeIPAGroupsOps(FreeIPAConnection connection, boolean debug, final int sizeLimit, final int timeLimit) {
		this.connection = connection;
		this.debug = debug;
		this.sizeLimit = sizeLimit;
		this.timeLimit = timeLimit;
	}

	@Override
	public Uid createObject(ObjectClass objClass, Set<Attribute> attrs,
			OperationOptions options) throws Exception {
		Map<String, Object> params = new HashMap<>();

		// Just converting parameters, so we can generate JSON
		String cn = fillParamsAndFindCN(attrs, params);

		if (cn == null || cn.isEmpty()) {
			throw new ConnectorException(
					"[FreeIPA connector] CN attribute cannot be null");
		}

		// Send request
		AddGroupServicePost post = new AddGroupServicePost(cn, params, debug, connection.getConfiguration());
		post.post(this.connection.getWebClient());

		return new Uid(cn);
	}

	@Override
	public Uid update(ObjectClass obj, Uid uid, Set<Attribute> attrs,
			OperationOptions options) throws Exception {
		Map<String, Object> params = new HashMap<>();

		// Just converting parameters, so we can generate JSON
		fillParamsAndFindCN(attrs, params);

		// Send request
		if (!params.isEmpty()) {
			UpdateGroupServicePost post = new UpdateGroupServicePost(
					uid.getUidValue(), params, debug, connection.getConfiguration());
			post.post(this.connection.getWebClient());
		}

		return uid;
	}

	@Override
	public void delete(ObjectClass objClass, Uid uid, OperationOptions options)
			throws Exception {
		DeleteGroupServicePost post = new DeleteGroupServicePost(
				uid.getUidValue(), new HashMap<String, Object>(), this.debug, connection.getConfiguration());
		post.post(this.connection.getWebClient());
	}

	@Override
	public Schema getSchema() throws Exception {
		final SchemaBuilder builder = new SchemaBuilder(FreeIPAConnector.class);

		Set<AttributeInfo> attributes = new HashSet<AttributeInfo>();

		// Only ACCOUNT type is possible.
		builder.defineObjectClass(ObjectClass.ACCOUNT_NAME, attributes);
		return builder.build();
	}

	@Override
	public void getObject(ObjectClass objClass, Object id,
			ResultsHandler handler) throws RemoteException {
		FindGroupServicePost post;
		if (id == null || StringUtil.isBlank(id.toString())) {
			post = new FindGroupServicePost(null,
					new HashMap<String, Object>(), debug, true, sizeLimit, timeLimit, connection.getConfiguration());
		} else {
			// We only search by CN now. If you need advanced filtering, this is
			// the place, where you would implement it
			// (also in createFilterTranslator method in FreeIPAConnector class)
			Map<String, Object> params = new HashMap<String, Object>();
			params.put(CN_ATTRIBUTE, id.toString());
			post = new FindGroupServicePost(null, params, debug, connection.getConfiguration());
		}

		JSONResponse<FindResponseResult> res = post.post(this.connection
				.getWebClient());

		
		// Create ConnectorObject and invoke handle method on given handler
		for (Map<String, Object> group : res.getResult().getResult()) {
			handler.handle(FreeIPAUtils.createConnectorObjectGroup(group,
					objClass));
		}
	}

	@Override
	public void sync(ObjectClass objClass, SyncToken token,
			SyncResultsHandler handler, OperationOptions options)
			throws RemoteException {
		throw new UnsupportedOperationException(
				"[FreeIPA connector] Sync operation for GROUP is not supported yet");
	}

	@Override
	public SyncToken getLatestSyncToken(ObjectClass objClass)
			throws RemoteException {
		throw new UnsupportedOperationException(
				"[FreeIPA connector] GetLastSyncToken operation for GROUP is not supported yet");
	}
	
	// ======================================= UTIL METHODS
	
	/**
	 * 
	 * Fills params from set of given attributes and finds CN attribute among
	 * them. If CN attribute has null value, {@link ConnectorException} is
	 * thrown.
	 * 
	 * @param attrs
	 * @param params
	 * @return
	 */
	private String fillParamsAndFindCN(Set<Attribute> attrs,
			Map<String, Object> params) {
		String cn = null;
		for (Attribute attr : attrs) {

			// TODO: maybe configurable cn attribute??
			if (attr.getName().equals(FREE_IPA_CN_ATTRIBUTE)) {
				if (attr.getValue() == null) {
					log.error(
							"[FreeIPA connector] CN attribute {0} cannot be null",
							LOGIN_ATRIBUTE);
					throw new ConnectorException(
							"[FreeIPA connector] CN attribute cannot be null");
				}
				cn = attr.getValue().get(0).toString();
				continue;
			}

			if (attr.getValue() == null) {
				log.warn(
						"[FreeIPA connector] WARNING attribute {0} has no value",
						attr.getName());
				params.put(attr.getName(), null);
			} else {
				// Deserialize if it is a complex type
				params.put(attr.getName(),
						FreeIPAUtils.deserializeIfNecessary(attr.getValue()));
			}
		} // END OF for
		return cn;
	}

}
