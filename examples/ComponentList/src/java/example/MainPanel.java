// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final Box box = Box.createVerticalBox();
  private final Component glue = Box.createVerticalGlue();

  private MainPanel() {
    super(new BorderLayout());
    box.setBorder(BorderFactory.createLineBorder(Color.RED, 10));
    JScrollPane scroll = new JScrollPane(box);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getVerticalScrollBar().setUnitIncrement(25);
    add(makeToolBar(), BorderLayout.NORTH);
    add(scroll);
    addComp(new JLabel("aaaaaaaaaaaaaaaaaaaaaa"));
    addComp(MakeComponentUtils.makeButton());
    addComp(MakeComponentUtils.makeCheckBox());
    addComp(MakeComponentUtils.makeLabel());
    setPreferredSize(new Dimension(320, 240));
  }

  private void addComp(JComponent comp) {
    comp.setMaximumSize(new Dimension(Short.MAX_VALUE, comp.getPreferredSize().height));
    box.remove(glue);
    box.add(Box.createVerticalStrut(5));
    box.add(comp);
    box.add(glue);
    box.revalidate();
    EventQueue.invokeLater(() -> comp.scrollRectToVisible(comp.getBounds()));
  }

  private JToolBar makeToolBar() {
    JButton addLabel = new JButton("add JLabel");
    addLabel.addActionListener(e -> addComp(MakeComponentUtils.makeLabel()));

    JButton addButton = new JButton("add JButton");
    addButton.addActionListener(e -> addComp(MakeComponentUtils.makeButton()));

    JButton addCheckBox = new JButton("add JCheckBox");
    addCheckBox.addActionListener(e -> addComp(MakeComponentUtils.makeCheckBox()));

    JToolBar bar = new JToolBar();
    bar.add(addLabel);
    bar.addSeparator();
    bar.add(addButton);
    bar.addSeparator();
    bar.add(addCheckBox);
    return bar;
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

final class MakeComponentUtils {
  private MakeComponentUtils() {
    /* Singleton */
  }

  public static JComponent makeLabel() {
    JLabel label = new JLabel("Height: 50") {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = 50;
        return d;
      }
    };
    label.setOpaque(true);
    label.setBackground(Color.YELLOW.brighter());
    return label;
  }

  public static JComponent makeButton() {
    JButton b = new JButton("Beep Test");
    b.addActionListener(e -> Toolkit.getDefaultToolkit().beep());
    return b;
  }

  public static JComponent makeCheckBox() {
    return new JCheckBox("bbbbbbbbbbbbbbbbbbbb", true);
  }
}
