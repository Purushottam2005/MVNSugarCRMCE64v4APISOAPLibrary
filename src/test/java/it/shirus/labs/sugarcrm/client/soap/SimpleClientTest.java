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
import java.util.Iterator;
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
import com.sugarcrm.sugarcrm.LinkArrayList;
import com.sugarcrm.sugarcrm.LinkList;
import com.sugarcrm.sugarcrm.LinkList2;
import com.sugarcrm.sugarcrm.LinkLists;
import com.sugarcrm.sugarcrm.LinkNameToFieldsArray;
import com.sugarcrm.sugarcrm.LinkNameValue;
import com.sugarcrm.sugarcrm.LinkNamesToFieldsArray;
import com.sugarcrm.sugarcrm.LinkValue2;
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
	private static URL wsdlURL;
	private static String sessionID = null;
	private static Sugarsoap ss = null;
	private static SugarsoapPortType port;

	private Properties p = new Properties();

	@Before
	public void setUp() throws FileNotFoundException, IOException {
		p.load((new ClassPathResource("SimpleClientTest.properties"))
				.getInputStream());

		try {
			wsdlURL = new URL(p.getProperty("sugarcrm.WSDL"));
		} catch (MalformedURLException e) {
			fail("WSDL Url failed: " + e.getMessage());
		}

		ss = new Sugarsoap(wsdlURL, SERVICE_NAME);
		port = ss.getSugarsoapPort();

	}

	@Test
	public void getServerInfo() {
		System.out
				.println("#--------------------------------------------------------------#");
		System.out.println("# Invoking Server Info...");
		System.out
				.println("#--------------------------------------------------------------#");
		System.out.println("# Version: " + port.getServerInfo().getVersion());
		System.out.println("# Flavor: " + port.getServerInfo().getFlavor());

		assertEquals("Version mismatch", "6.5.0", port.getServerInfo()
				.getVersion());
		assertEquals("Edition mismatch", "CE", port.getServerInfo().getFlavor());
		System.out
				.println("#--------------------------------------------------------------#");
	}

	@Test
	public void login() {
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
			MessageDigest messageDiget = null;
			try {
				messageDiget = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e1) {
				fail(e1.getMessage());
			}
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
				System.out.println("# Your session Id: "
						+ _login__return.getId());
				sessionID = _login__return.getId();
				assertTrue("Invalid len SessionId", sessionID.length() == 26);
				System.out
						.println("#--------------------------------------------------------------#");
			} catch (Exception e) {
				fail("Login failed. Message: " + e.getMessage());
			}
		}
	}

	@Test
	public void getAvailableModules() {
		System.out.println();
		System.out
				.println("#--------------------------------------------------------------#");
		System.out.println("# Invoking Get Module List...");
		System.out.println("# SessionId: " + sessionID);
		System.out
				.println("#--------------------------------------------------------------#");

		try {
			ModuleList moduleList = port.getAvailableModules(sessionID, "");
			List<ModuleListEntry> module = moduleList.getModules().getItem();

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

	@Test
	public void getEntryList() {
		System.out.println();
		System.out
				.println("#--------------------------------------------------------------#");
		System.out.println("# Invoking Get Account Entry List...");
		System.out.println("# SessionId: " + sessionID);

		try {
			SelectFields entrySelectFields = new SelectFields();
			{

				Document docEntrySelectFields = DocumentBuilderFactory
						.newInstance().newDocumentBuilder().newDocument();

				Element fieldId = docEntrySelectFields.createElement("item");
				fieldId.setTextContent("id");

				Element fieldName = docEntrySelectFields.createElement("item");
				fieldName.setTextContent("name");

				Element fieldBillingCity = docEntrySelectFields
						.createElement("item");
				fieldBillingCity.setTextContent("billing_address_city");

				entrySelectFields.setArrayType("ns1:select_fields_array[]");
				entrySelectFields.getAny().add((Element) fieldId);
				entrySelectFields.getAny().add((Element) fieldName);
				entrySelectFields.getAny().add((Element) fieldBillingCity);
			}

			LinkNamesToFieldsArray linkNamesToFieldsArray = new LinkNamesToFieldsArray();
			{
				SelectFields linkSelectFields = new SelectFields();

				Document docLinkSelectFields = DocumentBuilderFactory
						.newInstance().newDocumentBuilder().newDocument();

				Element fieldId = docLinkSelectFields.createElement("item");
				fieldId.setTextContent("id");

				Element fieldName = docLinkSelectFields.createElement("item");
				fieldName.setTextContent("name");

				Element fieldEmail = docLinkSelectFields
						.createElement("item");
				fieldEmail.setTextContent("email1");

				linkSelectFields.setArrayType("xsd:string[]");
				linkSelectFields.getAny().add((Element) fieldId);
				linkSelectFields.getAny().add((Element) fieldName);
				linkSelectFields.getAny().add((Element) fieldEmail);

				LinkNameToFieldsArray linkNameToFieldsArray = new LinkNameToFieldsArray();
				linkNameToFieldsArray.setName("contacts");
				linkNameToFieldsArray.setValue(linkSelectFields);

				linkNamesToFieldsArray
						.setArrayType("ns1:link_name_to_fields_array[]");
				linkNamesToFieldsArray.getAny().add(
						(LinkNameToFieldsArray) linkNameToFieldsArray);
			}


			GetEntryListResultVersion2 resultList = port.getEntryList(
					sessionID, "Accounts", "", "", 5, entrySelectFields,
					linkNamesToFieldsArray, 3, 0, false);

			System.out.println("# Total Count: " + resultList.getTotalCount());
			System.out
					.println("# Result Count: " + resultList.getResultCount());
			System.out
					.println("#--------------------------------------------------------------#");

			EntryList entryList = resultList.getEntryList();
			LinkLists relationshipList = resultList.getRelationshipList();
			
			List<EntryValue> entryValue = entryList.getItem();

			// Get Entry List
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
			
			// Get Relationship list
			System.out
			.println("#--------------------------------------------------------------#");
			System.out
			.println("# Get related Contact for Account                              #");
			System.out
			.println("#--------------------------------------------------------------#");
			List<LinkList2> relationships = relationshipList.getItem();
			for (LinkList2 relationship : relationships) {
				LinkList linkListItem = relationship.getLinkList();
				List<LinkNameValue> nameValueItem = linkListItem.getItem();
				for (LinkNameValue linkNameValue : nameValueItem) {
					LinkArrayList records = linkNameValue.getRecords();
					List<LinkValue2> nameValueItemRecord = records.getItem();
					for (LinkValue2 linkValue2 : nameValueItemRecord) {
						for (Iterator<NameValue> iterator = linkValue2.getLinkValue().getItem().iterator() ; iterator
								.hasNext();) {
							NameValue nameValueItemRecord1 = (NameValue) iterator
									.next();
							System.out.println("# Contact Field Name: "
									+ nameValueItemRecord1.getName());
							System.out.println("# Contact Field Value: "
									+ nameValueItemRecord1.getValue());
						}
					}
				}
			}
			System.out
			.println("#--------------------------------------------------------------#");
		} catch (Exception e) {
			fail("Get Entry failed: " + e.getMessage());
		}
	}
}
