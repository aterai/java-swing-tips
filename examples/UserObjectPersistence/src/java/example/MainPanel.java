// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.beans.DefaultPersistenceDelegate;
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

public final class MainPanel extends JPanel {
  private final JTextArea textArea = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new TriStateCheckBoxTree();
    tree.setEditable(true);
    tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    tree.expandRow(0);
    // tree.setToggleClickCount(1);

    JButton save = new JButton("save");
    save.addActionListener(e -> saveTree(tree));

    JButton load = new JButton("load");
    load.addActionListener(e -> loadTree(tree));

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(save);
    box.add(Box.createHorizontalStrut(4));
    box.add(load);

    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sp.setResizeWeight(.5);
    sp.setTopComponent(new JScrollPane(tree));
    sp.setBottomComponent(new JScrollPane(textArea));

    add(sp);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private void saveTree(JTree tree) {
    try {
      Path path = File.createTempFile("output", ".xml").toPath();
      String[] names = {"label", "status"};
      try (XMLEncoder xe = new XMLEncoder(getOutputStream(path))) {
        xe.setPersistenceDelegate(CheckBoxNode.class, new DefaultPersistenceDelegate(names));
        xe.writeObject(tree.getModel());
      }
      try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
        textArea.read(r, "temp");
      }
    } catch (IOException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      textArea.setText(ex.getMessage());
    }
  }

  private void loadTree(JTree tree) {
    String text = textArea.getText();
    if (!text.isEmpty()) {
      try (XMLDecoder xd = new XMLDecoder(getInputStream(text))) {
        DefaultTreeModel m = (DefaultTreeModel) xd.readObject();
        m.addTreeModelListener(new CheckBoxStatusUpdateListener());
        tree.setModel(m);
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

class TriStateCheckBoxTree extends JTree {
  protected TriStateCheckBoxTree() {
    this(getDefaultTreeModel());
  }

  protected TriStateCheckBoxTree(TreeModel model) {
    super(model);
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
    // Java 9: Collections.list(root.breadthFirstEnumeration()).stream()
    Collections.list((Enumeration<?>) root.breadthFirstEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .forEach(n -> {
          String title = Objects.toString(n.getUserObject(), "");
          n.setUserObject(new CheckBoxNode(title, Status.DESELECTED));
        });
    model.addTreeModelListener(new CheckBoxStatusUpdateListener());
  }

  @Override public void updateUI() {
    setCellRenderer(null);
    setCellEditor(null);
    super.updateUI();
    // ???#1: JDK 1.6.0 bug??? Nimbus LnF
    setCellRenderer(new CheckBoxNodeRenderer());
    setCellEditor(new CheckBoxNodeEditor());
  }
}

class TriStateCheckBox extends JCheckBox {
  @Override public void updateUI() {
    Icon currentIcon = getIcon();
    setIcon(null);
    super.updateUI();
    if (Objects.nonNull(currentIcon)) {
      setIcon(new IndeterminateIcon());
    }
    setOpaque(false);
  }
}

class IndeterminateIcon implements Icon {
  private static final Color FOREGROUND = new Color(0xC8_32_14_FF, true);
  // TEST: private static final Color FOREGROUND = UIManager.getColor("CheckBox.foreground");
  private static final int MARGIN = 4;
  private static final int HEIGHT = 2;
  private final Icon icon = UIManager.getIcon("CheckBox.icon");

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    icon.paintIcon(c, g2, 0, 0);
    g2.setPaint(FOREGROUND);
    g2.fillRect(MARGIN, (getIconHeight() - HEIGHT) / 2, getIconWidth() - MARGIN - MARGIN, HEIGHT);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return icon.getIconWidth();
  }

  @Override public int getIconHeight() {
    return icon.getIconHeight();
  }
}
