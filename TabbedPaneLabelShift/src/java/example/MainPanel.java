// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    JTextArea log = new JTextArea();
    String key1 = "TabbedPane.selectedLabelShift";
    log.append(getDefaultsText(key1));
    String key2 = "TabbedPane.labelShift";
    log.append(getDefaultsText(key2));

    int iv1 = UIManager.getLookAndFeelDefaults().getInt(key1);
    SpinnerNumberModel slsModel = new SpinnerNumberModel(iv1, -5, 5, 1);
    slsModel.addChangeListener(e -> {
      SpinnerNumberModel source = (SpinnerNumberModel) e.getSource();
      Integer offset = source.getNumber().intValue();
      UIManager.put(key1, offset);
      SwingUtilities.updateComponentTreeUI(getTopLevelAncestor());
    });

    int iv2 = UIManager.getLookAndFeelDefaults().getInt(key2);
    SpinnerNumberModel lsModel = new SpinnerNumberModel(iv2, -5, 5, 1);
    lsModel.addChangeListener(e -> {
      SpinnerNumberModel source = (SpinnerNumberModel) e.getSource();
      Integer offset = source.getNumber().intValue();
      UIManager.put(key2, offset);
      SwingUtilities.updateComponentTreeUI(getTopLevelAncestor());
    });

    String title1 = String.format("UIManager.put(\"%s\", offset)", key1);
    Box box1 = Box.createHorizontalBox();
    box1.setBorder(BorderFactory.createTitledBorder(title1));
    box1.add(new JLabel("offset = "));
    box1.add(new JSpinner(slsModel));
    box1.add(Box.createHorizontalGlue());

    String title2 = String.format("UIManager.put(\"%s\", offset)", key2);
    Box box2 = Box.createHorizontalBox();
    box2.setBorder(BorderFactory.createTitledBorder(title2));
    box2.add(new JLabel("offset = "));
    box2.add(new JSpinner(lsModel));
    box2.add(Box.createHorizontalGlue());

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(box1);
    p.add(box2);

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("title 0", new ColorIcon(Color.RED), new JScrollPane(log));
    tabs.addTab("title 1", new ColorIcon(Color.GREEN), new JButton("button"));
    tabs.addTab("title 2", new ColorIcon(Color.BLUE), new JLabel("label"));
    tabs.addTab("title 3", new JPanel());
    tabs.setTabComponentAt(3, new JLabel("lbl", new ColorIcon(Color.CYAN), SwingConstants.LEFT));

    add(p, BorderLayout.NORTH);
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  private static String getDefaultsText(String key) {
    return String.format("%s: %s%n", key, UIManager.getLookAndFeelDefaults().get(key));
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

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}
