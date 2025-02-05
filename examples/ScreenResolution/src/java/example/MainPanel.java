// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private final Dimension defaultSize = new Dimension(320, 240);
  private Dimension dpiPreferredSize;

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        /* @see BasicTableUI#installDefaults()
        JTable's original row height is 16. To correctly display the
        contents on Linux we should have set it to 18, Windows 19 and
        Solaris 20. As these values vary so much it's too hard to be
        backward compatible and try to update the row height, we're
        therefore NOT going to adjust the row height based on font.
        If the developer changes the font, it's there responsibility
        to update the row height.
        */
        setRowHeight((int) (getRowHeight() * getDpiScaling()));
      }
    };
    table.setAutoCreateRowSorter(true);

    JTree tree = new JTree() {
      @Override public void updateUI() {
        super.updateUI();
        if (isFixedRowHeight()) {
          setRowHeight((int) (getRowHeight() * getDpiScaling()));
          // System.out.println("Tree.rowHeight: " + getRowHeight());
        }
      }
    };
    // tree.setRowHeight(0);
    JScrollPane s1 = new JScrollPane(table);
    JScrollPane s2 = new JScrollPane(tree);
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, s1, s2);
    // // TEST:
    // JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, s1, s2) {
    //   @Override protected void paintComponent(Graphics g) {
    //     Graphics2D g2 = (Graphics2D) g.create();
    //     GraphicsConfiguration gc = g2.getDeviceConfiguration();
    //     System.out.println(gc.getDefaultTransform());
    //     g2.setTransform(gc.getDefaultTransform());
    //     System.out.println(gc.getNormalizingTransform());
    //     g2.transform(gc.getNormalizingTransform());
    //     super.paintComponent(g2);
    //   }
    // };
    split.setResizeWeight(.5);
    add(split);
  }

  public static float getDpiScaling() {
    int sr = Toolkit.getDefaultToolkit().getScreenResolution();
    float dpi = System.getProperty("os.name").startsWith("Windows") ? 96f : 72f;
    return sr / dpi;
  }

  @Override public Dimension getPreferredSize() {
    if (Objects.isNull(dpiPreferredSize)) {
      float s = getDpiScaling();
      float fw = defaultSize.width * s;
      float fh = defaultSize.height * s;
      dpiPreferredSize = new Dimension((int) fw, (int) fh);
    }
    // System.out.println(dpiPreferredSize);
    return dpiPreferredSize;
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
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
