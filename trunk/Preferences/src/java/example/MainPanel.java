package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.prefs.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private static final String PREFIX = "xxx_";
    private final Preferences prefs;
    private final Dimension dim = new Dimension(320, 200);
    private final Point pos = new Point();
    private final JButton exitButton = new JButton();
    private final JButton clearButton = new JButton();

    public MainPanel(final JFrame frame) {
        super(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        this.prefs = Preferences.userNodeForPackage(getClass());
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                saveLocation();
                frame.dispose();
            }
        });
        frame.addComponentListener(new ComponentAdapter() {
            @Override public void componentMoved(ComponentEvent e) {
                if(frame.getExtendedState()==JFrame.NORMAL) {
                    Point pt = frame.getLocationOnScreen();
                    if(pt.x<0 || pt.y<0) return;
                    try{
                        pos.setLocation(pt);
                    }catch(IllegalComponentStateException icse) {
                        icse.printStackTrace();
                    }
                }
            }
            @Override public void componentResized(ComponentEvent e) {
                if(frame.getExtendedState()==JFrame.NORMAL) {
                    dim.setSize(getSize());
                }
            }
        });
        exitButton.setAction(new AbstractAction("exit") {
            @Override public void actionPerformed(ActionEvent evt) {
                saveLocation();
                frame.dispose();
            }
        });
        clearButton.setAction(new AbstractAction("Preferences#clear() and JFrame#dispose()") {
            @Override public void actionPerformed(ActionEvent evt) {
                try{
                    prefs.clear();
                    prefs.flush();
                }catch(BackingStoreException e) {
                    e.printStackTrace();
                }
                frame.dispose();
            }
        });

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(clearButton);
        box.add(Box.createHorizontalStrut(2));
        box.add(exitButton);

        add(new JLabel("TEST"));
        add(box, BorderLayout.SOUTH);

        int wdim = prefs.getInt(PREFIX+"dimw", dim.width);
        int hdim = prefs.getInt(PREFIX+"dimh", dim.height);
        dim.setSize(wdim, hdim);
        setPreferredSize(dim);

        Rectangle screen = frame.getGraphicsConfiguration().getBounds();
        pos.setLocation(screen.x + screen.width/2  - dim.width/2,
                        screen.y + screen.height/2 - dim.height/2);
        int xpos = prefs.getInt(PREFIX+"locx", pos.x);
        int ypos = prefs.getInt(PREFIX+"locy", pos.y);
        pos.setLocation(xpos,ypos);
        frame.setLocation(pos.x, pos.y);
    }
    private void saveLocation() {
        prefs.putInt(PREFIX+"locx", pos.x);
        prefs.putInt(PREFIX+"locy", pos.y);
        prefs.putInt(PREFIX+"dimw", dim.width);
        prefs.putInt(PREFIX+"dimh", dim.height);
        try{
            prefs.flush();
        }catch(BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        //frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
