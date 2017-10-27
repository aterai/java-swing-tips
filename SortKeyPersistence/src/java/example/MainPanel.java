package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTextArea textArea = new JTextArea();

        String[] columnNames = {"A", "B"};
        Object[][] data = {
            {"aaa", "ccccccc"}, {"bbb", "\u2600\u2601\u2602\u2603"}
        };
        JTable table = new JTable(new DefaultTableModel(data, columnNames));
        table.setAutoCreateRowSorter(true);

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setResizeWeight(.5);
        sp.setTopComponent(new JScrollPane(table));
        sp.setBottomComponent(new JScrollPane(textArea));

        JButton encodeButton = new JButton("XMLEncoder");
        encodeButton.addActionListener(e -> {
            try {
                File file = File.createTempFile("output", ".xml");
                //try (XMLEncoder xe = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)))) {
                try (XMLEncoder xe = new XMLEncoder(new BufferedOutputStream(Files.newOutputStream(file.toPath())))) {
                    xe.setPersistenceDelegate(RowSorter.SortKey.class, new DefaultPersistenceDelegate(new String[] {"column", "sortOrder"}));
                    xe.writeObject(table.getRowSorter().getSortKeys());

                    xe.setPersistenceDelegate(DefaultTableModel.class, new DefaultTableModelPersistenceDelegate());
                    xe.writeObject(table.getModel());
                }
                //try (Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                try (Reader r = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                    textArea.read(r, "temp");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        JButton decodeButton = new JButton("XMLDecoder");
        decodeButton.addActionListener(e -> {
            String text = textArea.getText();
            if (text.isEmpty()) {
                return;
            }
            try (XMLDecoder xd = new XMLDecoder(new BufferedInputStream(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))))) {
                @SuppressWarnings("unchecked")
                List<? extends RowSorter.SortKey> keys = (List<? extends RowSorter.SortKey>) xd.readObject();
                DefaultTableModel model = (DefaultTableModel) xd.readObject();
                table.setModel(model);
                table.setAutoCreateRowSorter(true);
                table.getRowSorter().setSortKeys(keys);
            }
        });

        JButton clearButton = new JButton("clear");
        clearButton.addActionListener(e -> table.setModel(new DefaultTableModel()));

        JPanel p = new JPanel();
        p.add(encodeButton);
        p.add(decodeButton);
        p.add(clearButton);
        add(sp);
        add(p, BorderLayout.SOUTH);
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

// http://web.archive.org/web/20090806075316/http://java.sun.com/products/jfc/tsc/articles/persistence4/
// http://www.oracle.com/technetwork/java/persistence4-140124.html
// https://ateraimemo.com/Swing/PersistenceDelegate.html
class DefaultTableModelPersistenceDelegate extends DefaultPersistenceDelegate {
    @Override protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder encoder) {
        super.initialize(type, oldInstance, newInstance, encoder);
        DefaultTableModel m = (DefaultTableModel) oldInstance;
        for (int row = 0; row < m.getRowCount(); row++) {
            for (int col = 0; col < m.getColumnCount(); col++) {
                Object[] o = {m.getValueAt(row, col), row, col};
                encoder.writeStatement(new Statement(oldInstance, "setValueAt", o));
            }
        }
    }
}
