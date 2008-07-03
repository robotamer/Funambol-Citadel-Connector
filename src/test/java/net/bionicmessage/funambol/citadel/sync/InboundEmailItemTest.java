/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bionicmessage.funambol.citadel.sync;

import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import java.io.File;
import java.io.FileInputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author matt
 */
public class InboundEmailItemTest {

    protected static SyncItem testItem;
    private static byte[] data = new byte[0];
    protected InboundEmailItem imi = null;

    public InboundEmailItemTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        File testFile = new File("testData/funambol-new-email.xml");
        FileInputStream fis = new FileInputStream(testFile);
        data = new byte[fis.available()+2];
        int readSoFar = fis.read(data);
        data[readSoFar++] = '\r';
        data[readSoFar++] = '\n';
        testItem = new SyncItemImpl(null, "testobj");
        testItem.setContent(data);
        String dataDebug = new String(data);
        System.err.println("Test data:");
        System.err.print(dataDebug);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        imi = new InboundEmailItem();
        imi.setSourceItem(testItem);
    }

    @After
    public void tearDown() {
    }


    @Test
    public void testGetToAddresses() throws Exception {
        assertTrue(imi.getToAddresses().contains("test@example.com"));
    }

    @Test
    public void testGetCCAddresses() throws Exception {
        assertTrue((imi.getCCAddresses().length() == 0));
    }

    @Test
    public void testGetBCCAddresses() throws Exception {
        assertTrue((imi.getBCCAddresses().length() == 0));
    }

    @Test
    public void testGetSubject() throws Exception {
        assertTrue(imi.getSubject().contains("Test message"));
    }

    @Test
    public void testGetSenderPersonName() throws Exception {
        assertTrue(imi.getSenderPersonName().equals("Mathew McBride"));
    }

    @Test
    public void testGetSenderAddress() throws Exception {
        assertTrue(imi.getSenderAddress().equals("matt@mcbridematt.dhs.org"));
    }

    @Test
    public void testGetRFC822Content() {
        assertNotNull(imi.getRFC822Content());
    }
}