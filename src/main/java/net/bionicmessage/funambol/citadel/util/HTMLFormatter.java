/*
 * HTMLFormatter.java
 *
 * Created on 3 September 2006, 10:45
 *
 * This program is free software: you can redistribute it and/or modify
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

package net.bionicmessage.funambol.citadel.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author matt
 */
public class HTMLFormatter extends Formatter {
    String LASTCLASS = "";
    String LASTMETHOD = "";
    /** Creates a new instance of HTMLFormatter */
    public HTMLFormatter() {
    }
    
    public String format(LogRecord record) {
        StringBuffer records = new StringBuffer();
        if (LASTCLASS.equals(record.getSourceClassName())) {
            if (LASTMETHOD.equals(record.getSourceMethodName())) {
                records.append(addMessage(record.getMessage()));
                if (record.getThrown() != null) {
                    Throwable throwed = record.getThrown();
                    addThrowed(throwed);
                }
            } else {
                records.append(addMethodName(record.getSourceMethodName()));
                records.append(addMessage(record.getMessage()));
                if (record.getThrown() != null) {
                    Throwable throwed = record.getThrown();
                    addThrowed(throwed);
                }
            }
        } else {
            LASTCLASS = record.getSourceClassName();
            LASTMETHOD = record.getSourceMethodName();
            records.append("<p><h2>");
            records.append(record.getSourceClassName());
            records.append("</h2>");
            records.append("</p>");
            records.append(addMethodName(record.getSourceMethodName()));
            records.append(addMessage(record.getMessage()));
        }
        return records.toString();
    }
    private String addMessage(String msg) {
        msg = msg.replace("<","&lt;");
        msg = msg.replace(">","&gt;");
        msg = msg.replace(":","&#58;");
        StringBuffer records = new StringBuffer();
        records.append("<p><pre>");
        records.append(msg);
        records.append("</pre></p>");
        return records.toString();
    }
    private String addThrowed(Throwable ex) {
        try {
            StringBuffer records = new StringBuffer();
            StringWriter wr = new StringWriter();
            PrintWriter pw = new PrintWriter(wr);
            ex.printStackTrace(pw);
            pw.close();
            records.append(wr.toString());
            String msg = wr.toString();
            msg = msg.replace("<","&lt;");
            msg = msg.replace(">","&gt;");
            msg = msg.replace(":","&#58;");
            return msg;
            
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return null;
    }
    private String addMethodName(String msg) {
        StringBuffer records = new StringBuffer();
        records.append("<p><h3>");
        records.append(msg);
        records.append("</h3>");
        records.append("<h4>");
        records.append(System.currentTimeMillis());
        records.append("</h4>");
        records.append("</p>");
        return records.toString();
    }
}
