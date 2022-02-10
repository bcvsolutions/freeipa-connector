package eu.bcvsolutions.freeipa.connector;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test the {@link FreeIPAUtils}.
 *
 */
public class FreeIPAUtilsTests {

	@Test
	public void testdiffFormatTime() {
		Calendar c1 = Calendar.getInstance();
		c1.set(Calendar.YEAR, 2000);
		c1.set(Calendar.MONTH, Calendar.JANUARY);
		c1.set(Calendar.DAY_OF_MONTH, 01);
		c1.set(Calendar.HOUR, 0);
		c1.set(Calendar.MINUTE, 0);
		c1.set(Calendar.SECOND, 10);
		
		Calendar c2 = Calendar.getInstance();
		c2.set(Calendar.YEAR, 2000);
		c2.set(Calendar.MONTH, Calendar.JANUARY);
		c2.set(Calendar.DAY_OF_MONTH, 01);
		c2.set(Calendar.HOUR, 0);
		c2.set(Calendar.MINUTE, 0);
		c2.set(Calendar.SECOND, 0);
		
		String expected = "0minutes 10seconds";
		Assert.assertEquals(expected, FreeIPAUtils.diffFormatTime(c1, c2));
	}
	
	@Test
	public void testdiffFormatTime2() {
		Calendar c1 = Calendar.getInstance();
		c1.set(Calendar.YEAR, 2000);
		c1.set(Calendar.MONTH, Calendar.JANUARY);
		c1.set(Calendar.DAY_OF_MONTH, 01);
		c1.set(Calendar.HOUR, 0);
		c1.set(Calendar.MINUTE, 30);
		c1.set(Calendar.SECOND, 0);
		
		Calendar c2 = Calendar.getInstance();
		c2.set(Calendar.YEAR, 2000);
		c2.set(Calendar.MONTH, Calendar.JANUARY);
		c2.set(Calendar.DAY_OF_MONTH, 01);
		c2.set(Calendar.HOUR, 0);
		c2.set(Calendar.MINUTE, 0);
		c2.set(Calendar.SECOND, 0);
		
		String expected = "29minutes 59seconds";
		Assert.assertEquals(expected, FreeIPAUtils.diffFormatTime(c1, c2));
	}
	
	@Test
	public void testCreateConnectorObjectUser() {
		String uid = "testLoginToConnectorObject";
		Map<String, Object> map = new HashMap<>();
		map.put("__NAME__", uid);
		map.put("__UID__", uid);
		ConnectorObject c = FreeIPAUtils.createConnectorObjectUser(map, ObjectClass.ACCOUNT);
		
		Assert.assertEquals(ObjectClass.ACCOUNT, c.getObjectClass());
		Assert.assertEquals(uid, c.getUid().getUidValue());
	}
	
	@Test
	public void testSerializeDeserialize() {
		String testString = "testStringSerializeDeserialize";
		byte[] serialized = FreeIPAUtils.serialize(testString);
		String deserialized = (String) FreeIPAUtils.deserialize(serialized);
		
		Assert.assertEquals(testString, deserialized);
	}
}
