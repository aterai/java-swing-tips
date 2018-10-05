package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import javax.jnlp.ClipboardService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JLabel label = new JLabel();
    private final JButton button = new JButton("get Clipboard DataFlavor");
    public MainPanel() {
        super(new BorderLayout());
        Object o;
        try {
            o = ServiceManager.lookup("javax.jnlp.ClipboardService");
        } catch (UnavailableServiceException ex) {
            o = null;
        }
        Optional<ClipboardService> csOp = Optional.ofNullable((ClipboardService) o);
        button.addActionListener(e -> {
            try {
                Transferable t = csOp.map(ClipboardService::getContents)
                    .orElse(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null));
                if (Objects.isNull(t)) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                String str = "";
                ImageIcon image = null;
                if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    image = new ImageIcon((Image) t.getTransferData(DataFlavor.imageFlavor));
                } else if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    str = Objects.toString(t.getTransferData(DataFlavor.stringFlavor));
                }
                label.setText(str);
                label.setIcon(image);
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
            }
        });

        add(new JScrollPane(label));
        add(button, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
