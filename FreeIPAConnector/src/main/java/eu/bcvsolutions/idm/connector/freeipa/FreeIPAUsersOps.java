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

import static eu.bcvsolutions.idm.connector.freeipa.FreeIPAConstants.DISABLED_ATRIBUTE;
import static eu.bcvsolutions.idm.connector.freeipa.FreeIPAConstants.FREE_IPA_USER_ATTRIBUTE;
import static eu.bcvsolutions.idm.connector.freeipa.FreeIPAConstants.LOGIN_ATRIBUTE;
import static eu.bcvsolutions.idm.connector.freeipa.FreeIPAConstants.MEMBER_OF_ATTRIBUTE;
import static eu.bcvsolutions.idm.connector.freeipa.FreeIPAConstants.NS_ACCOUNT_LOCK_ATRIBUTE;
import static eu.bcvsolutions.idm.connector.freeipa.FreeIPAConstants.RENAME_ATTRIBUTE;
import static eu.bcvsolutions.idm.connector.freeipa.FreeIPAConstants.SPECIAL_ATTRIBUTES;
import static eu.bcvsolutions.idm.connector.freeipa.FreeIPAConstants.UID_ATRIBUTE;

import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;

import eu.bcvsolutions.idm.connector.freeipa.post.group.AddMemberGroupServicePost;
import eu.bcvsolutions.idm.connector.freeipa.post.group.RemoveMemberGroupServicePost;
import eu.bcvsolutions.idm.connector.freeipa.post.user.AddUserServicePost;
import eu.bcvsolutions.idm.connector.freeipa.post.user.DeleteUserServicePost;
import eu.bcvsolutions.idm.connector.freeipa.post.user.DisableUserServicePost;
import eu.bcvsolutions.idm.connector.freeipa.post.user.EnableUserServicePost;
import eu.bcvsolutions.idm.connector.freeipa.post.user.FindUserServicePost;
import eu.bcvsolutions.idm.connector.freeipa.post.user.UpdateUserServicePost;
import eu.bcvsolutions.idm.connector.freeipa.service.response.JSONResponse;
import eu.bcvsolutions.idm.connector.freeipa.service.response.result.FindResponseResult;

public class FreeIPAUsersOps implements FreeIPAObjectOperations {

	private FreeIPAConnection connection;
	private static final Log log = Log.getLog(FreeIPAUsersOps.class);
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
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

	public FreeIPAUsersOps(FreeIPAConnection connection, boolean debug, int sizeLimit, int timeLimit) {
		this.connection = connection;
		this.debug = debug;
		this.sizeLimit = sizeLimit;
		this.timeLimit = timeLimit;
	}

	@Override
	public Uid createObject(ObjectClass objClass, Set<Attribute> attrs,
			OperationOptions options) throws Exception {

		Map<String, Object> params = new HashMap<>();
		// This collection is used for remembering password and enabled
		// attributes (possibly others?).
		// Reason for this is that FreeIPA has separate methods for password
		// change and enabling users
		// so we have to call them separately.
		Map<String, Object> specialParams = new HashMap<>();

		// Just converting parameters, so we can generate JSON
		String login = fillParamsAndFindLogin(attrs, params, specialParams);

		if (login == null || login.isEmpty()) {
			throw new ConnectorException(
					"[FreeIPA connector] Login attribute cannot be null");
		}

		// Send request
		AddUserServicePost post = new AddUserServicePost(login, params, debug, connection.getConfiguration());
		post.post(this.connection.getWebClient());

		// Disabling and maybe other operations?
		doSpecialActions(login, specialParams, true);

		return new Uid(login);

	}

	@Override
	public Uid update(ObjectClass obj, Uid uid, Set<Attribute> attrs,
			OperationOptions options) throws Exception {

		Map<String, Object> params = new HashMap<>();
		// This collection is used for remembering group membership
		// attributes (possibly others?).
		// Reason for this is that FreeIPA has separate methods for
		// adding and removing users from groups
		// so we have to call them separately.
		Map<String, Object> specialParams = new HashMap<>();

		// Just converting parameters, so we can generate JSON
		final String name = fillParamsAndFindLogin(attrs, params, specialParams);

		// Renaming is handled in separate attribute
		if (isRenaming(uid, name)) {
			params.put(RENAME_ATTRIBUTE, name);
		}

		// Send request
		if (!params.isEmpty()) {
			UpdateUserServicePost post = new UpdateUserServicePost(
					uid.getUidValue(), params, debug, connection.getConfiguration());
			post.post(this.connection.getWebClient());
		} else {
			log.info("[FreeIPA connector] Empty attributes - no operation will be performed");
		}

		// Group memberships and maybe other operations?
		doSpecialActions(uid.getUidValue(), specialParams, false);

		return isRenaming(uid, name) ? new Uid(name) : uid;
	}

	private boolean isRenaming(Uid uid, String name) {
		return name != null && !uid.getUidValue().equals(name);
	}

	/**
	 * 
	 * Because some actions cannot be performed simply by setting attributes on
	 * user objects, we have to invoke special commands in order to achieve
	 * desired result.
	 * 
	 * @param login
	 * @param specialParams
	 */
	private void doSpecialActions(String login,
			Map<String, Object> specialParams, boolean isCreate) {

		for (String paramName : specialParams.keySet()) {
			switch (paramName) {
			case DISABLED_ATRIBUTE:
				// We do not use enabling and disabling of users by calling separate FreeIPA methods
				// however it might come in handy in the future. If so, then only thing needed to do so
				// is to add DISABLED_ATRIBUTE in SPECIAL_ATTRIBUTES in FreeIPAConstants class
				enableDisableUser(login, specialParams.get(paramName));
				break;
			case MEMBER_OF_ATTRIBUTE:
				processGroupMemberships(login, specialParams.get(paramName), isCreate);
				break;
			default:
				break;
			}
		}

	}

	/**
	 * 
	 * Processes group memberships. This method compares value of
	 * MEMBER_OF_ATTRIBUTE with given list of groups and then calls
	 * group_add_member or group_remove_member respectively in order to
	 * synchronize group memberships.
	 * 
	 * @param login
	 * @param object
	 * @throws ConnectorException
	 *             if given member value is not a {@link Collection}
	 */
	private void processGroupMemberships(final String login, final Object object, boolean addOnly) {
		if (!(object instanceof Collection<?>)) {
			throw new ConnectorException("Malformed " + MEMBER_OF_ATTRIBUTE
					+ " member of attribute "+object);
		}

		Collection<?> desiredMemberships = (Collection<?>) object;
		Collection<?> ipaMemberships = getIPAMemberships(login);

		Collection<Object> toAdd = new ArrayList<Object>();
		toAdd.addAll(desiredMemberships);
		toAdd.removeAll(ipaMemberships);
		
		addUserGroups(toAdd, login);
		
		if (!addOnly) {
			Collection<Object> toRemove = new ArrayList<Object>();
			toRemove.addAll(ipaMemberships);
			toRemove.removeAll(desiredMemberships);		
			removeUserGroups(toRemove, login);
		}
	}

	/**
	 * 
	 * Removes user from user group in FreeIPA
	 * 
	 * @param toRemove
	 * @param login
	 */
	private void removeUserGroups(Collection<Object> toRemove,
			final String login) {

		for (Object o : toRemove) {
			String group = o.toString();
			log.info("[FreeIPA connector] Removing user {0} from group {1}",
					login, group);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put(FREE_IPA_USER_ATTRIBUTE, login);
			RemoveMemberGroupServicePost post = new RemoveMemberGroupServicePost(
					group, params, this.debug, connection.getConfiguration());
			post.post(this.connection.getWebClient());
		}

	}

	/**
	 * 
	 * Adds user to user group in FreeIPA
	 * 
	 * @param toAdd
	 * @param login
	 */
	private void addUserGroups(Collection<Object> toAdd, final String login) {

		for (Object o : toAdd) {
			String group = o.toString();
			log.info("[FreeIPA connector] Adding user {0} to group {1}", login,
					group);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put(FREE_IPA_USER_ATTRIBUTE, login);
			AddMemberGroupServicePost post = new AddMemberGroupServicePost(
					group, params, this.debug, connection.getConfiguration());
			post.post(this.connection.getWebClient());
		}

	}

	/**
	 * 
	 * Returns users group memberships in FreeIPA
	 * 
	 * @param login
	 *            Login of user
	 * @return
	 */
	private List<?> getIPAMemberships(String login) {
		Map<String, Object> params = new HashMap<>();
		params.put(FreeIPAConstants.UID_ATRIBUTE, login);
		FindUserServicePost post = new FindUserServicePost(login,
				params, this.debug, connection.getConfiguration());
		JSONResponse<FindResponseResult> res = post.post(this.connection
				.getWebClient());

		List<Object> ipaMemberships = new ArrayList<>();
		Object o = res.getResult().getResult()[0].get(MEMBER_OF_ATTRIBUTE);
		if (o instanceof Collection<?>) {
			ipaMemberships.addAll((Collection<?>) o);
		} else if (o != null) {
			ipaMemberships.add(o.toString());
		}
		return ipaMemberships;
	}

	/**
	 * 
	 * Enables or disables user according to given enable attribute
	 * 
	 * @param login
	 * @param disable
	 */
	private void enableDisableUser(String login, Object disable) {
		boolean disableBool = (boolean) disable;
		if (!disableBool) {
			EnableUserServicePost post = new EnableUserServicePost(login, debug, connection.getConfiguration());
			post.post(this.connection.getWebClient());
		} else {
			DisableUserServicePost post = new DisableUserServicePost(login,
					debug, connection.getConfiguration());
			post.post(this.connection.getWebClient());
		}
	}

	@Override
	public void delete(ObjectClass objClass, Uid uid, OperationOptions options)
			throws Exception {
		DeleteUserServicePost post = new DeleteUserServicePost(
				uid.getUidValue(), new HashMap<String, Object>(), debug, connection.getConfiguration());
		post.post(this.connection.getWebClient());
	}

	@Override
	public Schema getSchema() throws Exception {
		final SchemaBuilder builder = new SchemaBuilder(FreeIPAConnector.class);

		Set<AttributeInfo> attributes = new HashSet<AttributeInfo>();
		attributes.add(Name.INFO); // ID

		attributes.add(new AttributeInfoBuilder(NS_ACCOUNT_LOCK_ATRIBUTE)
				.setType(Boolean.class).setRequired(false).build());

		// Only ACCOUT type is posible
		builder.defineObjectClass(ObjectClass.ACCOUNT_NAME, attributes);
		return builder.build();
	}

	@Override
	public void getObject(ObjectClass objClass, Object id,
			ResultsHandler handler) throws IOException {

		FindUserServicePost post;
		if (id == null || StringUtil.isBlank(id.toString())) {
			post = new FindUserServicePost(null, new HashMap<String, Object>(),
					debug, true, sizeLimit, timeLimit, connection.getConfiguration());
		} else {
			// We only search by UID now. If you need advanced filtering, this
			// is the place, where you would implement it
			// (also in createFilterTranslator method in FreeIPAConnector class)
			Map<String, Object> params = new HashMap<String, Object>();
			params.put(UID_ATRIBUTE, id.toString());
			post = new FindUserServicePost(null, params, debug, connection.getConfiguration());
		}

		JSONResponse<FindResponseResult> res = post.post(this.connection
				.getWebClient());

		// Parsing response... nothing interesting

		for (Map<String, Object> user : res.getResult().getResult()) {
			handler.handle(FreeIPAUtils.createConnectorObjectUser(user,
					objClass));
		}
	}

	@Override
	public void sync(ObjectClass objClass, SyncToken token,
			SyncResultsHandler handler, OperationOptions options)
			throws Exception {
		throw new UnsupportedOperationException(
				"[FreeIPA connector] Operation SYNC is not yet supported");
	}

	@Override
	public SyncToken getLatestSyncToken(ObjectClass objClass)
			throws RemoteException {
		BigDecimal date = new BigDecimal(sdf.format(new Date()));
		SyncToken token = new SyncToken(date);
		return token;
	}

	// ====================== UTIL METHODS ======================

	private String fillParamsAndFindLogin(Set<Attribute> attrs,
			Map<String, Object> params, Map<String, Object> specialParams) {
		String login = null;
		for (Attribute attr : attrs) {

			// TODO: maybe configurable login attribute??
			if (attr.getName().equals(LOGIN_ATRIBUTE)) {
				if (attr.getValue() == null) {
					log.error(
							"[FreeIPA connector] Login attribute {0} cannot be null",
							LOGIN_ATRIBUTE);
					throw new ConnectorException(
							"[FreeIPA connector] Login attribute cannot be null");
				}
				login = attr.getValue().get(0).toString();
				continue;
			}

			if (SPECIAL_ATTRIBUTES.contains(attr.getName())) {
				specialParams.put(attr.getName(), attr.getValue());
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
		return login;
	}

}
