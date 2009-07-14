/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bionicmessage.funambol.citadel.sync;

import com.funambol.email.pdi.converter.MailToXML;
import com.funambol.email.pdi.mail.Email;
import com.funambol.email.pdi.mail.EmailItem;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.source.SyncSource;
import java.util.Date;
import net.bionicmessage.funambol.citadel.store.CitadelMailObject;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.bionicmessage.funambol.citadel.store.CitadelPart;

/**
 *
 * @author matt
 */
public class OutboundEmailItem {
    /* For <created> tag */

    static private SimpleDateFormat fmt =
            new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    protected CitadelMailObject mailObject = null;
    protected SyncItem si = null;
    protected int cropAtBytes = -1;
    protected boolean addAttachments = false;

    public OutboundEmailItem() {
    }

    public void setMailObject(CitadelMailObject cmo) {
        mailObject = cmo;
    }

    public CitadelMailObject getMailObject() {
        return mailObject;
    }

    public SyncItem getSyncItem(SyncSource originator,
            SyncItemKey key,
            SyncItemKey parent) throws Exception {
        si = new SyncItemImpl(originator, key, parent.getKeyAsString(), SyncItemState.NEW);
        Email eml = new Email();
        eml.getParentId().setPropertyValue(parent.getKeyValue());
        if (mailObject != null && mailObject.hasData()) {
            String rfc822 = CitadelToRFC822.convertToRFC822(mailObject, cropAtBytes, addAttachments);
            eml.getEmailItem().setPropertyValue(rfc822);
        }
        boolean read = mailObject.isSeen();
        eml.getRead().setPropertyValue(Boolean.toString(read)); // for now
        eml.getForwarded().setPropertyValue("false");
        eml.getDeleted().setPropertyValue("false");
        eml.getReplied().setPropertyValue("false");
        eml.getFlagged().setPropertyValue("false");
        Date created = mailObject.getTime();
        String dtCreated = fmt.format(created);
        eml.getCreated().setPropertyValue(dtCreated);
        MailToXML mtx = new MailToXML(null, "UTF-8");
        String content = mtx.convert(eml);
        si.setContent(content.getBytes("UTF-8"));
        si.setFormat("UTF-8");
        si.setType("application/vnd.omads-email+xml");
        return si;
    }

    public void setCropAtBytes(int cropAtBytes) {
        this.cropAtBytes = cropAtBytes;
    }

    public boolean doAttachmentsFitInsideCrop() {
        if (cropAtBytes > -1) {
            int totalSize = mailObject.getData().length();
            List<CitadelPart> partList = mailObject.getAttachedParts();
            Iterator<CitadelPart> partIterator = partList.iterator();
            while (partIterator.hasNext()) {
                CitadelPart part = partIterator.next();
                totalSize = totalSize + part.getPartSize();
            }
            return (totalSize < cropAtBytes);
        }
        return true;
    }

    public void setAddAttachments(boolean addAttachments) {
        this.addAttachments = addAttachments;
    }
}
