package eu.bcvsolutions.idm.connector.freeipa.security;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.lang.StringBuilder;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import eu.bcvsolutions.idm.connector.freeipa.FreeIPAUtils;
import eu.bcvsolutions.idm.connector.freeipa.GuardedStringAccessor;
import eu.bcvsolutions.idm.connector.freeipa.httpclient.HttpClient;
import eu.bcvsolutions.idm.connector.freeipa.httpclient.HttpResponse;

public final class FreeIPASessionCookie {
	
		private static final Log log = Log
			.getLog(FreeIPASessionCookie.class);
	
		public static final String IPA_PASSWORD_LOGIN_PATH = "/session/login_password";
		public static final String IPA_SESSION_COOKIE_NAME = "ipa_session";
		public static final String IPA_SESSION_COOKIE_EXPIRES = "expires";
		public static final String IPA_SESSION_COOKIE_PATH = "path";
		public static final String IPA_SESSION_COOKIE_DOMAIN = "domain";
		public static final String IPA_SESSION_COOKIE_MAG_BEARER_TOKEN_NAME = "magbearertoken";

		private final String sessionCookie;
		// Note: DO NOT SEND THESE - authentication does not work for some reason if
		// you do send those cookie params
		private final String path;
		private final String domain;
		private final Calendar expirationDate;
		
		private static FreeIPASessionCookie instance;
		
		public static FreeIPASessionCookie getInstace(final String endPoint, final String userName, final GuardedString pwd) {
			synchronized (FreeIPASessionCookie.class) {
				if(!isCookieValid(instance)) {
					log.info("[FreeIPA connector] Invalid cookie - updating");
					instance = obtainAuthenticationCookie(endPoint, userName, pwd);
				}
			}
			return instance;
		}
		
		public FreeIPASessionCookie(String sessionCookie, String path,
				String domain, Calendar expirationDate) {
			super();
			this.sessionCookie = sessionCookie;
			this.path = path;
			this.domain = domain;
			this.expirationDate = expirationDate;
		}

		/**
		 * Sends request to IPA and obtains session cookie which will be used to
		 * authorize later requests
		 */
		private static FreeIPASessionCookie obtainAuthenticationCookie(String endPoint, String userName, GuardedString pwd) {
			log.info("[FreeIPA connector] authenticating endpoint = {0}", endPoint);
			HttpClient wc = HttpClient.create(endPoint + IPA_PASSWORD_LOGIN_PATH);
			// This is not nice, but creating constants for all this stuff is
			// overkill
			wc.header("accept", "text/plain");
			wc.header("referer", endPoint);
			wc.header("content-type", "application/x-www-form-urlencoded");

			HttpResponse r = wc.post(getAuthPayloadAsString(userName, pwd));

			// This is a bit wobbly... what if the name is lower case?
			List<String> newCookies = r.getSetCookieHeaders();

			if (newCookies == null) {
				throw new ConnectorException(
						"[FreeIPA connector] Cannot obtain session cookie");
			}

			return processCookieString(newCookies);
			
		}
		
		private static FreeIPASessionCookie processCookieString(List<String> cookies) {
			for (String cookie : cookies) {
				String session = null;
				String path = null;
				String domain = null;
				Calendar exp = null;
				String[] semicolonSplit = cookie.split(";");
				boolean isSessionCookie = false;
				for (String s : semicolonSplit) {
					String[] equalsSplit = s.split("=");
					if (equalsSplit.length == 2) {
						switch (equalsSplit[0].trim().toLowerCase()) {
						case IPA_SESSION_COOKIE_NAME:
							session = equalsSplit[1].trim();
							isSessionCookie = true;
							break;
						case IPA_SESSION_COOKIE_EXPIRES:
							exp = processExpirationDate(equalsSplit[1]);
							break;
						case IPA_SESSION_COOKIE_PATH:
							path = equalsSplit[1].trim();
							break;
						case IPA_SESSION_COOKIE_DOMAIN:
							domain = equalsSplit[1].trim();
							break;
						default:
							continue;
						} // END OF switch
					} else if (equalsSplit.length == 3) {
						switch (equalsSplit[0].trim().toLowerCase()) {
						case IPA_SESSION_COOKIE_NAME:
							if (IPA_SESSION_COOKIE_MAG_BEARER_TOKEN_NAME.equals(equalsSplit[1].trim().toLowerCase())) {
								StringBuilder stringBuilder = new StringBuilder(equalsSplit[1].trim());
								stringBuilder.append("=");
								stringBuilder.append(equalsSplit[2].trim());
								session = stringBuilder.toString();
								isSessionCookie = true;
							}
							break;
						case IPA_SESSION_COOKIE_PATH:
							path = equalsSplit[1].trim();
							break;
						case IPA_SESSION_COOKIE_DOMAIN:
							domain = equalsSplit[1].trim();
							break;
						default:
							continue;
						} // END OF switch
					} // END OF if
				} // END OF for
				if (isSessionCookie) {
					return new FreeIPASessionCookie(session, path, domain, exp);
				}
			}
			throw new ConnectorException(
					"[FreeIPA connector] Cannot obtain session cookie");
			
		} // END OF processCookieString

		// TODO: This probably does not work due to the time zones 
		private static Calendar processExpirationDate(String string) {
			
			DateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));

			try {
				Date d = df.parse(string);
				Calendar result = Calendar.getInstance();
				result.setTime(d);
				return result;
			} catch (ParseException e) {
				throw new ConnectorException(
						"[FreeIPA connector] Cannot parse expiration date "
								+ string
								+ ". Expected String in format E, dd MMM yyyy HH:mm:ss z");
			}
		}

		private static String getAuthPayloadAsString(String userName, GuardedString pwd) {
			GuardedStringAccessor accessor = new GuardedStringAccessor();
			pwd.access(accessor);
			char[] result = accessor.getArray();
			final String finpasswd = String.copyValueOf(result);
			return "user=" + userName + "&password=" + finpasswd;
		}

		private static boolean isCookieValid(FreeIPASessionCookie cookie) {
			if (cookie == null) {
				return false;
			}
			if (cookie.getExpirationDate() == null) {
				//TODO !!! how to check validity of MagBearerToken?
				// The Expire attribute of the cookie was not set so we expect the cookie does not 
				// expire
				return true;
			} else {
				Calendar now = Calendar.getInstance(cookie.getExpirationDate().getTimeZone());
				// Adding one minute just to be sure that cookie wont expire until request is sent 
				//now.add(1, Calendar.MINUTE);
				log.info("[FreeIPA connector] Cookie will be active for next {0}", FreeIPAUtils.diffFormatTime(cookie.getExpirationDate(), now));
				
				return now.before(cookie.getExpirationDate());
			}
		}
		
		public static void resetCookie() {
			instance = null;
		}
		
		public String getSessionCookie() {
			return sessionCookie;
		}

		public String getPath() {
			return path;
		}

		public String getDomain() {
			return domain;
		}

		public Calendar getExpirationDate() {
			return expirationDate;
		}
}
