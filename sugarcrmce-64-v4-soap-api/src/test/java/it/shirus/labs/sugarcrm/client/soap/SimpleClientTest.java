/**
 * 
 */
package it.shirus.labs.sugarcrm.client.soap;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.sugarcrm.sugarcrm.NameValueList;
import com.sugarcrm.sugarcrm.Sugarsoap;
import com.sugarcrm.sugarcrm.SugarsoapPortType;
import com.sugarcrm.sugarcrm.UserAuth;

/**
 * @author amusarra
 * 
 */
public class SimpleClientTest {

	private static final QName SERVICE_NAME = new QName(
			"http://www.sugarcrm.com/sugarcrm", "sugarsoap");
	
	private static final String APPLICATION_NAME = Class.class.getName();
	private URL wsdlURL;
	private String sessionID = null;
	
	private Properties p = new Properties();

	@Before
	public void setUp() throws FileNotFoundException, IOException {
		p.load((new ClassPathResource("SimpleClientTest.properties")).getInputStream());
	}

	@Test
	public void simpleSugarCRMProcess() throws NoSuchAlgorithmException {
		try {
			wsdlURL = new URL(p.getProperty("sugarcrm.WSDL"));
		} catch (MalformedURLException e) {
			fail("WSDL Url failed: " + e.getMessage());
		}
		
		Sugarsoap ss = new Sugarsoap(wsdlURL, SERVICE_NAME);
		SugarsoapPortType port = ss.getSugarsoapPort();

		System.out.println("SugarCRM Server Info...");
		assertEquals("Version","6.4.0", port.getServerInfo().getVersion());
		assertEquals("Flavor","CE", port.getServerInfo().getFlavor());
		System.out.println("SugarCRM Server Info...[ OK ]");
		
		/**
		 * Try to login on SugarCRM
		 * 
		 * 1) Prepare a MD5 hash password 
		 * 2) Prepare a User Auth object 
		 * 3) Execute login
		 */
		{
			System.out.println("Invoking login...");

			// 1. Prepare a MD5 hash password
			MessageDigest messageDiget = MessageDigest.getInstance("MD5");
			messageDiget.update(p.getProperty("sugarcrm.password").getBytes());

			// 2. Prepare a User Auth object
			com.sugarcrm.sugarcrm.UserAuth _login_userAuth = new UserAuth();
			_login_userAuth.setUserName(p.getProperty("sugarcrm.username"));
			_login_userAuth.setPassword((new BigInteger(1, messageDiget
					.digest())).toString(16));
			java.lang.String _login_applicationName = APPLICATION_NAME;
			com.sugarcrm.sugarcrm.NameValueList _login_nameValueList = new NameValueList();	

			try {
				// 3. Execute login
				com.sugarcrm.sugarcrm.EntryValue _login__return = port.login(
						_login_userAuth, _login_applicationName,
						_login_nameValueList);
				System.out.println("Login Successfully for " + p.getProperty("sugarcrm.username"));
				System.out.println("Your session Id: " + _login__return.getId());
				sessionID = _login__return.getId();
			} catch (Exception e) {
				fail("Login failed. Message: " + e.getMessage());
			}
		}
	}
}
