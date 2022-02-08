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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Set;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;

/**
 * This abstract class defines set of methods which has to be implemented for
 * particular type of objects.
 *
 */
public interface FreeIPAObjectOperations {

	/**
	 * This method create a new object in FreeIPA.
	 * 
	 * @param objClass
	 * @param attrs
	 * @param options
	 * @return
	 * @throws Exception
	 */
	public Uid createObject(ObjectClass objClass, Set<Attribute> attrs,
			OperationOptions options) throws Exception;

	/**
	 * This method actualizes a given object in FreeIPA according to set of
	 * attributes.
	 * 
	 * @param obj
	 * @param uid
	 * @param attrs
	 * @param options
	 * @return
	 * @throws Exception
	 */
	public abstract Uid update(ObjectClass obj, Uid uid, Set<Attribute> attrs,
			OperationOptions options) throws Exception;

	/**
	 * The method deletes object for given ID in FreeIPA.
	 * 
	 * @param objClass
	 * @param uid
	 * @param options
	 * @throws Exception
	 */
	public void delete(ObjectClass objClass, Uid uid, OperationOptions options)
			throws Exception;

	/**
	 * This method returns a schema for given object type in FreeIPA.
	 * 
	 * @return
	 * @throws Exception
	 */
	public Schema getSchema() throws Exception;

	public void getObject(ObjectClass objClass, Object id,
			ResultsHandler handler) throws RemoteException, IOException;

	public void sync(ObjectClass objClass, SyncToken token,
			SyncResultsHandler handler, OperationOptions options)
			throws RemoteException, Exception;

	public SyncToken getLatestSyncToken(ObjectClass objClass)
			throws RemoteException;

}
