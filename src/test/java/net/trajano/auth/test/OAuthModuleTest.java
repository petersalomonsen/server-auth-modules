package net.trajano.auth.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.security.auth.message.AuthException;

import javax.security.auth.message.MessagePolicy;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;

import net.trajano.auth.OAuthModule;
import net.trajano.auth.OpenIDConnectAuthModule;
import net.trajano.auth.internal.Base64;
import net.trajano.auth.internal.OpenIDProviderConfiguration;

import org.junit.Test;

/**
 * Tests the OAuthModule.
 */
public class OAuthModuleTest {

    @Test
    public void testCompressDecompress() throws Exception {

        final String payload = "foo";
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final GZIPOutputStream os = new GZIPOutputStream(baos);
        os.write(payload.toString()
                .getBytes("UTF-8"));
        os.close();
        final String encoded = Base64.encodeWithoutPadding(baos.toByteArray());

        final GZIPInputStream is = new GZIPInputStream(new ByteArrayInputStream(Base64.decode(encoded)));
        @SuppressWarnings("resource")
        final String decoded = new Scanner(is).useDelimiter("\\A")
                .next();
        is.close();
        assertEquals(payload, decoded);
    }

    /**
     * Tests getting the configuration from Google.
     *
     * @throws Exception
     */
    @Test
    public void testGoogleOpenIdConfiguration() throws Exception {

        final Map<String, String> options = new HashMap<>();
        options.put(OAuthModule.CLIENT_ID_KEY, "clientID");
        options.put(OAuthModule.CLIENT_SECRET_KEY, "clientSecret");
        options.put(OAuthModule.REDIRECTION_ENDPOINT_URI_KEY, "/someendpoint");
        options.put(OpenIDConnectAuthModule.ISSUER_URI_KEY, "https://accounts.google.com/");

        final MessagePolicy mockPolicy = mock(MessagePolicy.class);
        when(mockPolicy.isMandatory()).thenReturn(true);

        final OpenIDConnectAuthModule module = new OpenIDConnectAuthModule();
        module.initialize(mockPolicy, null, null, options);
    }

    /**
     * Tests getting the configuration from Salesforce.
     *
     * @throws Exception
     */
    @Test
    public void testSalesforceOpenIdConfiguration() throws Exception {

        final Map<String, String> options = new HashMap<>();
        options.put(OAuthModule.CLIENT_ID_KEY, "clientID");
        options.put(OAuthModule.CLIENT_SECRET_KEY, "clientSecret");
        options.put(OpenIDConnectAuthModule.ISSUER_URI_KEY, "https://login.salesforce.com");
        options.put(OAuthModule.REDIRECTION_ENDPOINT_URI_KEY, "/someendpoint");

        final MessagePolicy mockPolicy = mock(MessagePolicy.class);
        when(mockPolicy.isMandatory()).thenReturn(true);

        final OpenIDConnectAuthModule module = new OpenIDConnectAuthModule();
        module.initialize(mockPolicy, null, null, options);
    }
    
    @Test
    public void testGetRedirectionEndpointUri() throws Exception {
        class OAM extends OAuthModule {
            
            public OAM() {                
            }
            
            @Override
            public URI getRedirectionEndpointUri(HttpServletRequest req) {
                return super.getRedirectionEndpointUri(req); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            protected OpenIDProviderConfiguration getOpenIDProviderConfig(HttpServletRequest req, Client client, Map<String, String> options) throws AuthException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }            
        }
        
        OAM oam = new OAM();
        Field uriFld = OAuthModule.class.getDeclaredField("redirectionEndpointUri");
        uriFld.setAccessible(true);
        uriFld.set(oam,"/someendpoint");
        
        final HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getRequestURL())
            .thenReturn(new StringBuffer("http://localhost:4200/blablabla"));
        
        URI u = oam.getRedirectionEndpointUri(servletRequest);
        
        assertEquals("http://localhost:4200/someendpoint", u.toASCIIString());        
    }
}
