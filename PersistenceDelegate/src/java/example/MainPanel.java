package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.beans.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    protected final JTextArea textArea = new JTextArea();

    protected final String[] columnNames = {"A", "B"};
    protected final Object[][] data = {
        {"aaa", "ccccccc"}, {"bbb", "\u2600\u2601\u2602\u2603"}
    };
    protected DefaultTableModel model = new DefaultTableModel(data, columnNames);

    public MainPanel() {
        super(new BorderLayout());

        JTable table = new JTable(model);
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setResizeWeight(.5);
        sp.setTopComponent(new JScrollPane(table));
        sp.setBottomComponent(new JScrollPane(textArea));

        JButton encButton = new JButton("XMLEncoder");
        encButton.addActionListener(e -> {
            try {
                File file = File.createTempFile("output", ".xml");
                // try (XMLEncoder xe = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)))) {
                try (XMLEncoder xe = new XMLEncoder(new BufferedOutputStream(Files.newOutputStream(file.toPath())))) {
                    xe.setPersistenceDelegate(DefaultTableModel.class, new DefaultTableModelPersistenceDelegate());
//                     xe.setExceptionListener(new ExceptionListener() {
//                         @Override public void exceptionThrown(Exception ex) {
//                             // XXX: ex.printStackTrace();
//                         }
//                     });
                    xe.writeObject(model);
                    // xe.flush();
                    // xe.close();
                }
                // try (Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                try (Reader r = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                    textArea.read(r, "temp");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        JButton decButton = new JButton("XMLDecoder");
        decButton.addActionListener(e -> {
            String text = textArea.getText();
            if (text.isEmpty()) {
                return;
            }
            try (XMLDecoder xd = new XMLDecoder(new BufferedInputStream(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))))) {
                model = (DefaultTableModel) xd.readObject();
                table.setModel(model);
            }
        });

        JButton clearButton = new JButton("clear");
        clearButton.addActionListener(e -> {
            model = new DefaultTableModel();
            table.setModel(model);
        });

        JPanel p = new JPanel();
        p.add(encButton);
        p.add(decButton);
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
class DefaultTableModelPersistenceDelegate extends DefaultPersistenceDelegate {
    @Override protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder encoder) {
        super.initialize(type, oldInstance, newInstance, encoder);
        DefaultTableModel m = (DefaultTableModel) oldInstance;
//         Vector v = m.getDataVector();
//         for (int i = 0; i < m.getRowCount(); i++) {
//             encoder.writeStatement(new Statement(oldInstance, "addRow", new Object[] { (Vector) v.get(i) }));
//         }
        for (int row = 0; row < m.getRowCount(); row++) {
            for (int col = 0; col < m.getColumnCount(); col++) {
                Object[] o = {m.getValueAt(row, col), row, col};
                encoder.writeStatement(new Statement(oldInstance, "setValueAt", o));
            }
        }
    }
}
