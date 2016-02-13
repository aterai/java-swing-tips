package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.activation.*;
import javax.swing.*;

public final class MainPanel {
    private MainPanel() { /* Singleton */ }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame f1 = new JFrame("@title@");
        JFrame f2 = new JFrame();
        f1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f2.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        DragPanel p1 = new DragPanel();
        DragPanel p2 = new DragPanel();

        p1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        p2.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        p1.add(new JLabel(UIManager.getIcon("OptionPane.warningIcon")));
        p1.add(new JLabel(UIManager.getIcon("OptionPane.questionIcon")));
        p1.add(new JLabel(UIManager.getIcon("OptionPane.informationIcon")));
        p1.add(new JLabel(UIManager.getIcon("OptionPane.errorIcon")));
        p1.add(new JLabel("Text"));

        MouseListener handler = new Handler();
        p1.addMouseListener(handler);
        p2.addMouseListener(handler);

        LabelTransferHandler th = new LabelTransferHandler();
        p1.setTransferHandler(th);
        p2.setTransferHandler(th);

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(new JScrollPane(new JTextArea()));
        p.add(p2);
        f1.getContentPane().add(p1);
        f2.getContentPane().add(p);
        f1.setSize(320, 240);
        f2.setSize(320, 240);
        f1.setLocationRelativeTo(null);
        Point pt = f1.getLocation();
        pt.translate(340, 0);
        f2.setLocation(pt);
        f1.setVisible(true);
        f2.setVisible(true);
    }
}

class DragPanel extends JPanel {
    public JLabel draggingLabel;
    protected DragPanel() {
        super();
    }
    protected DragPanel(LayoutManager lm) {
        super(lm);
    }
}

class Handler extends MouseAdapter {
    @Override public void mousePressed(MouseEvent e) {
        DragPanel p = (DragPanel) e.getComponent();
        Component c = SwingUtilities.getDeepestComponentAt(p, e.getX(), e.getY());
        if (c instanceof JLabel) {
            p.draggingLabel = (JLabel) c;
            p.getTransferHandler().exportAsDrag(p, e, TransferHandler.MOVE);
        }
    }
}

class LabelTransferHandler extends TransferHandler {
    private final DataFlavor localObjectFlavor;
    private final JLabel label = new JLabel() {
        @Override public boolean contains(int x, int y) {
            return false;
        }
    };
    private final JWindow window = new JWindow();
    protected LabelTransferHandler() {
        super("Text");
        //System.out.println("LabelTransferHandler");
        localObjectFlavor = new ActivationDataFlavor(DragPanel.class, DataFlavor.javaJVMLocalObjectMimeType, "JLabel");
        window.add(label);
        //window.setAlwaysOnTop(true); // AccessControlException: access denied ("java.awt.AWTPermission" "setWindowAlwaysOnTop")
        //AWTUtilities.setWindowOpaque(window, false); // JDK 1.6.0
        window.setBackground(new Color(0x0, true)); // JDK 1.7.0
        DragSource.getDefaultDragSource().addDragSourceMotionListener(new DragSourceMotionListener() {
            @Override public void dragMouseMoved(DragSourceDragEvent dsde) {
                Point pt = dsde.getLocation();
                //pt.translate(5, 5); // offset
                window.setLocation(pt);
            }
        });
    }
    @Override protected Transferable createTransferable(JComponent c) {
        System.out.println("createTransferable" + localObjectFlavor.getMimeType());
        DragPanel p = (DragPanel) c;
        JLabel l = p.draggingLabel;
        DataHandler dh = new DataHandler(c, localObjectFlavor.getMimeType());
        String text = l.getText();
        if (Objects.nonNull(text)) {
            return new LabelTransferable(dh, localObjectFlavor, text);
        } else {
            return dh;
        }
    }
    @Override public boolean canImport(TransferHandler.TransferSupport support) {
        return support.isDrop() && support.isDataFlavorSupported(localObjectFlavor);
    }
    @Override public int getSourceActions(JComponent c) {
        System.out.println("getSourceActions");
        if (c instanceof DragPanel) {
            DragPanel p = (DragPanel) c;
            JLabel l = p.draggingLabel;
            label.setIcon(l.getIcon());
            label.setText(l.getText());
            window.pack();
            Point pt = l.getLocation();
            SwingUtilities.convertPointToScreen(pt, p);
            window.setLocation(pt);
            window.setVisible(true);
        }
        return TransferHandler.MOVE;
    }
    @Override public boolean importData(TransferHandler.TransferSupport support) {
        System.out.println("importData");
        if (!canImport(support)) {
            return false;
        }
        DragPanel target = (DragPanel) support.getComponent();
        try {
            DragPanel src = (DragPanel) support.getTransferable().getTransferData(localObjectFlavor);
            JLabel l = new JLabel();
            l.setIcon(src.draggingLabel.getIcon());
            l.setText(src.draggingLabel.getText());
            target.add(l);
            target.revalidate();
            return true;
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    @Override protected void exportDone(JComponent c, Transferable data, int action) {
        System.out.println("exportDone");
        DragPanel src = (DragPanel) c;
        if (action == TransferHandler.MOVE) {
            src.remove(src.draggingLabel);
            src.revalidate();
            src.repaint();
        }
        src.draggingLabel = null;
        window.setVisible(false);
    }
}

class LabelTransferable implements Transferable {
    private final DataHandler dh;
    private final DataFlavor localObjectFlavor;
    private final StringSelection ss;

    protected LabelTransferable(DataHandler dh, DataFlavor localObjectFlavor, String text) {
        this.dh = dh;
        this.localObjectFlavor = localObjectFlavor;
        this.ss = new StringSelection(text + "\n");
    }
    @Override public DataFlavor[] getTransferDataFlavors() {
        List<DataFlavor> list = new ArrayList<>();
        for (DataFlavor f: ss.getTransferDataFlavors()) {
            list.add(f);
        }
        for (DataFlavor f: dh.getTransferDataFlavors()) {
            list.add(f);
        }
        return list.toArray(dh.getTransferDataFlavors());
    }
    @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor f: getTransferDataFlavors()) {
            if (flavor.equals(f)) {
                return true;
            }
        }
        return false;
    }
    @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(localObjectFlavor)) {
            return dh.getTransferData(flavor);
        } else {
            return ss.getTransferData(flavor);
        }
//                 if (flavor.equals(DataFlavor.stringFlavor)) {
//                     return ss.getTransferData(flavor);
//                 } else if (flavor.equals(DataFlavor.plainTextFlavor)) {
//                     return ss.getTransferData(flavor);
//                 } else if (flavor.equals(localObjectFlavor)) {
//                     return dh.getTransferData(flavor);
//                 } else {
//                     throw new UnsupportedFlavorException(flavor);
//                 }
    }
}
