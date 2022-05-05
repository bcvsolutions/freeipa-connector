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

import java.util.Arrays;
import java.util.List;

public class FreeIPAConstants {

	public static final String LOGIN_ATTRIBUTE = "__NAME__";
	public static final String UID_ATTRIBUTE = "uid";
	public static final String CN_ATTRIBUTE = "cn";
	public static final String NS_ACCOUNT_LOCK_ATTRIBUTE = "nsaccountlock";
	public static final String MEMBER_OF_GROUP_ATTRIBUTE = "memberof_group";
	public static final String FREE_IPA_USER_ATTRIBUTE = "user";
	public static final String FREE_IPA_CN_ATTRIBUTE = "__NAME__";
	public static final String SIZE_LIMIT_ATTRIBUTE = "sizelimit";
	public static final String TIME_LIMIT_ATTRIBUTE = "timelimit";
	public static final String RENAME_ATTRIBUTE = "rename";

	public static final String CERT_SERIAL_NUMBER_ATTRIBUTE = "certserialnumber";
	public static final String DEPARTMENT_NUMBER_ATTRIBUTE = "department";
	public static final String DESCRIPTION_ATTRIBUTE = "description";
	public static final String DISPLAY_NAME_ATTRIBUTE = "displayname";
	public static final String EMPLOYEE_NUMBER_ATTRIBUTE = "employeenumber";
	public static final String GECOS_ATTRIBUTE = "gecos";
	public static final String GID_NUMBER_ATTRIBUTE = "gidnumber";
	public static final String GIVEN_NAME__ATTRIBUTE = "givenname";
	public static final String HOME_DIRECTORY_ATTRIBUTE = "homedirectory";
	public static final String IDENTITY_NAME_ATTRIBUTE = "identityname";
	public static final String INITIALS_ATTRIBUTE = "initials";
	public static final String IPA_USER_AUTH_TYPE_ATTRIBUTE = "ipauserauthtype";
	public static final String JOB_CODE_ATTRIBUTE = "jobcode";
	public static final String L_ATTRIBUTE = "l";
	public static final String LOGIN_SHELL_ATTRIBUTE = "loginshell";
	public static final String MAIL_ATTRIBUTE = "mail";
	public static final String MAIL_HOST_ATTRIBUTE = "mailhost";
	public static final String MAIL_ROUTING_ADDRESS_ATTRIBUTE = "mailroutingaddress";
	public static final String MANAGER_ATTRIBUTE = "manager";
	public static final String MOBILE_ATTRIBUTE = "mobile";
	public static final String O_ATTRIBUTE = "o";
	public static final String OU_ATTRIBUTE = "ou";
	public static final String PAGER_ATTRIBUTE = "pager";
	public static final String PERSONAL_TITLE_ATTRIBUTE = "personaltitle";
	public static final String SHORT_UID_ATTRIBUTE = "shortuid";
	public static final String SN_ATTRIBUTE = "sn";
	public static final String TELEPHONE_NUMBER_ATTRIBUTE = "telephonenumber";
	public static final String TITLE_ATTRIBUTE = "title";
	public static final String UID_NUMBER_ATTRIBUTE = "uidnumber";
	public static final String USER_CERTIFICATE_ATTRIBUTE = "usercertificate";
	public static final String USER_PASSWORD_ATTRIBUTE = "userpassword";
	public static final String IDM_PASSWORD_ATTRIBUTE = "__PASSWORD__";
	
	public static final String MANAGED_BY_ATTRIBUTE = "managedby";
	public static final String MEMBER_USER_ATTRIBUTE = "member_user";

	public static final List<String> SPECIAL_ATTRIBUTES = Arrays
			.asList(new String[] {MEMBER_OF_GROUP_ATTRIBUTE});
	public static final int DEFAULT_TIME_LIMIT = 20;
	public static final int DEFAULT_SIZE_LIMIT = 99999;
	

	private FreeIPAConstants() {
	}

}
