package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.IOException;
import javax.jnlp.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JLabel label = new JLabel();
    private final ClipboardService cs;
    public MainPanel() {
        super(new BorderLayout());

        ClipboardService tmp;
        try {
            tmp = (ClipboardService) ServiceManager.lookup("javax.jnlp.ClipboardService");
        } catch (UnavailableServiceException t) {
            tmp = null;
        }
        cs = tmp;
        add(new JScrollPane(label));
        add(new JButton(new AbstractAction("get Clipboard DataFlavor") {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    Transferable t = (cs == null)?Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null)
                                               :cs.getContents();
                    if (t == null) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    String str = "";
                    ImageIcon image = null;
                    if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                        image = new ImageIcon((Image) t.getTransferData(DataFlavor.imageFlavor));
                    } else if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        str = (String) t.getTransferData(DataFlavor.stringFlavor);
                    }
                    label.setText(str);
                    label.setIcon(image);
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        }), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 200));
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
