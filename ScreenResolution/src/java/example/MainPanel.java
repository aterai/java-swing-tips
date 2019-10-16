// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private final Dimension defaultSize = new Dimension(320, 240);
  private Dimension preferredSize;

  public static float getSizeOfText() {
    int sr = Toolkit.getDefaultToolkit().getScreenResolution();
    float dpi = System.getProperty("os.name").startsWith("Windows") ? 96f : 72f;
    return sr / dpi;
  }

  @Override public Dimension getPreferredSize() {
    if (Objects.isNull(preferredSize)) {
      float sot = getSizeOfText();
      preferredSize = new Dimension((int) (defaultSize.width * sot), (int) (defaultSize.height * sot));
    }
    System.out.println(preferredSize);
    return preferredSize;
  }

  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
      {"aaa", 12, true}, {"bbb", 5, false},
      {"CCC", 92, true}, {"DDD", 0, false}
    };
    DefaultTableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model) {
      @Override public void updateUI() {
        super.updateUI();
        // @see BasicTableUI#installDefaults()
        // JTable's original row height is 16.  To correctly display the
        // contents on Linux we should have set it to 18, Windows 19 and
        // Solaris 20.  As these values vary so much it's too hard to
        // be backward compatible and try to update the row height, we're
        // therefor NOT going to adjust the row height based on font.  If the
        // developer changes the font, it's there responsibility to update
        // the row height.
        setRowHeight((int) (getRowHeight() * getSizeOfText()));
      }
    };
    table.setAutoCreateRowSorter(true);

    JTree tree = new JTree() {
      @Override public void updateUI() {
        super.updateUI();
        if (isFixedRowHeight()) {
          setRowHeight((int) (getRowHeight() * getSizeOfText()));
          System.out.println("Tree.rowHeight: " + getRowHeight());
        }
      }
    };
    // tree.setRowHeight(0);
    JSplitPane sp = new JSplitPane() {
      // // TEST:
      // @Override protected void paintComponent(Graphics g) {
      //   Graphics2D g2 = (Graphics2D) g.create();
      //   GraphicsConfiguration gc = g2.getDeviceConfiguration();
      //   System.out.println(gc.getDefaultTransform());
      //   g2.setTransform(gc.getDefaultTransform());
      //   System.out.println(gc.getNormalizingTransform());
      //   g2.transform(gc.getNormalizingTransform());
      //   super.paintComponent(g2);
      // }
    };
    sp.setLeftComponent(new JScrollPane(table));
    sp.setRightComponent(new JScrollPane(tree));
    sp.setResizeWeight(.5);
    add(sp);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
