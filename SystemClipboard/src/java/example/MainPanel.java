package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;
import javax.jnlp.*;

public class MainPanel extends JPanel{
    private final JLabel label = new JLabel();
    private ClipboardService cs = null;
    public MainPanel() {
        super(new BorderLayout());
        //if(isWebStart()) {
        try{
            cs = (ClipboardService)ServiceManager.lookup("javax.jnlp.ClipboardService");
        }catch(UnavailableServiceException e) {
            cs = null;
        }
        add(new JScrollPane(label));
        add(new JButton(new AbstractAction("get Clipboard DataFlavor") {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    Transferable t = (cs==null)?Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null)
                                               :cs.getContents();
                    if(t==null) {
                        java.awt.Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    String str = "";
                    ImageIcon image = null;
                    if(t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                        image = new ImageIcon((Image)t.getTransferData(DataFlavor.imageFlavor));
                    }else if(t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        str = (String)t.getTransferData(DataFlavor.stringFlavor);
                    }
                    label.setText(str);
                    label.setIcon(image);
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 200));
    }
//     private static boolean isWebStart() {
//         try{
//             javax.jnlp.ServiceManager.lookup("javax.jnlp.BasicService");
//             return true;
//         }catch(Exception ex) {
//             return false;
//         }
//     }
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
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
