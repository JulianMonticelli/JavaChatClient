/*
 * This program, if distributed by its author to the public as source code,
 * can be used if credit is given to its author and any project or program
 * released with the source code is released under the same stipulations.
 */
package chatclient;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author Julian
 */
public class ChatClient extends JFrame {

    private boolean shiftIsDown;
    
    private static ChatClient instance;
    
    private JPanel chatPanel;
    private JPanel messageBoxPanel;
    private JScrollPane scrollPane;
    private JTextArea chatBox;
    private JTextField messageBox;
    private JButton sendButton;
    
    private JMenuBar jMenu;
    
    private BorderLayout borderLayout;
    
    
    // There should only be one server connection at any given time
    private static ServerConnectionHandler serverConnectionHandler;
    
    public ChatClient() {
        initChatBox();
        initMessageBox();
        initSendButton();
        initMessageBoxPanel();
        initChatPanel();
        initJMenuBar();
        
        // JFrame build code
        this.setTitle("Babble Client");
        this.setContentPane(chatPanel);
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }
    
    private void sendMessageBoxText() {
        if (serverConnectionHandler != null) {
            if (!messageBox.getText().isEmpty()) {
                serverConnectionHandler.send(messageBox.getText());
                messageBox.setText("");
            }
        } else {
            JOptionPane.showMessageDialog(null, "To send a message, you"
                    + " must first be connected to a server!", 
                    "Not connected...", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void initChatBox() {
        chatBox = new JTextArea();
        scrollPane = new JScrollPane(chatBox);
        
        // Scroll pane defaults
        scrollPane.setPreferredSize(new Dimension(600, 400));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setAutoscrolls(true);
        
        // Basic chatbox style
        chatBox.setEditable(false);
        chatBox.setText("Not connected...\n");
        
        chatBox.setMargin(new Insets(5, 5, 5, 5));
        
        // Caret updating
        DefaultCaret caret = (DefaultCaret) chatBox.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        // Word wrapping
        chatBox.setLineWrap(true);
        chatBox.setWrapStyleWord(true);
    }
    
    private void initMessageBox() {
        messageBox = new JTextField();
        messageBox.setPreferredSize(new Dimension(600, 20));
        messageBox.setEditable(true);
        messageBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SHIFT:
                        shiftIsDown = true;
                        break;
                    case KeyEvent.VK_ENTER:
                        if (shiftIsDown) {
                            messageBox.setText(messageBox.getText()  + "\n");
                        }
                        else {
                            sendMessageBoxText();
                        }
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SHIFT:
                        shiftIsDown = false;
                        break;
                }
            }
        });
    }
    
    private void initSendButton() {
        sendButton = new JButton("Send");
        sendButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageBoxText();
            }
                    
                });
    }
    
    private void initChatPanel() {
        chatPanel = new JPanel();
        borderLayout = new BorderLayout(3, 3);
        chatPanel.setLayout(borderLayout);
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(messageBoxPanel, BorderLayout.SOUTH);
    }
    
    private void initMessageBoxPanel() {
        messageBoxPanel = new JPanel();
        messageBoxPanel.add(messageBox);
        messageBoxPanel.add(sendButton);
    }
    
    private void initJMenuBar() {
        jMenu = new JMenuBar();
        
        JMenu connections = new JMenu("Connections");
        JMenuItem connect = new JMenuItem("Connect...");
        connections.add(connect);
        jMenu.add(connections);
        connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String host = "";
                int ip = 0;
                
                while (host.isEmpty()) {
                    host = JOptionPane.showInputDialog(null, "Please enter a"
                        + " valid hostname/IP to connect to", "Enter Host",
                        JOptionPane.QUESTION_MESSAGE);
                }
                while (ip == 0) {
                    String ipStr = JOptionPane.showInputDialog(null, "Please"
                            + " enter a valid port.", "Enter Port", 
                            JOptionPane.QUESTION_MESSAGE);
                    try {
                        ip = Integer.parseInt(ipStr);
                    } catch (NumberFormatException ex) {
                        // Do nothing.
                    }
                }
                
                if (ip < 0) return; 
                
                // Kill previous server connection if there is one
                if (serverConnectionHandler != null) {
                    serverConnectionHandler.kill();
                }
                
                // Initialize new server connection
                serverConnectionHandler 
                        = new ServerConnectionHandler(host, ip, chatBox);
                
                // Start thread with new Server Connection
                new Thread(serverConnectionHandler).start();
            }
            
        });
        
        JMenuItem disconnect = new JMenuItem("Disconnect");
        connections.add(disconnect);
        disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (serverConnectionHandler != null) {
                    serverConnectionHandler.disconnect();
                } else {
                    JOptionPane.showMessageDialog(null, "To disconnect, you"
                            + " must first be connected to a server!", 
                            "Not connected...", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
                
        
        this.setJMenuBar(jMenu);
    }
    
    
    public static void main(String[] args) {
        instance = new ChatClient();
    }
}
