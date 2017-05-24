package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.*;
import javax.activation.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        DefaultListModel<ListItem> model = new DefaultListModel<>();
        //http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
        model.addElement(new ListItem("asdasdfsd",  "wi0009-32.png"));
        model.addElement(new ListItem("12345",      "wi0054-32.png"));
        model.addElement(new ListItem("ADFFDF.asd", "wi0062-32.png"));
        model.addElement(new ListItem("test",       "wi0063-32.png"));
        model.addElement(new ListItem("32.png",     "wi0064-32.png"));
        model.addElement(new ListItem("asdfsd.jpg", "wi0096-32.png"));
        model.addElement(new ListItem("6896",       "wi0111-32.png"));
        model.addElement(new ListItem("t467467est", "wi0122-32.png"));
        model.addElement(new ListItem("test123",    "wi0124-32.png"));
        model.addElement(new ListItem("test(1)",    "wi0126-32.png"));

        ReorderbleList<ListItem> list = new ReorderbleList<>(model);
        add(new JCheckBox(new AbstractAction("Compact drag image mode") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox c = (JCheckBox) e.getSource();
                if (c.isSelected()) {
                    list.setTransferHandler(new CompactListItemTransferHandler());
                } else {
                    list.setTransferHandler(new ListItemTransferHandler());
                }
            }
        }) {
            @Override public void updateUI() {
                super.updateUI();
                setSelected(false);
            }
        }, BorderLayout.NORTH);
        add(new JScrollPane(list));
        setPreferredSize(new Dimension(320, 240));
    }

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
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class ListItemTransferHandler extends TransferHandler {
    private final DataFlavor localObjectFlavor;
    private int[] indices;
    private int addIndex = -1; //Location where items were added
    private int addCount; //Number of items added.
    protected static final JLabel LABEL = new JLabel() {
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = 32;
            return d;
        }
    };

    protected ListItemTransferHandler() {
        super();
        localObjectFlavor = new ActivationDataFlavor(Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
        LABEL.setOpaque(true);
        LABEL.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        LABEL.setHorizontalAlignment(SwingConstants.CENTER);
        LABEL.setForeground(Color.WHITE);
        LABEL.setBackground(new Color(0, 0, 255, 200));
    }
    @Override protected Transferable createTransferable(JComponent c) {
        JList<?> source = (JList<?>) c;
        indices = source.getSelectedIndices();
        Object[] transferedObjects = source.getSelectedValuesList().toArray(new Object[0]);
        return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    }
    @Override public boolean canImport(TransferHandler.TransferSupport info) {
        //Cursor flickering? return info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
        if (info.isDrop() && info.isDataFlavorSupported(localObjectFlavor)) {
            info.setDropAction(TransferHandler.MOVE);
            return true;
        } else {
            return false;
        }
    }
    @Override public int getSourceActions(JComponent c) {
        System.out.println("getSourceActions");
        Component glassPane = c.getRootPane().getGlassPane();
        glassPane.setCursor(DragSource.DefaultMoveDrop);
        if (c instanceof JList) {
            JList source = (JList) c;
            setDragImage(createDragImage(source));
            //Point pt = c.getMousePosition();
            //if (Objects.nonNull(pt)) {
            //    setDragImageOffset(pt);
            //}
            Optional.ofNullable(c.getMousePosition()).ifPresent(this::setDragImageOffset);
            return TransferHandler.MOVE; //TransferHandler.COPY_OR_MOVE;
        }
        return TransferHandler.NONE;
    }
    private static BufferedImage createDragImage(JList source) {
        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        ListCellRenderer renderer = source.getCellRenderer();
        for (int i: source.getSelectedIndices()) {
            @SuppressWarnings("unchecked")
            Component c = renderer.getListCellRendererComponent(source, source.getModel().getElementAt(i), i, false, false);
            Rectangle rect = source.getCellBounds(i, i);
            SwingUtilities.paintComponent(g2, c, source, rect);
        }
        g2.dispose();
        return bi;
    }
    @SuppressWarnings("unchecked")
    @Override public boolean importData(TransferHandler.TransferSupport info) {
        TransferHandler.DropLocation tdl = info.getDropLocation();
        if (!canImport(info) || !(tdl instanceof JList.DropLocation)) {
            return false;
        }
        JList.DropLocation dl = (JList.DropLocation) tdl;
        JList target = (JList) info.getComponent();
        DefaultListModel listModel = (DefaultListModel) target.getModel();
        int index = dl.getIndex();
        //boolean insert = dl.isInsert();
        int max = listModel.getSize();
        if (index < 0 || index > max) {
            index = max;
        }
        addIndex = index;
        try {
            Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
            for (int i = 0; i < values.length; i++) {
                int idx = index++;
                listModel.add(idx, values[i]);
                target.addSelectionInterval(idx, idx);
            }
            addCount = values.length;
            return true;
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    @Override protected void exportDone(JComponent c, Transferable data, int action) {
        System.out.println("exportDone");
        Component glassPane = c.getRootPane().getGlassPane();
        //glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        glassPane.setVisible(false);
        cleanup(c, action == TransferHandler.MOVE);
    }
    private void cleanup(JComponent c, boolean remove) {
        if (remove && Objects.nonNull(indices)) {
            //If we are moving items around in the same list, we
            //need to adjust the indices accordingly, since those
            //after the insertion point have moved.
            if (addCount > 0) {
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] >= addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            JList source = (JList) c;
            DefaultListModel model = (DefaultListModel) source.getModel();
            for (int i = indices.length - 1; i >= 0; i--) {
                model.remove(indices[i]);
            }
        }
        indices  = null;
        addCount = 0;
        addIndex = -1;
    }
}

class CompactListItemTransferHandler extends ListItemTransferHandler {
    @Override public int getSourceActions(JComponent c) {
        System.out.println("getSourceActions");
        Component glassPane = c.getRootPane().getGlassPane();
        glassPane.setCursor(DragSource.DefaultMoveDrop);
        if (!(c instanceof JList)) {
            return TransferHandler.NONE;
        }
        JList source = (JList) c;
        int w = source.getFixedCellWidth();
        int h = source.getFixedCellHeight() - 20; //TODO
        setDragImage(createCompactDragImage(source, w, h));
        setDragImageOffset(new Point(w / 2, h));
        return TransferHandler.MOVE; //TransferHandler.COPY_OR_MOVE;
    }
    private static BufferedImage createCompactDragImage(JList source, int w, int h) {
        if (w <= 0 || h <= 0) {
            return null;
        }
        int[] selectedIndices = source.getSelectedIndices();
        BufferedImage br = source.getGraphicsConfiguration().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
        Graphics2D g2 = br.createGraphics();
        ListCellRenderer renderer = source.getCellRenderer();
        int idx = selectedIndices[0];
        Object valueAt = source.getModel().getElementAt(idx);
        @SuppressWarnings("unchecked")
        Component c = renderer.getListCellRendererComponent(source, valueAt, idx, false, false);
        Rectangle rect = source.getCellBounds(idx, idx);
        SwingUtilities.paintComponent(g2, c, source, 0, 0, rect.width, rect.height);
        int selectedCount = selectedIndices.length;
        boolean isMoreThanOneItemSelected = selectedCount > 1;
        if (isMoreThanOneItemSelected) {
            LABEL.setText(String.valueOf(selectedCount));
            Dimension d = LABEL.getPreferredSize();
            SwingUtilities.paintComponent(g2, LABEL, source, (w - d.width) / 2, (h - d.height) / 2, d.width, d.height);
        }
        g2.dispose();
        br.coerceData(true);
        return br;
    }
}
