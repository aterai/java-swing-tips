package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"Name", "Comment"};
    private final Object[][] data = {
        {"test1.jpg", "adfasd"},
        {"test1234.jpg", "  "},
        {"test15354.gif", "fasdf"},
        {"t.png", "comment"},
        {"tfasdfasd.jpg", "123"},
        {"afsdfasdfffffffffffasdfasdf.mpg", "test"},
        {"fffffffffffasdfasdf", ""},
        {"test1.jpg", ""}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
        @Override public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new FileListTable(model);

    public MainPanel() {
        super(new BorderLayout());

        InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke tab    = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        KeyStroke enter  = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        KeyStroke stab   = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK);
        KeyStroke senter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);
        im.put(tab, im.get(enter));
        im.put(stab, im.get(senter));

        final Color orgColor = table.getSelectionBackground();
        final Color tflColor = this.getBackground();
        table.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) {
                table.setSelectionForeground(Color.WHITE);
                table.setSelectionBackground(orgColor);
            }
            @Override public void focusLost(FocusEvent e) {
                table.setSelectionForeground(Color.BLACK);
                table.setSelectionBackground(tflColor);
            }
        });

        table.setComponentPopupMenu(new TablePopupMenu());
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }

    protected class CreateAction extends AbstractAction {
        public CreateAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            model.addRow(new Object[] {"new", ""});
            Rectangle rect = table.getCellRect(model.getRowCount() - 1, 0, true);
            table.scrollRectToVisible(rect);
        }
    }

    protected class DeleteAction extends AbstractAction {
        public DeleteAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            int[] selection = table.getSelectedRows();
            if (selection.length == 0) {
                return;
            }
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
    }

    protected class ClearAction extends AbstractAction {
        public ClearAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            table.clearSelection();
        }
    }

    protected class TablePopupMenu extends JPopupMenu {
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            add(new CreateAction("add", null));
            add(new ClearAction("clearSelection", null));
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            int[] l = table.getSelectedRows();
            deleteAction.setEnabled(l.length > 0);
            super.show(c, x, y);
        }
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

class SelectedImageFilter extends RGBImageFilter {
    //public SelectedImageFilter() {
    //    canFilterIndexColorModel = false;
    //}
    @Override public int filterRGB(int x, int y, int argb) {
        int r = (argb >> 16) & 0xff;
        int g = (argb >>  8) & 0xff;
        return (argb & 0xff0000ff) | ((r >> 1) << 16) | ((g >> 1) << 8);
        //return (argb & 0xffffff00) | ((argb & 0xff) >> 1);
    }
}

class FileNameRenderer implements TableCellRenderer {
    private final Dimension dim = new Dimension();
    private final JPanel p = new JPanel(new BorderLayout()) {
        @Override public Dimension getPreferredSize() {
            return dim;
        }
    };
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JLabel textLabel = new JLabel(" ");
    private final JLabel iconLabel;
    private final Border focusCellHighlightBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
    private final Border noFocusBorder;
    private final ImageIcon nicon;
    private final ImageIcon sicon;

    public FileNameRenderer(JTable table) {
        Border b = UIManager.getBorder("Table.noFocusBorder");
        if (Objects.isNull(b)) { //Nimbus???
            Insets i = focusCellHighlightBorder.getBorderInsets(textLabel);
            b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
        }
        noFocusBorder = b;

        p.setOpaque(false);
        panel.setOpaque(false);

        //http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
        nicon = new ImageIcon(getClass().getResource("wi0063-16.png"));
        sicon = new ImageIcon(p.createImage(new FilteredImageSource(nicon.getImage().getSource(), new SelectedImageFilter())));

        iconLabel = new JLabel(nicon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder());

        p.add(iconLabel, BorderLayout.WEST);
        p.add(textLabel);
        panel.add(p, BorderLayout.WEST);

        Dimension d = iconLabel.getPreferredSize();
        dim.setSize(d);
        table.setRowHeight(d.height);
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        textLabel.setFont(table.getFont());
        textLabel.setText(Objects.toString(value, ""));
        textLabel.setBorder(hasFocus ? focusCellHighlightBorder : noFocusBorder);

        FontMetrics fm = table.getFontMetrics(table.getFont());
        Insets i = textLabel.getInsets();
        int swidth = iconLabel.getPreferredSize().width + fm.stringWidth(textLabel.getText()) + i.left + i.right;
        int cwidth = table.getColumnModel().getColumn(column).getWidth();
        dim.width = swidth > cwidth ? cwidth : swidth;

        if (isSelected) {
            textLabel.setOpaque(true);
            textLabel.setForeground(table.getSelectionForeground());
            textLabel.setBackground(table.getSelectionBackground());
            iconLabel.setIcon(sicon);
        } else {
            textLabel.setOpaque(false);
            textLabel.setForeground(table.getForeground());
            textLabel.setBackground(table.getBackground());
            iconLabel.setIcon(nicon);
        }
        return panel;
    }
}

class FileListTable extends JTable {
    public FileListTable(TableModel model) {
        super(model);
    }
    @Override public void updateUI() {
        // Bug ID: 6788475 Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
        // http://bugs.java.com/view_bug.do?bug_id=6788475
        // XXX: set dummy ColorUIResource
        setSelectionForeground(new ColorUIResource(Color.RED));
        setSelectionBackground(new ColorUIResource(Color.RED));
        super.updateUI();

        putClientProperty("Table.isFileList", Boolean.TRUE);
        setCellSelectionEnabled(true);
        setIntercellSpacing(new Dimension());
        setShowGrid(false);
        setAutoCreateRowSorter(true);
        setFillsViewportHeight(true);

        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return super.getTableCellRendererComponent(table, value, false, false, row, column);
            }
        });

        TableColumn col = getColumnModel().getColumn(0);
        col.setCellRenderer(new FileNameRenderer(this));
        col.setPreferredWidth(200);
        col = getColumnModel().getColumn(1);
        col.setPreferredWidth(300);
    }
}
