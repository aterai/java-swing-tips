// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
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
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"A", "B"};
    Object[][] data = {
        {"aaa", "1234567890"},
        {"bbb", "☀☁☂☃"},
    };
    JTable table = new JTable(new DefaultTableModel(data, columnNames));

    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sp.setResizeWeight(.5);
    sp.setTopComponent(new JScrollPane(table));
    sp.setBottomComponent(new JScrollPane(log));

    JButton encodeButton = new JButton("XMLEncoder");
    encodeButton.addActionListener(e -> encode(table));

    JButton decodeButton = new JButton("XMLDecoder");
    decodeButton.addActionListener(e -> decode(table));

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

  private void encode(JTable table) {
    try {
      Path path = File.createTempFile("output", ".xml").toPath();
      // try (XMLEncoder xe = new XMLEncoder(new FileOutputStream(file))) {
      try (XMLEncoder xe = new XMLEncoder(getOutputStream(path))) {
        Class<DefaultTableModel> clz = DefaultTableModel.class;
        xe.setPersistenceDelegate(clz, new DefaultTableModelPersistenceDelegate());
        // xe.setExceptionListener(new ExceptionListener() {
        //   @Override public void exceptionThrown(Exception ex) {
        //     // XXX: ex.printStackTrace();
        //   }
        // });
        xe.writeObject(clz.cast(table.getModel()));
        // xe.flush();
        // xe.close();
      }
      // try (Reader r = new BufferedReader(
      //     new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
      try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
        log.read(r, "temp");
      }
    } catch (IOException ex) {
      // ex.printStackTrace();
      log.setText(ex.getMessage());
    }
  }

  private void decode(JTable table) {
    String text = log.getText();
    if (!text.isEmpty()) {
      try (XMLDecoder xd = new XMLDecoder(getInputStream(text))) {
        DefaultTableModel m = (DefaultTableModel) xd.readObject();
        table.setModel(m);
      }
    }
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
      Logger.getGlobal().severe(ex::getMessage);
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
class DefaultTableModelPersistenceDelegate extends DefaultPersistenceDelegate {
  @Override protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder encoder) {
    super.initialize(type, oldInstance, newInstance, encoder);
    DefaultTableModel m = (DefaultTableModel) oldInstance;
    // Vector v = m.getDataVector();
    // for (int i = 0; i < m.getRowCount(); i++) {
    //   Object[] o = new Object[] { (Vector) v.get(i) };
    //   encoder.writeStatement(new Statement(oldInstance, "addRow", o));
    // }
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
