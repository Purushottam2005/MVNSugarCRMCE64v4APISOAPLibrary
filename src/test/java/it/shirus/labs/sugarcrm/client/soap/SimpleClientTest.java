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
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sugarcrm.sugarcrm.EntryList;
import com.sugarcrm.sugarcrm.EntryValue;
import com.sugarcrm.sugarcrm.GetEntryListResultVersion2;
import com.sugarcrm.sugarcrm.LinkNameToFieldsArray;
import com.sugarcrm.sugarcrm.LinkNamesToFieldsArray;
import com.sugarcrm.sugarcrm.ModuleList;
import com.sugarcrm.sugarcrm.ModuleListEntry;
import com.sugarcrm.sugarcrm.NameValue;
import com.sugarcrm.sugarcrm.NameValueList;
import com.sugarcrm.sugarcrm.SelectFields;
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
		p.load((new ClassPathResource("SimpleClientTest.properties"))
				.getInputStream());
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

		System.out
				.println("#--------------------------------------------------------------#");
		System.out.println("# Invoking Server Info...");
		System.out
				.println("#--------------------------------------------------------------#");
		System.out.println("# Version: " + port.getServerInfo().getVersion());
		System.out.println("# Flavor: " + port.getServerInfo().getFlavor());

		assertEquals("Version", "6.5.0", port.getServerInfo().getVersion());
		assertEquals("Flavor", "CE", port.getServerInfo().getFlavor());
		System.out
		.println("#--------------------------------------------------------------#");

		/**
		 * Try to login on SugarCRM
		 * 
		 * 1) Prepare a MD5 hash password 2) Prepare a User Auth object 3)
		 * Execute login
		 */
		{
			System.out.println();
			System.out
					.println("#--------------------------------------------------------------#");
			System.out.println("# Invoking login...");
			System.out
					.println("#--------------------------------------------------------------#");

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
				System.out.println("# Login Successfully for "
						+ p.getProperty("sugarcrm.username"));
				System.out
						.println("# Your session Id: " + _login__return.getId());
				sessionID = _login__return.getId();
				System.out
				.println("#--------------------------------------------------------------#");
			} catch (Exception e) {
				fail("Login failed. Message: " + e.getMessage());
			}
		}

		/**
		 * Try get entry list operation
		 */
		{
			System.out.println();
			System.out
					.println("#--------------------------------------------------------------#");
			System.out.println("# Invoking Get Account Entry List...");

			try {
				SelectFields selectFields = new SelectFields();
				LinkNamesToFieldsArray linkNamesToFieldsArray = new LinkNamesToFieldsArray();

				Document doc = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder().newDocument();

				Element fieldId = doc.createElement("item");
				fieldId.setTextContent("id");

				Element fieldName = doc.createElement("item");
				fieldName.setTextContent("name");

				Element fieldBillingCity = doc.createElement("item");
				fieldBillingCity.setTextContent("billing_address_city");

				selectFields.setArrayType("xsd:string[]");
				selectFields.getAny().add((Element) fieldId);
				selectFields.getAny().add((Element) fieldName);
				selectFields.getAny().add((Element) fieldBillingCity);

				linkNamesToFieldsArray
						.setArrayType("ns1:link_name_to_fields_array[]");

				GetEntryListResultVersion2 resultList = port.getEntryList(
						sessionID, "Accounts", "", "", 5, selectFields,
						linkNamesToFieldsArray, 3, 0, false);

				System.out.println("# Total Count: "
						+ resultList.getTotalCount());
				System.out.println("# Result Count: "
						+ resultList.getResultCount());
				System.out
						.println("#--------------------------------------------------------------#");

				EntryList entryList = resultList.getEntryList();
				List<EntryValue> entryValue = entryList.getItem();

				for (EntryValue entry : entryValue) {
					System.out
							.println("#--------------------------------------------------------------#");
					NameValueList nameValueList = entry.getNameValueList();
					List<NameValue> nameValueItem = nameValueList.getItem();

					System.out.println("# Account Entry ID: " + entry.getId());

					for (NameValue nameValue : nameValueItem) {
						System.out.println("# Account Field Name: "
								+ nameValue.getName());
						System.out.println("# Account Field Value: "
								+ nameValue.getValue());
					}

					System.out
							.println("#--------------------------------------------------------------#");

				}
			} catch (Exception e) {
				fail("Get Entry failed: " + e.getMessage());
			}
		}

		/**
		 * Try get modules
		 */
		{
			System.out.println();
			System.out
					.println("#--------------------------------------------------------------#");
			System.out.println("# Invoking Get Module List...");
			System.out
					.println("#--------------------------------------------------------------#");

			try {
				ModuleList moduleList = port.getAvailableModules(sessionID, "");
				List<ModuleListEntry> module = moduleList.getModules()
						.getItem();

				for (ModuleListEntry moduleListEntry : module) {
					System.out
					.println("#--------------------------------------------------------------#");
					System.out.println("# Module Key:"
							+ moduleListEntry.getModuleKey());
					System.out.println("# Module Label:"
							+ moduleListEntry.getModuleLabel());
					System.out
					.println("#--------------------------------------------------------------#");
				}

			} catch (Exception e) {
				fail("Get Modules failed: " + e.getMessage());
			}
		}

	}
}
