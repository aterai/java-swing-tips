package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Objects;
// import javax.activation.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(makeList()));
        p.setBorder(BorderFactory.createTitledBorder("Drag & Drop JList"));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
        add(p);
    }
    private static JList<Color> makeList() {
        DefaultListModel<Color> listModel = new DefaultListModel<>();
        listModel.addElement(Color.RED);
        listModel.addElement(Color.BLUE);
        listModel.addElement(Color.GREEN);
        listModel.addElement(Color.CYAN);
        listModel.addElement(Color.ORANGE);
        listModel.addElement(Color.PINK);
        listModel.addElement(Color.MAGENTA);
        JList<Color> list = new JList<Color>(listModel) {
            @Override public void updateUI() {
                setSelectionBackground(null); // Nimbus
                super.updateUI();
            }
        };
        // list.setVisibleRowCount(-1);
        list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setTransferHandler(new ListItemTransferHandler());
        list.setDropMode(DropMode.INSERT);
        list.setDragEnabled(true);

        // Disable row Cut, Copy, Paste
        ActionMap map = list.getActionMap();
        Action dummy = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { /* Dummy action */ }
        };
        map.put(TransferHandler.getCutAction().getValue(Action.NAME), dummy);
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME), dummy);
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy);

        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Color color = (Color) value;
                ((JLabel) c).setForeground(color);
                return c;
            }
        });

        return list;
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

// Demo - BasicDnD (The Java™ Tutorials > Creating a GUI With JFC/Swing > Drag and Drop and Data Transfer)
// https://docs.oracle.com/javase/tutorial/uiswing/dnd/basicdemo.html
class ListItemTransferHandler extends TransferHandler {
    protected final DataFlavor localObjectFlavor;
    protected int[] indices;
    protected int addIndex = -1; // Location where items were added
    protected int addCount; // Number of items added.

    protected ListItemTransferHandler() {
        super();
        // localObjectFlavor = new ActivationDataFlavor(Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
        localObjectFlavor = new DataFlavor(Object[].class, "Array of items");
    }
    @Override protected Transferable createTransferable(JComponent c) {
        JList<?> source = (JList<?>) c;
        indices = source.getSelectedIndices();
        Object[] transferedObjects = source.getSelectedValuesList().toArray(new Object[0]);
        // return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
        return new Transferable() {
            @Override public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] {localObjectFlavor};
            }
            @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
                return Objects.equals(localObjectFlavor, flavor);
            }
            @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                 if (isDataFlavorSupported(flavor)) {
                     return transferedObjects;
                 } else {
                     throw new UnsupportedFlavorException(flavor);
                 }
             }
        };
    }
    @Override public boolean canImport(TransferHandler.TransferSupport info) {
        return info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
    }
    @Override public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE; // TransferHandler.COPY_OR_MOVE;
    }
    @SuppressWarnings("unchecked")
    @Override public boolean importData(TransferHandler.TransferSupport info) {
        if (!canImport(info)) {
            return false;
        }
        TransferHandler.DropLocation tdl = info.getDropLocation();
        if (!(tdl instanceof JList.DropLocation)) {
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

        // ???
        //if (indices != null && index >= indices[0] - 1 && index <= indices[indices.length - 1]) {
        //    indices = null;
        //    return false;
        //}

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
