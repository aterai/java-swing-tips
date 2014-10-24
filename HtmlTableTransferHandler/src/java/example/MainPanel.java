package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final String[] columnNames = {"Type", "Value"};
    private final Object[][] data = {
        {"String",  "text"      },
        {"Date",    new Date()  },
        {"Integer", 12          },
        {"Double",  3.45        },
        {"Boolean", Boolean.TRUE},
        {"Color",   Color.RED   }
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        JPanel p = new JPanel(new GridLayout(3, 1));
        JTable table1 = new PropertyTable(model);
        JTable table2 = new PropertyTable(model);
        for (JTable t: Arrays.asList(table1, table2)) {
            t.setDefaultRenderer(Color.class, new ColorRenderer());
            t.setDefaultEditor(Color.class,   new ColorEditor());
            t.setDefaultEditor(Date.class,    new DateEditor());
            p.add(new JScrollPane(t));
        }
        table2.setTransferHandler(new HtmlTableTransferHandler());
        p.add(new JScrollPane(new JEditorPane("text/html", "")));
        add(p);
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

class PropertyTable extends JTable {
    private Class<?> editingClass;
    public PropertyTable(TableModel model) {
        super(model);
    }
    //public PropertyTable(Object[][] data, String[] columnNames) {
    //    super(data, columnNames);
    //}
    private Class<?> getClassAt(int row, int column) {
        int mc = convertColumnIndexToModel(column);
        int mr = convertRowIndexToModel(row);
        return getModel().getValueAt(mr, mc).getClass();
    }
    @Override public TableCellRenderer getCellRenderer(int row, int column) {
        if (convertColumnIndexToModel(column) == 1) {
            return getDefaultRenderer(getClassAt(row, column));
        } else {
            return super.getCellRenderer(row, column);
        }
    }
    @Override public TableCellEditor getCellEditor(int row, int column) {
        if (convertColumnIndexToModel(column) == 1) {
            editingClass = getClassAt(row, column);
            return getDefaultEditor(editingClass);
        } else {
            editingClass = null;
            return super.getCellEditor(row, column);
        }
    }
    @Override public Class<?> getColumnClass(int column) {
        if (convertColumnIndexToModel(column) == 1) {
            return editingClass;
        } else {
            return super.getColumnClass(column);
        }
    }
}

class DateEditor extends JSpinner implements TableCellEditor {
    protected transient ChangeEvent changeEvent;
    private final JSpinner.DateEditor editor;

    public DateEditor() {
        super(new SpinnerDateModel());
        editor = new JSpinner.DateEditor(this, "yyyy/MM/dd");
        setEditor(editor);
        setArrowButtonEnabled(false);
        editor.getTextField().setHorizontalAlignment(JFormattedTextField.LEFT);

        editor.getTextField().addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                setArrowButtonEnabled(false);
            }
            @Override public void focusGained(FocusEvent e) {
                //System.out.println("getTextField");
                setArrowButtonEnabled(true);
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        editor.getTextField().setCaretPosition(8);
                        editor.getTextField().setSelectionStart(8);
                        editor.getTextField().setSelectionEnd(10);
                    }
                });
            }
        });
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }
    private void setArrowButtonEnabled(boolean flag) {
        for (Component c: getComponents()) {
            if (c instanceof JButton) {
                ((JButton) c).setEnabled(flag);
            }
        }
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        setValue(value);
        return this;
    }
    @Override public Object getCellEditorValue() {
        return getValue();
    }

    //Copied from AbstractCellEditor
    //protected EventListenerList listenerList = new EventListenerList();
    //protected transient ChangeEvent changeEvent;
    @Override public boolean isCellEditable(EventObject e) {
        return true;
    }
    @Override public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
    @Override public boolean stopCellEditing() {
        try {
            commitEdit();
        } catch (ParseException pe) {
            Toolkit.getDefaultToolkit().beep();
            return false;
//             // Edited value is invalid, spinner.getValue() will return
//             // the last valid value, you could revert the spinner to show that:
//             editor.getTextField().setValue(getValue());
        }
        fireEditingStopped();
        return true;
    }
    @Override public void cancelCellEditing() {
        fireEditingCanceled();
    }
    @Override public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }
    @Override public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }
    public CellEditorListener[] getCellEditorListeners() {
        return listenerList.getListeners(CellEditorListener.class);
    }
    protected void fireEditingStopped() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
            }
        }
    }
    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
            }
        }
    }
}

class ColorRenderer extends DefaultTableCellRenderer {
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof Color) {
            Color color = (Color) value;
            l.setIcon(new ColorIcon(color));
            l.setText(String.format("(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue()));
        }
        return l;
    }
}

//http://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableDialogEditDemoProject/src/components/ColorEditor.java
class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
    protected static final String EDIT = "edit";
    private final JButton button = new JButton();
    private final JColorChooser colorChooser;
    private final JDialog dialog;
    private Color currentColor;

    public ColorEditor() {
        super();
        //Set up the editor (from the table's point of view),
        //which is a button.
        //This button brings up the color chooser dialog,
        //which is the editor from the user's point of view.
        button.setActionCommand(EDIT);
        button.addActionListener(this);
        //button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        button.setOpaque(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setHorizontalTextPosition(SwingConstants.RIGHT);

        //Set up the dialog that the button brings up.
        colorChooser = new JColorChooser();
        dialog = JColorChooser.createDialog(button, "Pick a Color", true, colorChooser, this, null);
    }
    /**
     * Handles events from the editor button and from
     * the dialog's OK button.
     */
    @Override public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {
            //The user has clicked the cell, so
            //bring up the dialog.
            button.setBackground(currentColor);
            button.setIcon(new ColorIcon(currentColor));
            colorChooser.setColor(currentColor);
            dialog.setVisible(true);

            //Make the renderer reappear.
            fireEditingStopped();
        } else { //User pressed dialog's "OK" button.
            currentColor = colorChooser.getColor();
        }
    }
    //Implement the one CellEditor method that AbstractCellEditor doesn't.
    @Override public Object getCellEditorValue() {
        return currentColor;
    }
    //Implement the one method defined by TableCellEditor.
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentColor = (Color) value;
        button.setIcon(new ColorIcon(currentColor));
        button.setText(String.format("(%d, %d, %d)", currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue()));
        return button;
    }
}

class ColorIcon implements Icon {
    private final Color color;
    public ColorIcon(Color color) {
        this.color = color;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.fillRect(x, y, getIconWidth(), getIconHeight());
    }
    @Override public int getIconWidth() {
        return 10;
    }
    @Override public int getIconHeight() {
        return 10;
    }
}

class HtmlTableTransferHandler extends TransferHandler {
    //@see javax/swing/plaf/basic/BasicTableUI.TableTransferHandler#createTransferable(JComponent)
    @Override protected Transferable createTransferable(JComponent c) {
        if (c instanceof JTable) {
            JTable table = (JTable) c;

            if (!table.getRowSelectionAllowed() && !table.getColumnSelectionAllowed()) {
                return null;
            }

            int[] rows;
            if (table.getRowSelectionAllowed()) {
                rows = table.getSelectedRows();
            } else {
                int rowCount = table.getRowCount();

                rows = new int[rowCount];
                for (int counter = 0; counter < rowCount; counter++) {
                    rows[counter] = counter;
                }
            }

            int[] cols;
            if (table.getColumnSelectionAllowed()) {
                cols = table.getSelectedColumns();
            } else {
                int colCount = table.getColumnCount();

                cols = new int[colCount];
                for (int counter = 0; counter < colCount; counter++) {
                    cols[counter] = counter;
                }
            }

            //if (rows == null || cols == null || rows.length == 0 || cols.length == 0) {
            if (cols == null || rows.length == 0 || cols.length == 0) {
                return null;
            }

            StringBuffer plainBuf = new StringBuffer();
            StringBuffer htmlBuf = new StringBuffer(64);

            htmlBuf.append("<html>\n<body>\n<table border='1'>\n");

            for (int row = 0; row < rows.length; row++) {
                htmlBuf.append("<tr>\n");
                for (int col = 0; col < cols.length; col++) {
                    Object obj = table.getValueAt(rows[row], cols[col]);
                    String val = Objects.toString(obj, "") + "\t"; //.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                    plainBuf.append(val);

                    if (obj instanceof Date) {
                        String v = Objects.toString((Date) obj, "");
                        htmlBuf.append("  <td><time>" + v + "</time></td>\n");
                    } else  if (obj instanceof Color) {
                        htmlBuf.append(String.format("  <td style='background-color:#%06x'>&nbsp;</td>%n", ((Color) obj).getRGB() & 0xffffff));
                    } else {
                        htmlBuf.append("  <td>" + Objects.toString(obj, "") + "</td>\n");
                    }
                }
                // we want a newline at the end of each line and not a tab
                plainBuf.deleteCharAt(plainBuf.length() - 1).append("\n");
                htmlBuf.append("</tr>\n");
            }

            // remove the last newline
            plainBuf.deleteCharAt(plainBuf.length() - 1);
            htmlBuf.append("</table>\n</body>\n</html>");

            return new BasicTransferable(plainBuf.toString(), htmlBuf.toString());
        }

        return null;
    }
    @Override public int getSourceActions(JComponent c) {
        return COPY;
    }
}

//Copied from javax/swing/plaf/basic/BasicTextUI.BasicTransferable
class BasicTransferable implements Transferable {

    protected String plainData;
    protected String htmlData;

    private static DataFlavor[] htmlFlavors;
    private static DataFlavor[] stringFlavors;
    private static DataFlavor[] plainFlavors;

    static {
        try {
            htmlFlavors = new DataFlavor[3];
            htmlFlavors[0] = new DataFlavor("text/html;class=java.lang.String");
            htmlFlavors[1] = new DataFlavor("text/html;class=java.io.Reader");
            htmlFlavors[2] = new DataFlavor("text/html;charset=unicode;class=java.io.InputStream");

            plainFlavors = new DataFlavor[3];
            plainFlavors[0] = new DataFlavor("text/plain;class=java.lang.String");
            plainFlavors[1] = new DataFlavor("text/plain;class=java.io.Reader");
            plainFlavors[2] = new DataFlavor("text/plain;charset=unicode;class=java.io.InputStream");

            stringFlavors = new DataFlavor[2];
            stringFlavors[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.String");
            stringFlavors[1] = DataFlavor.stringFlavor;

        } catch (ClassNotFoundException cle) {
            System.err.println("error initializing javax.swing.plaf.basic.BasicTranserable");
        }
    }

    public BasicTransferable(String plainData, String htmlData) {
        this.plainData = plainData;
        this.htmlData = htmlData;
    }

    /**
     * Returns an array of DataFlavor objects indicating the flavors the data
     * can be provided in.  The array should be ordered according to preference
     * for providing the data (from most richly descriptive to least descriptive).
     * @return an array of data flavors in which this data can be transferred
     */
    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] richerFlavors = getRicherFlavors();
        int nRicher = richerFlavors.length; //(richerFlavors != null) ? richerFlavors.length : 0;
        int nHTML = isHTMLSupported() ? htmlFlavors.length : 0;
        int nPlain = isPlainSupported() ? plainFlavors.length : 0;
        int nString = isPlainSupported() ? stringFlavors.length : 0;
        int nFlavors = nRicher + nHTML + nPlain + nString;
        DataFlavor[] flavors = new DataFlavor[nFlavors];

        // fill in the array
        int nDone = 0;
        if (nRicher > 0) {
            System.arraycopy(richerFlavors, 0, flavors, nDone, nRicher);
            nDone += nRicher;
        }
        if (nHTML > 0) {
            System.arraycopy(htmlFlavors, 0, flavors, nDone, nHTML);
            nDone += nHTML;
        }
        if (nPlain > 0) {
            System.arraycopy(plainFlavors, 0, flavors, nDone, nPlain);
            nDone += nPlain;
        }
        if (nString > 0) {
            System.arraycopy(stringFlavors, 0, flavors, nDone, nString);
            //nDone += nString;
        }
        return flavors;
    }

    /**
     * Returns whether or not the specified data flavor is supported for
     * this object.
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is supported
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        DataFlavor[] flavors = getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an object which represents the data to be transferred.  The class
     * of the object returned is defined by the representation class of the flavor.
     *
     * @param flavor the requested flavor for the data
     * @see DataFlavor#getRepresentationClass
     * @exception IOException                if the data is no longer available
     *              in the requested flavor.
     * @exception UnsupportedFlavorException if the requested data flavor is
     *              not supported.
     */
    @SuppressWarnings("deprecation")
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        //???: DataFlavor[] richerFlavors = getRicherFlavors();
        if (isRicherFlavor(flavor)) {
            return getRicherData(flavor);
        } else if (isHTMLFlavor(flavor)) {
            //String data = getHTMLData();
            //data = (data == null) ? "" : data;
            String data = Objects.toString(getHTMLData(), "");
            if (String.class.equals(flavor.getRepresentationClass())) {
                return data;
            } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                return new StringReader(data);
            } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                return new StringBufferInputStream(data);
            }
            // fall through to unsupported
        } else if (isPlainFlavor(flavor)) {
            //String data = getPlainData();
            //data = (data == null) ? "" : data;
            String data = Objects.toString(getPlainData(), "");
            if (String.class.equals(flavor.getRepresentationClass())) {
                return data;
            } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                return new StringReader(data);
            } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                return new StringBufferInputStream(data);
            }
            // fall through to unsupported

        } else if (isStringFlavor(flavor)) {
            //String data = getPlainData();
            //data = (data == null) ? "" : data;
            //return data;
            return Objects.toString(getPlainData(), "");
        }
        throw new UnsupportedFlavorException(flavor);
    }

    // --- richer subclass flavors ----------------------------------------------

    protected boolean isRicherFlavor(DataFlavor flavor) {
        DataFlavor[] richerFlavors = getRicherFlavors();
        int nFlavors = richerFlavors.length; //(richerFlavors != null) ? richerFlavors.length : 0;
        for (int i = 0; i < nFlavors; i++) {
            if (richerFlavors[i].equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Some subclasses will have flavors that are more descriptive than HTML
     * or plain text.  If this method returns a non-null value, it will be
     * placed at the start of the array of supported flavors.
     */
    protected DataFlavor[] getRicherFlavors() {
        return new DataFlavor[0]; //null;
    }

    protected Object getRicherData(DataFlavor flavor) throws UnsupportedFlavorException {
        return null;
    }

    // --- html flavors ----------------------------------------------------------

    /**
     * Returns whether or not the specified data flavor is an HTML flavor that
     * is supported.
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is supported
     */
    protected boolean isHTMLFlavor(DataFlavor flavor) {
        DataFlavor[] flavors = htmlFlavors;
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Should the HTML flavors be offered?  If so, the method
     * getHTMLData should be implemented to provide something reasonable.
     */
    protected boolean isHTMLSupported() {
        return htmlData != null;
    }

    /**
     * Fetch the data in a text/html format
     */
    protected String getHTMLData() {
        return htmlData;
    }

    // --- plain text flavors ----------------------------------------------------

    /**
     * Returns whether or not the specified data flavor is an plain flavor that
     * is supported.
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is supported
     */
    protected boolean isPlainFlavor(DataFlavor flavor) {
        DataFlavor[] flavors = plainFlavors;
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Should the plain text flavors be offered?  If so, the method
     * getPlainData should be implemented to provide something reasonable.
     */
    protected boolean isPlainSupported() {
        return plainData != null;
    }

    /**
     * Fetch the data in a text/plain format.
     */
    protected String getPlainData() {
        return plainData;
    }

    // --- string flavorss --------------------------------------------------------

    /**
     * Returns whether or not the specified data flavor is a String flavor that
     * is supported.
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is supported
     */
    protected boolean isStringFlavor(DataFlavor flavor) {
        DataFlavor[] flavors = stringFlavors;
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(flavor)) {
                return true;
            }
        }
        return false;
    }
}
