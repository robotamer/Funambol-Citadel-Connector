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

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import net.bionicmessage.funambol.citadel.store.CitadelMailObject;
import net.bionicmessage.funambol.citadel.store.CitadelPart;

/**
 * Conversion class from CitadelMailObject to RFC2822 message
 * @author matt
 */
public class CitadelToRFC822 {

    public static String convertToRFC822(CitadelMailObject cmo,
            int cropTextAt,
            boolean allowAttachments) throws Exception {
        if (!cmo.hasData()) {
            throw new IllegalArgumentException("Submitted mail object has no data part");
        }
        MimeMultipart mpart = new MimeMultipart();
        MimeMessage mmessage = new MimeMessage((javax.mail.Session) null);
        InternetAddress fromAddr = new InternetAddress();
        fromAddr.setPersonal(cmo.getFrom());
        String rfca = (String) cmo.getProperties().get("rfca");
        if (rfca == null) {
            // try from + node instead
            String node = (String) cmo.getProperties().get("path");
            String from = cmo.getFrom() + "@" + node;
            fromAddr.setAddress(from);
        } else {
            fromAddr.setAddress((String) cmo.getProperties().get("rfca"));
        }
        mmessage.setFrom(fromAddr);
        mmessage.setSentDate(cmo.getTime());
        MimeBodyPart contentPart = new MimeBodyPart();
        String data = cmo.getData();
        if (cropTextAt > 0 && data.length() > cropTextAt) {
            data = data.substring(0, cropTextAt);
        }
        contentPart.setContent(data, "text/plain"); // todo: allow other types
        if (cmo.getSubject() != null) {
            mmessage.setSubject(cmo.getSubject());
        }
        mpart.addBodyPart(contentPart);
        if (allowAttachments) {
            Iterator<CitadelPart> partIterator = cmo.getAttachedParts().iterator();
            while (partIterator.hasNext()) {
                CitadelPart part = partIterator.next();
                MimeBodyPart attachment = new MimeBodyPart();
                ByteArrayDataSource ds = new ByteArrayDataSource(part.getPartData(), part.getPartType());
                attachment.setDataHandler(new DataHandler(ds));
                attachment.setFileName(part.getPartName());
                attachment.setDisposition("attachment");
                mpart.addBodyPart(attachment);
            }
        }
        mmessage.setContent(mpart);
        ByteArrayOutputStream rfcStream = new ByteArrayOutputStream();
        mmessage.writeTo(rfcStream);
        return rfcStream.toString();
    }
}
