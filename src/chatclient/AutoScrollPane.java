/*
 * This program, if distributed by its author to the public as source code,
 * can be used if credit is given to its author and any project or program
 * released with the source code is released under the same stipulations.
 */

package chatclient;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JScrollPane;

/**
 * @author Julian
 */
public class AutoScrollPane extends JScrollPane {
    
    private boolean bottomLocked;
    
    public AutoScrollPane(Component c) {
        super(c);
        bottomLocked = true;
        
        // Scroll pane defaults
        this.setPreferredSize(new Dimension(600, 400));
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.setAutoscrolls(true);
        //this.
    }
    
    
    
    public void autoScroll() {
        if (bottomLocked) {
            this.getVerticalScrollBar()
                    .setAlignmentY(this.verticalScrollBar.getMaximum());
        }
    }
    
    
}
