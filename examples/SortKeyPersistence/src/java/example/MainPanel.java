// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.PersistenceDelegate;
import java.beans.Statement;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"A", "B"};
    Object[][] data = {
        {"aaa", "1234567890"},
        {"bbb", "☀☁☂☃"}
    };
    JTable table = new JTable(new DefaultTableModel(data, columnNames));
    table.setAutoCreateRowSorter(true);

    JTextArea textArea = new JTextArea();

    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sp.setResizeWeight(.5);
    sp.setTopComponent(new JScrollPane(table));
    sp.setBottomComponent(new JScrollPane(textArea));

    JButton encodeButton = new JButton("XMLEncoder");
    encodeButton.addActionListener(e -> {
      try {
        Path path = File.createTempFile("output", ".xml").toPath();
        try (XMLEncoder xe = new XMLEncoder(getOutputStream(path))) {
          String[] constructors = {"column", "sortOrder"};
          PersistenceDelegate d = new DefaultPersistenceDelegate(constructors);
          xe.setPersistenceDelegate(RowSorter.SortKey.class, d);
          xe.writeObject(table.getRowSorter().getSortKeys());

          xe.setPersistenceDelegate(
              DefaultTableModel.class, new DefaultTableModelPersistenceDelegate());
          xe.writeObject(table.getModel());
        }
        // try (Reader r = new BufferedReader(
        //    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
        try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
          textArea.read(r, "temp");
        }
      } catch (IOException ex) {
        ex.printStackTrace();
        textArea.setText(ex.getMessage());
      }
    });

    JButton decodeButton = new JButton("XMLDecoder");
    decodeButton.addActionListener(e -> {
      String text = textArea.getText();
      if (text.isEmpty()) {
        return;
      }
      try (XMLDecoder xd = new XMLDecoder(getInputStream(text))) {
        // @SuppressWarnings("unchecked")
        // var keys = (List<? extends RowSorter.SortKey>) xd.readObject();
        Class<RowSorter.SortKey> clz = RowSorter.SortKey.class;
        List<? extends RowSorter.SortKey> keys = ((List<?>) xd.readObject()).stream()
            .filter(clz::isInstance)
            .map(clz::cast)
            .collect(Collectors.toList());
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

  private BufferedOutputStream getOutputStream(Path path) throws IOException {
    return new BufferedOutputStream(Files.newOutputStream(path));
  }

  private BufferedInputStream getInputStream(String text) {
    byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
    return new BufferedInputStream(new ByteArrayInputStream(bytes));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// https://www.oracle.com/technical-resources/articles/java/persistence4.html
// https://ateraimemo.com/Swing/PersistenceDelegate.html
class DefaultTableModelPersistenceDelegate extends DefaultPersistenceDelegate {
  @Override protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder encoder) {
    super.initialize(type, oldInstance, newInstance, encoder);
    DefaultTableModel m = (DefaultTableModel) oldInstance;
    for (int row = 0; row < m.getRowCount(); row++) {
      for (int col = 0; col < m.getColumnCount(); col++) {
        encoder.writeStatement(getSetValueAt(oldInstance, m.getValueAt(row, col), row, col));
      }
    }
  }

  private Statement getSetValueAt(Object oldInstance, Object... o) {
    return new Statement(oldInstance, "setValueAt", o);
  }
}
