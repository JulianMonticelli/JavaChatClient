/*
 * This program, if distributed by its author to the public as source code,
 * can be used if credit is given to its author and any project or program
 * released with the source code is released under the same stipulations.
 */

package chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JTextArea;

/**
 * @author Julian
 */
class ServerConnectionHandler implements Runnable {
    
    private int port;
    
    private JTextArea textBox;
    
    Socket sock;
    BufferedReader in;
    PrintWriter out;
    
    EncryptionHandler encHandler;
    
    public ServerConnectionHandler(String ip, int port, JTextArea textBox) {
        this.textBox = textBox;
        try {
            sock = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter(sock.getOutputStream());
            encHandler = new EncryptionHandler();
        } catch (IOException e) {
            textBox.append("Could not establish a connection with the provided" 
                    + " host. Are you sure " + ip + ":" + port + " is both"
                    + " accurate and online?\n");
        }
    }
    
    @Override
    public void run() {
        if (sock == null) return;
        
        String buffer;
        
        try {
            out.println(encHandler.generatePublicKeyMessage());
            out.flush();
            while ((buffer = in.readLine()) != null) {
                if (!buffer.startsWith("!!PUBK:")) {
                    kill();
                    return;
                } else {
                    String pubKey = buffer.substring(7);
                    try {
                        encHandler.initEncryptionHandler(pubKey);
                    } catch (Exception e) {
                        e.printStackTrace();
                        kill();
                        return;
                    }
                }
                break;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            kill();
            return;
        }
        
        try {
            while ((buffer = in.readLine()) != null) {
                String message = encHandler.decryptMessage(buffer);
                if (message.equals("<BYE>")) {
                    break;
                }
                else if (message.startsWith("<UMSG>")) {
                    message = message.substring(6);
                } else if (message.startsWith("<RAW>")) {
                    message = message.substring(5);
                }
                textBox.append(message + "\n");
            }
        } catch (IOException e) {
            textBox.append(e.toString());
        } finally {
            kill();
        }
    }
    
    public void disconnect() {
        if (sock != null) {
            send("/disconnect");  
        }
    }
    
    public void kill() {
        try {
            // WARNING: This might be bad code!
            if (in != null) {
                in.close();
            }
        } catch (IOException ex) {
            textBox.append(ex.toString());
        }
        if (out != null) {
           out.close();        
        }
        try {
            if (sock != null) {
                sock.close();
            }
        } catch (IOException ex) {
            textBox.append(ex.toString());
        } finally {
            textBox.append("Connection to server has been killed.\n");
        }
    }

    public void send(String text) {
        if (out == null) return;
        out.println(encHandler.encryptMessage(text));
        out.flush();
    }
    
}
