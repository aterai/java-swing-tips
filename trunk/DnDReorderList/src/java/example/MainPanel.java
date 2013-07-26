package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.IOException;
import javax.activation.*;
//import javax.activation.DataHandler;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(makeList()));
        p.setBorder(BorderFactory.createTitledBorder("Drag & Drop JList"));
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
        add(p);
    }
    @SuppressWarnings("unchecked")
    private static JList makeList() {
        DefaultListModel listModel = new DefaultListModel();
        listModel.addElement(Color.RED);
        listModel.addElement(Color.BLUE);
        listModel.addElement(Color.GREEN);
        listModel.addElement(Color.CYAN);
        listModel.addElement(Color.ORANGE);
        listModel.addElement(Color.PINK);
        listModel.addElement(Color.MAGENTA);
        JList list = new JList(listModel) {
            @Override public void updateUI() {
                setSelectionBackground(null); //Nimbus
                super.updateUI();
            }
        };
        //list.setVisibleRowCount(-1);
        list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setTransferHandler(new ListItemTransferHandler());
        list.setDropMode(DropMode.INSERT);
        list.setDragEnabled(true);

        //Disable row Cut, Copy, Paste
        ActionMap map = list.getActionMap();
        AbstractAction dummy = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {}
        };
        map.put(TransferHandler.getCutAction().getValue(Action.NAME),   dummy);
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),  dummy);
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy);

        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Color color = (Color)value;
                ((JLabel)c).setForeground(color);
                return c;
            }
        });

        return list;
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
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//Demo - BasicDnD (Drag and Drop and Data Transfer)>http://docs.oracle.com/javase/tutorial/uiswing/dnd/basicdemo.html
//Drag and drop for non-String objects>http://www.javakb.com/Uwe/Forum.aspx/java-programmer/43866/Drag-and-drop-for-non-String-objects
class ListItemTransferHandler extends TransferHandler {
    private final DataFlavor localObjectFlavor;
    private Object[] transferedObjects = null;
    public ListItemTransferHandler() {
        localObjectFlavor = new ActivationDataFlavor(Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
    }
    @SuppressWarnings("deprecation")
    @Override protected Transferable createTransferable(JComponent c) {
        JList list = (JList) c;
        indices = list.getSelectedIndices();
        transferedObjects = list.getSelectedValues();
        return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    }
    @Override public boolean canImport(TransferSupport info) {
        if(!info.isDrop() || !info.isDataFlavorSupported(localObjectFlavor)) {
            return false;
        }
        return true;
    }
    @Override public int getSourceActions(JComponent c) {
        return MOVE; //TransferHandler.COPY_OR_MOVE;
    }
    @SuppressWarnings("unchecked")
    @Override public boolean importData(TransferSupport info) {
        if(!canImport(info)) {
            return false;
        }
        JList target = (JList)info.getComponent();
        JList.DropLocation dl = (JList.DropLocation)info.getDropLocation();
        DefaultListModel listModel = (DefaultListModel)target.getModel();
        int index = dl.getIndex();
        //boolean insert = dl.isInsert();
        int max = listModel.getSize();
        if(index<0 || index>max) {
            index = max;
        }
        addIndex = index;

        // ???
        //if(indices != null && index >= indices[0] - 1 && index <= indices[indices.length - 1]) {
        //    indices = null;
        //    return false;
        //}

        try{
            Object[] values = (Object[])info.getTransferable().getTransferData(localObjectFlavor);
            addCount = values.length;
            for(int i=0;i<values.length;i++) {
                int idx = index++;
                listModel.add(idx, values[i]);
                target.addSelectionInterval(idx, idx);
            }
            return true;
        }catch(UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }
    @Override protected void exportDone(JComponent c, Transferable data, int action) {
        cleanup(c, action == MOVE);
    }
    private void cleanup(JComponent c, boolean remove) {
        if(remove && indices != null) {
            JList source = (JList)c;
            DefaultListModel model  = (DefaultListModel)source.getModel();
            //If we are moving items around in the same list, we
            //need to adjust the indices accordingly, since those
            //after the insertion point have moved.
            if(addCount > 0) {
                for(int i=0;i<indices.length;i++) {
                    if(indices[i]>=addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            for(int i=indices.length-1;i>=0;i--) {
                model.remove(indices[i]);
            }
        }
        indices  = null;
        addCount = 0;
        addIndex = -1;
    }
    private int[] indices = null;
    private int addIndex  = -1; //Location where items were added
    private int addCount  = 0;  //Number of items added.
}
