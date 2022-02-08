package eu.bcvsolutions.freeipa.connector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;

public class FreeIPAUtils {

	private static final Log log = Log.getLog(FreeIPAUtils.class);

	/**
	 * 
	 * Creates {@link ConnectorObject} from given attributes map and sets its uid and name attributes 
	 * to the value of uidAttrName parameter.
	 * 
	 * @param attrs
	 * @param oclass
	 * @param uidAttrName
	 * @return
	 */
	public static ConnectorObject createConnectorObject(
			Map<String, Object> attrs, ObjectClass oclass, String uidAttrName) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setObjectClass(oclass);

		for (String attrName : attrs.keySet()) {
			if (attrName.equals(uidAttrName)) {
				builder.setName(processLoginAttribute(attrs.get(attrName)));
				builder.setUid(processLoginAttribute(attrs.get(attrName)));
			} else {
				builder.addAttribute(attrName,
						processAttribute(attrName, attrs.get(attrName)));
			}
		}

		return builder.build();
	}

	/**
	 * 
	 * Creates {@link ConnectorObject} and sets its name according to attribute with 
	 * name determined by {@link FreeIPAConstants#UID_ATRIBUTE}
	 * 
	 * @param attrs
	 * @param oclass
	 * @return
	 */
	public static ConnectorObject createConnectorObjectUser(
			Map<String, Object> attrs, ObjectClass oclass) {
		return createConnectorObject(attrs, oclass,
				FreeIPAConstants.UID_ATRIBUTE);
	}

	/**
	 * 
	 * Creates {@link ConnectorObject} and sets its name according to attribute with 
	 * name determined by {@link FreeIPAConstants#CN_ATTRIBUTE}
	 * 
	 * @param attrs
	 * @param oclass
	 * @return
	 */
	public static ConnectorObject createConnectorObjectGroup(
			Map<String, Object> group, ObjectClass objClass) {
		return createConnectorObject(group, objClass,
				FreeIPAConstants.CN_ATTRIBUTE);
	}

	/**
	 * 
	 * Extracts login attribute from {@link Object} value. It should be either single
	 * valued attribute, or multivalued attribute with one value.
	 * 
	 * @param object
	 * @return String representation of login attribute
	 * @throws ConnectorException if single {@link String} value cannot be extracted from given {@link Object}
	 */
	private static String processLoginAttribute(Object object) {
		if (object instanceof Collection<?>) {
			Collection<?> col = (Collection<?>) object;
			if (col.size() == 1) {
				return col.iterator().next().toString();
			} else {
				throw new ConnectorException("[FreeIPA connector] Malformed login attribute");
			}
		} else {
			return object.toString();
		}
	}

	/**
	 * 
	 * Purpose of this method is to create {@link Collection} of objects, that can be send
	 * via OpenICF interface to IdM. If given object is collection, then multivalued attribute will be send
	 * to IdM and its values will either be basic types or serialized array of bytes, depending on 
	 * {@link FrameworkUtil#isSupportedAttributeType(Class)} result.
	 * 
	 * 
	 * @param attrName
	 * @param object
	 * @return
	 */
	private static Collection<?> processAttribute(String attrName, Object object) {
		if (object instanceof Collection<?>) {
			Collection<Object> col = new ArrayList<>();
			for (Object o : (Collection<?>) object) {
				if (!FrameworkUtil.isSupportedAttributeType(o.getClass())) {
					col.add(serialize(o));
				} else {
					col.add(o);
				}
			}
			return col;
		} else {
			Collection<Object> col = new ArrayList<>();
			col.add(object);
			return col;
		}
	}

	/**
	 * This function transforms object to byte array.
	 * 
	 * @param object
	 * @return
	 */
	public static byte[] serialize(Object object) {
		ByteArrayOutputStream handleBAOS = new ByteArrayOutputStream(100);
		try {
			ObjectOutputStream handleOOS = new ObjectOutputStream(handleBAOS);
			handleOOS.writeObject(object);

			return handleBAOS.toByteArray();

		} catch (Exception e) {
			throw new ConnectorException(e);

		} finally {
			try {
				handleBAOS.close();

			} catch (IOException e) {
				log.error("[FreeIPA connector] Cannot close ByteArrayOutputStream.", e);
			}
		}
	}

	/**
	 * This function creates object from byte array.
	 * 
	 * @param objectBytes
	 * @return
	 */
	public static Object deserialize(byte[] objectBytes) {

		ByteArrayInputStream bais = new ByteArrayInputStream(objectBytes);

		try {
			ObjectInputStream ois = new ObjectInputStream(bais);

			return ois.readObject();

		} catch (Exception e) {
			throw new ConnectorException(e);

		} finally {
			try {
				bais.close();

			} catch (IOException e) {
				log.error("[FreeIPA connector] Cannot close ByteArrayInputStream.", e);
			}
		}
	}

	public static Object deserializeIfCan(Object o) {
		try {
			if (o instanceof byte[]) {
				byte[] input = (byte[]) o;
				return deserialize(input);
			} else if (o instanceof GuardedString) {
				// This is here primarily for passwords
				GuardedString gs = (GuardedString) o;
				GuardedStringAccessor accessor = new GuardedStringAccessor();
				gs.access(accessor);
				char[] result = accessor.getArray();
				final String finstr = String.copyValueOf(result);
				return finstr;
			} else {
				return o;
			}
		} catch (Throwable t) {
			log.error("[FreeIPA connector] Cannot deserialize object {0}", o);
			throw new ConnectorException(t);
		}
	}

	public static Object deserializeIfNecessary(List<Object> value) {
		boolean multivalued = value.size() > 1;

		if (multivalued) {

			List<Object> val = new ArrayList<Object>();
			for (Object o : value) {
				val.add(deserializeIfCan(o));
			}
			return val;

		} else {
			// If it is not multivalued, then we try to pass it as it is, if
			Object val = value.get(0);
			return deserializeIfCan(val);
		}

	}
	
	public static String diffFormatTime(Calendar a, Calendar b) {
		long diff = Math.max(a.getTimeInMillis() - b.getTimeInMillis(), 0);
		Calendar diffCal = Calendar.getInstance();
		diffCal.setTimeInMillis(diff);
		return diffCal.get(Calendar.MINUTE) + "minutes " + diffCal.get(Calendar.SECOND) + "seconds";
	}

}
