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
    
    /***************************************************************************
     * The bulk of the client-server connection thread code. Exchanges public
     * keys with the server and loops looking for input from the server.
     */
    @Override
    public void run() {
        if (sock == null) return;
        
        String buffer;
        
        try {
            
            // Push our public session key to the server
            out.println(encHandler.generatePublicKeyMessage());
            out.flush();
            
            // Await a public session key from the server in response
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
        
        // While the connection is alive, continuously await a message from the
        // server
        try {
            while ((buffer = in.readLine()) != null) {
                String message;
                try {
                    message = encHandler.decipherMessage(buffer);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    kill();
                    return;
                }
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
    
    /***************************************************************************
     * Disconnect from the client by sending a disconnect command to the server.
     */
    public void disconnect() {
        // Disconnect by sending a disconnect command
        if (sock != null) {
            send("/disconnect");  
        }
    }
    
    /***************************************************************************
     * Kill the connection to the server. Also, append a message that the server
     * has been killed.
     */
    public void kill() {
        
        // Code works, but there may be a better way to do this.
        try {
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

    
    /***************************************************************************
     * Send a message to the server which is 
     * @param text 
     */
    public void send(String text) {
        if (out == null) return;
        try {
            out.println(
                    encHandler.encodeMessage(EncryptionHandler.getNewAESKey(),
                            text));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        out.flush();
    }
    
}
