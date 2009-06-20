/*
 * JCitadel 
 * Copyright (c) 2007-2008 Mathew McBride <matt@mcbridematt.dhs.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package org.citadel.lite;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author matt
 */
public class CitadelToolkit {

    static final byte[] endSeq = "000\r\n".getBytes();
    private Socket serverSocket;
    private InputStream serverInputStream;
    private OutputStream serverOutputStream;
    private static final String MSGS = "MSGS\n";
    private static final String MSGS_NEW = "MSGS NEW\n";
    private static byte[] MSGS_NEW_B = null;
    private static final String MSGS_OLD = "MSGS OLD\n";
    private static byte[] MSGS_OLD_B = null;
    private static final short NEW_MESSAGE = 1;
    private static final short OLD_MESSAGE = 0;

    static {
        try {
            MSGS_NEW_B = MSGS_NEW.getBytes("UTF-8");
            MSGS_OLD_B = MSGS_OLD.getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static final String MSGP = "MSGP ";
    private int newline = 10;
    private String user = null;
    private String pass = null;
    private int totalMsgsInCurRoom = 0;

    /** Function inspired by webcit. We can't use BufferedReader.readLine()
     * since on some environments (IBM..) it barfs on non-unicode bytes 
     */
    private String serv_getln() throws IOException {
        int c = 0;
        byte[] buf = new byte[4096];
        int i = 0;
        do {
            c = serverInputStream.read();
            if (c != 10 && c != 13 && c != -1) {
                buf[i++] = (byte) c;
            }
        } while (c != 10 && c != -1);
        return new String(buf, 0, i, "UTF-8");
    }

    public CitadelToolkit() {
    }

    public String login(String user, String pass) {
        try {
            this.user = user;
            this.pass = pass;
            String sendUser = "USER " + user + "\n";
            serverOutputStream.write(sendUser.getBytes("UTF-8"));
            String response = serv_getln();
            if (!response.substring(0, 3).equals("300")) {
                System.out.print("Received ");
                System.out.print(response);
                System.out.println(" instead of 300");
                return null;
            }
            String sendPass = "PASS " + pass + "\n";
            serverOutputStream.write(sendPass.getBytes("UTF-8"));
            response = serv_getln();
            System.out.println(response);
            return response;
        } catch (IOException ex) {
            Logger.getLogger(CitadelToolkit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String gotoRoom(String room) {
        try {
            String gotoRoom = "GOTO " + room + "\n";
            serverOutputStream.write(gotoRoom.getBytes("UTF-8"));
            String response = serv_getln();
            // Parse the response to get some data out of it
            String[] params = response.split("|");
            String totalMsgCount = params[2];
            totalMsgsInCurRoom = Integer.parseInt(totalMsgCount);
            return response;
        } catch (IOException ex) {
            Logger.getLogger(CitadelToolkit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    public int getMessageCountForRoom() {
        return totalMsgsInCurRoom;
    }
    public List getMessagesInRoom() {
        try {
            serverOutputStream.write(MSGS.getBytes("UTF-8"));
            String initialResponse = serv_getln();
            if (!initialResponse.substring(0, 3).equals("100")) {
                return null;
            }
            ArrayList list = new ArrayList();
            String resp = null;
            while (true) {
                resp = serv_getln();
                if (resp.equals("000")) {
                    break;
                }
                list.add(resp);
            }
            return list;
        } catch (IOException ex) {
            Logger.getLogger(CitadelToolkit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void getMessgesInRoomWithSeen(CitadelCallback callback) {
        int curArrayNum = 0;
        for (short i = 0; i < 2; i++) {
            byte[] thisCycle = (i == 0) ? MSGS_NEW_B : MSGS_OLD_B;
            try {
                serverOutputStream.write(thisCycle);
                String initalResponse = serv_getln();
                // TODO: Error check
                while(true) {
                    String resp = serv_getln();
                    if (resp.equals("000")) {
                       break;
                    } else {
                        short msgStatus = (i==0) ? NEW_MESSAGE : OLD_MESSAGE;
                        callback.message(resp, msgStatus);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(CitadelToolkit.class.getName()).log(Level.SEVERE, null, ex);
            }
            callback.finishedList();
        }
    }

    public boolean setPreferredType(String preferred) throws IOException {
        serverOutputStream.write(MSGP.getBytes("UTF-8"));
        serverOutputStream.write(preferred.getBytes("UTF-8"));
        serverOutputStream.write(newline);
        String response = serv_getln();
        if (!response.substring(0, 3).equals("200")) {
            return false;
        }
        return true;
    }

    public CtdlMessage getMessage4(String msgId) throws CitadelException, IOException {
        String getMsg4 = "MSG4 " + msgId + "\n";
        serverOutputStream.write(getMsg4.getBytes("UTF-8"));
        String resp = serv_getln();
        if (isErrorMsg(resp)) {
            throw new CitadelException(resp);
        }
        CtdlMessage msg = new CtdlMessage();
        while (true) {
            resp = serv_getln();
            if (resp.equals("000")) {
                break;
            }
            msg.parserFeedLine(resp);
        }
        return msg;
    }

    public CtdlMessage getMessageHeaders(String msgId) {
        try {
            String getMsg4 = "MSG0 " + msgId + "|1\n";
            serverOutputStream.write(getMsg4.getBytes("UTF-8"));
            String resp = serv_getln();
            if (!resp.substring(0, 3).equals("100")) {
                return null; // Failed
            }
            CtdlMessage msg = new CtdlMessage();
            while (true) {
                resp = serv_getln();
                if (resp.equals("000")) {
                    break;
                }
                msg.parserFeedLine(resp);
            }
            return msg;
        } catch (IOException ex) {
            Logger.getLogger(CitadelToolkit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void postMessage(String room,
            String toAddress,
            String subject,
            String fromName,
            String ccs,
            String bccs,
            String email,
            String contents) throws CitadelException, IOException {
        gotoRoom(room);
        String ent0 = String.format("ENT0 1|%s||4|%s|%s|0|%s|%s||%s\r\n", toAddress,
                subject,
                fromName,
                ccs,
                bccs,
                email);
        serverOutputStream.write(ent0.getBytes());
        String resp = serv_getln();
        if (isErrorMsg(resp)) {
            throw new CitadelException(resp);
        }
        serverOutputStream.write(contents.getBytes());
        serverOutputStream.write(new byte[]{'\r', '\n'});
        serverOutputStream.write(endSeq);

    }

    public void deleteMessage(String msgId) throws IOException {
        String deleteString = "DELE " + msgId + "\r\n";
        serverOutputStream.write(deleteString.getBytes());
        serv_getln();
    }

    public void moveToTrash(String room, String msgId)
            throws IOException, CitadelException {
        gotoRoom(room);
        String moveString = String.format("MOVE %s|%s|0\r\n", msgId, "Trash");
        serverOutputStream.write(moveString.getBytes());
        String resp = serv_getln();
        if (isErrorMsg(resp)) {
            throw new CitadelException(resp);
        }

    }

    public byte[] downloadPart(String msgId, String partNum, int sizeOfPart) throws Exception {
        String DLAT = "DLAT " + msgId + "|" + partNum + "\r\n";
        serverOutputStream.write(DLAT.getBytes());
        String dlatResp = serv_getln();
        if (isErrorMsg(dlatResp)) {
            throw new CitadelException(dlatResp);
        }
        String siz = dlatResp.substring(4);
        byte[] downloaded = new byte[Integer.parseInt(siz)];
        int totalRead = 0;
        while ((totalRead < downloaded.length)) {
            totalRead = totalRead + serverInputStream.read(downloaded, totalRead, downloaded.length - totalRead);
        }
        return downloaded;
    }

    public void close() throws IOException {
        serverSocket.close();
    }

    public void open(String host, int port) throws IOException {
        serverSocket = new Socket(host, port);
        //serverSocket.open();
        // Wrap input stream in buffer
        serverInputStream = new BufferedInputStream(
                serverSocket.getInputStream(), 350);
        serverOutputStream = serverSocket.getOutputStream();
        System.out.println(serv_getln());
    }

    public boolean isErrorMsg(String line) {
        String errCode = line.substring(0, 3);
        int code = Integer.parseInt(errCode);
        if (code >= 500 && code < 600) {
            return true;
        }
        return false;
    }
}
