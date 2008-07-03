/* This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.bionicmessage.funambol.citadel.sync;

import com.funambol.common.pim.common.Property;
import com.funambol.email.util.Def;
import com.funambol.framework.core.AlertCode;
import com.funambol.framework.core.PropParam;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.source.ContentType;
import com.funambol.framework.engine.source.SyncContext;
import com.funambol.framework.engine.source.SyncSourceInfo;
import com.funambol.framework.filter.Clause;
import com.funambol.framework.filter.FieldClause;
import com.funambol.framework.filter.FilterClause;
import com.funambol.framework.filter.LogicalClause;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.tools.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import net.bionicmessage.funambol.citadel.store.CtdlFnblConstants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A test class for CitadelSyncSource
 * @author matt
 */
public class CitadelSyncSourceTest {

    public static final String CONFIG_FILE_PATH = ".citadelsyncsource_test_properties";
    SyncContext ctx = null;
    static Properties testProps = null;
    CitadelSyncSource css = null;
    Sync4jPrincipal spp = null;

    public CitadelSyncSourceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        String testPropFilePath = String.format("%s%s%s",
                System.getProperty("user.home"),
                File.separator,
                CONFIG_FILE_PATH);
        File testPropertiesFile = new File(testPropFilePath);
        if (!testPropertiesFile.exists()) {
            System.err.format("Cannot execute tests as the config file\n%s",
                    testPropFilePath);
            System.err.println(" does not exist. See the documentation for information");
            throw new Exception("Testcases are not configured");
        }
        testProps = new Properties();
        testProps.load(new FileInputStream(testPropertiesFile));
        String storeLocation = String.format("%s%c%s",
                System.getProperty("java.io.tmpdir"),
                File.separatorChar,
                "citadelemailsynctest");
        testProps.setProperty(CtdlFnblConstants.STORE_LOC,
                storeLocation);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        css = new CitadelSyncSource();
        // Keep test properties and sync source properties separate
        Properties syncSourceProperties = new Properties();
        syncSourceProperties.setProperty(CtdlFnblConstants.SERVER_HOST,
                testProps.getProperty(CtdlFnblConstants.SERVER_HOST));
        syncSourceProperties.setProperty(CtdlFnblConstants.SERVER_PORT,
                testProps.getProperty(CtdlFnblConstants.SERVER_PORT));
        syncSourceProperties.setProperty(CtdlFnblConstants.ROOM_BASE + "Mail",
                testProps.getProperty(CtdlFnblConstants.ROOM_BASE + "Mail"));
        syncSourceProperties.setProperty(CtdlFnblConstants.STORE_LOC,
                testProps.getProperty(CtdlFnblConstants.STORE_LOC));
        css.setSyncSourceProperties(syncSourceProperties);
        // Set up a Sync4jPrincipal
        spp = spp.createPrincipal(
                testProps.getProperty(CtdlFnblConstants.USER_NAME),
                "testDevice");
        String authString = String.format("%s:%s",
                testProps.getProperty(CtdlFnblConstants.USER_NAME),
                testProps.getProperty(CtdlFnblConstants.USER_PASS));
        byte[] b64auth = Base64.encode(authString.getBytes("UTF-8"));
        spp.setEncodedUserPwd(new String(b64auth, "UTF-8"));
        spp.getUser().setPassword(
                testProps.getProperty(CtdlFnblConstants.USER_PASS));
        SyncSourceInfo ssInfo = new SyncSourceInfo();
        ContentType[] ctypes = new ContentType[]{
            new ContentType("application/vnd.omads-email+xml", "1.2"),
            new ContentType("application/vnd.omads-folder+xml", "1.2")
        };
        ssInfo.setSupportedTypes(ctypes);
        css.setInfo(ssInfo);
    }

    @After
    public void tearDown() {
    }

    /* Settings:
     * Filter: none
     * Sync mode: slow
     */
    @Test
    public void syncAndGetAllSyncItemKeys() throws Exception {
        FilterClause cls = new FilterClause();
        ctx = new SyncContext(spp, AlertCode.SLOW,
                cls,
                null,
                0);
        css.beginSync(ctx);
        SyncItemKey[] allKeys = css.getAllSyncItemKeys();
        for (int i = 0; i < allKeys.length; i++) {
            SyncItemKey key = allKeys[i];
            System.out.format("Key: %s\n", key.getKeyAsString()).flush();
        }
        css.commitSync();
        css.endSync();
    }
    /* Settings:
     * Filter: none
     * Sync mode: slow
     */

   @Test
    public void syncAndGetAllSyncItems() throws Exception {
        FilterClause cls = new FilterClause();
        ctx = new SyncContext(spp, AlertCode.SLOW,
                cls,
                null,
                0);
        css.beginSync(ctx);
        SyncItemKey[] allKeys = css.getAllSyncItemKeys();
        for (int i = 0; i < allKeys.length; i++) {
            SyncItemKey key = allKeys[i];
            System.out.format("Key: %s\n", key.getKeyAsString()).flush();
            SyncItem itm = css.getSyncItemFromId(key);
            String data = new String(itm.getContent());
            System.out.print(data);

        }
        css.commitSync();
        css.endSync();
    }

    //@Test
    public void testWithFilterAndAttachments() throws Exception {
        FilterClause cls = new FilterClause();
        LogicalClause lc = new LogicalClause();
        FieldClause fc = new FieldClause();
        com.funambol.framework.core.Property fprop = 
                new com.funambol.framework.core.Property();
        fc.setProperty(fprop);
        fc.getProperty().setPropName(Def.FILTER_SIZE_LABEL_HEADER);
        fc.getProperty().setMaxSize(20*1024);
        PropParam enableAttach = new PropParam();
        enableAttach.setDataType(Def.FILTER_SIZE_LABEL_ATTACH);
        PropParam enableBodySize = new PropParam();
        enableBodySize.setDataType(Def.FILTER_SIZE_LABEL_BODY);
        fc.getProperty().getPropParams().add(enableAttach);
        fc.getProperty().getPropParams().add(enableBodySize);
        lc.setOperands(new Clause[]{fc});
        cls.setClause(lc);
        ctx = new SyncContext(spp, AlertCode.SLOW,
                cls,
                null,
                0);
        css.beginSync(ctx);
        SyncItemKey[] allKeys = css.getAllSyncItemKeys();
        for (int i = 0; i < allKeys.length; i++) {
            SyncItemKey key = allKeys[i];
            System.out.format("Key: %s\n", key.getKeyAsString()).flush();
            SyncItem itm = css.getSyncItemFromId(key);
            String data = new String(itm.getContent());
            System.out.print(data);

        }
        css.commitSync();
        css.endSync();
    }
}
