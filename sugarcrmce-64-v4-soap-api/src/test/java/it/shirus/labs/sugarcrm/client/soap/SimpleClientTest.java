/**
 * 
 */
package it.shirus.labs.sugarcrm.client.soap;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.sugarcrm.sugarcrm.Sugarsoap;
import com.sugarcrm.sugarcrm.SugarsoapPortType;

/**
 * @author amusarra
 * 
 */
public class SimpleClientTest {

	private static final QName SERVICE_NAME = new QName(
			"http://www.sugarcrm.com/sugarcrm", "sugarsoap");
	private URL wsdlURL;
	
	private Properties p = new Properties();

	@Before
	public void setUp() throws FileNotFoundException, IOException {
		p.load((new ClassPathResource("SimpleClientTest.properties")).getInputStream());
	}

	@Test
	public void simpleSugarCRMProcess() {
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
	}

}
