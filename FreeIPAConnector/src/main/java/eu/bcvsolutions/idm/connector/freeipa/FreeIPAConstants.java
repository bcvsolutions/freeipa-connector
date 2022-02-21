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

	public static final String LOGIN_ATRIBUTE = "__NAME__";
	public static final String UID_ATRIBUTE = "uid";
	public static final String CN_ATTRIBUTE = "cn";
	public static final String DISABLED_ATRIBUTE = "disabled";
	public static final String NS_ACCOUNT_LOCK_ATRIBUTE = "disabled";
	public static final String MEMBER_OF_ATTRIBUTE = "memberof_group";
	public static final String FREE_IPA_USER_ATTRIBUTE = "user";
	public static final String FREE_IPA_CN_ATTRIBUTE = "__NAME__";
	public static final String SIZE_LIMIT_ATTRIBUTE = "sizelimit";
	public static final String TIME_LIMIT_ATTRIBUTE = "timelimit";
	public static final String RENAME_ATTRIBUTE = "rename";


	public static final List<String> SPECIAL_ATTRIBUTES = Arrays
			.asList(new String[] {MEMBER_OF_ATTRIBUTE});
	public static final int DEFAULT_TIME_LIMIT = 20;
	public static final int DEFAULT_SIZE_LIMIT = 99999;
	

	private FreeIPAConstants() {
	}

}
