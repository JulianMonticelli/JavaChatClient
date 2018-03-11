/*
 * This program, if distributed by its author to the public as source code,
 * can be used if credit is given to its author and any project or program
 * released with the source code is released under the same stipulations.
 */

package chatclient;

/**
 * @author Julian
 */
public final class ChatOpcodes {
    
    // Some of these may be unnecessary, but more is better - it will permit
    // me to keep an eye on what I need and what I have
    
    
    // An opcode reserved strictly for a heartbeat - currently unimplemented
    public static final char OP_HEARTBEAT = (char)0x00;
    
    // An unformatted message opcode - this message should be displayed as-is
    public static final char OP_SERVER_MESSAGE = (char)0x01;
    
    // A message sent to the public chat channel from a user
    public static final char OP_USER_MESSAGE = (char)0x10;
    
    // A message sent to a user privately
    public static final char OP_PRIVATE_MESSAGE_TO = (char)0xA0;
    
    // A message sent from a user privately
    public static final char OP_PRIVATE_MESSAGE_FROM = (char)0xA1;
    
    // An opcode to indicate a graceful disconnect
    public static final char OP_DISCONNECT = (char)0xFF;
    
}
