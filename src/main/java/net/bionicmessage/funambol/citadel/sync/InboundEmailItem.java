/*
 * Funambol Citadel Connector
 * (C) Mathew McBride 2007-2008
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
 * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Funambol" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Funambol".
 */
package net.bionicmessage.funambol.citadel.sync;

import com.funambol.common.pim.converter.ConverterException;
import com.funambol.email.pdi.converter.MailToXML;
import com.funambol.email.pdi.mail.Email;
import com.funambol.email.pdi.mail.EmailItem;
import com.funambol.email.pdi.parser.XMLMailParser;
import com.funambol.email.util.Def;
import com.funambol.framework.engine.SyncItem;
import com.funambol.email.util.Utility;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.source.SyncSource;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

/**
 *
 * @author matt
 */
public class InboundEmailItem {

    protected SyncItem sourceItem = null;
    protected String rfc822Contents = null;
    protected MimeMessage mimeMessage = null;
    protected Email eml = null;

    public InboundEmailItem() {
    }

    public void setSourceItem(SyncItem sourceItem) throws Exception {
        this.sourceItem = sourceItem;
        init();
    }

    public SyncItem getSourceItem() {
        return sourceItem;
    }

    private void init() throws Exception {
        String content = Utility.getContentFromSyncItem(sourceItem);
        content = content.replace("&gt;", ">"); // fix cdata output bug
        ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes());
        XMLMailParser xmp = new XMLMailParser(bis);
        eml = xmp.parse();
        EmailItem ei = eml.getEmailItem();
        rfc822Contents = ei.getStringRFC2822();
        ByteArrayInputStream rfc822 = new ByteArrayInputStream(rfc822Contents.getBytes("UTF-8"));
        mimeMessage = new MimeMessage(null, rfc822);
    }

    public String getAddresses(javax.mail.Message.RecipientType type) throws MessagingException {
        Address[] toRecips = mimeMessage.getRecipients(type);
        StringBuffer recipsString = new StringBuffer();
        if (toRecips != null) {
            for (int i = 0; i < toRecips.length; i++) {
                if (i > 0) {
                    recipsString.append(",");
                }
                Address address = toRecips[i];
                if (address instanceof InternetAddress) {
                    InternetAddress ia = (InternetAddress) address;
                    recipsString.append(ia.getAddress());
                }
            }
        }
        return recipsString.toString();
    }

    public String getToAddresses() throws MessagingException {
        return getAddresses(javax.mail.Message.RecipientType.TO);
    }

    public String getCCAddresses() throws MessagingException {
        return getAddresses(javax.mail.Message.RecipientType.CC);
    }

    public String getBCCAddresses() throws MessagingException {
        return getAddresses(javax.mail.Message.RecipientType.BCC);
    }

    public String getSubject() throws MessagingException {
        return mimeMessage.getSubject();
    }

    public String getSenderPersonName() throws MessagingException {
        Address ad = mimeMessage.getFrom()[0];
        if (ad instanceof InternetAddress) {
            return ((InternetAddress) ad).getPersonal();
        } else {
            return "";
        }
    }

    public String getSenderAddress() throws MessagingException {
        Address ad = mimeMessage.getFrom()[0];
        if (ad instanceof InternetAddress) {
            return ((InternetAddress) ad).getAddress();
        } else {
            return ad.toString();
        }
    }

    public String getRFC822Content() {
        return rfc822Contents;
    }

    public SyncItem generateAckSyncItem(SyncSource syncSource)
            throws ConverterException, UnsupportedEncodingException {
        String newUid = Long.toString(System.currentTimeMillis());
        eml.getUID().setPropertyValue(newUid);
        eml.getParentId().setPropertyValue("ROOT/S");
        MailToXML etx = new MailToXML(null, "UTF-8");
        String itmContent = etx.convert(eml);
        byte[] content = itmContent.getBytes("UTF-8");
        SyncItem si = new SyncItemImpl(syncSource, newUid);
        si.setContent(content);
        si.setType(Def.TYPE_EMAIL);
        si.setState(SyncItemState.NEW);
        return si;
    }
}