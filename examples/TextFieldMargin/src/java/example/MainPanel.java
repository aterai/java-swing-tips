// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.InsetsUIResource;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    JTextField field1 = new JTextField("1111111111");
    Insets m = field1.getMargin();
    log.append(m.toString() + "\n");
    Insets margin = new Insets(m.top, m.left + 10, m.bottom, m.right);
    field1.setMargin(margin);

    JTextField field2 = new JTextField("2222222222222");
    Border b1 = BorderFactory.createEmptyBorder(0, 20, 0, 0);
    Border b2 = BorderFactory.createCompoundBorder(field2.getBorder(), b1);
    field2.setBorder(b2);

    Box box = Box.createVerticalBox();
    box.add(makePanel(new JTextField("000000000000000000")));
    box.add(Box.createVerticalStrut(5));
    box.add(makePanel(field1));
    box.add(Box.createVerticalStrut(5));
    box.add(makePanel(field2));
    add(box, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private int getLeftMargin(JTextField c) {
    log.append("----\n");
    log.append("getMargin().left: " + c.getMargin().left + "\n");
    log.append("getInsets().left: " + c.getInsets().left + "\n");
    Border bdr = c.getBorder();
    if (bdr != null) {
      log.append("getBorder().getBorderInsets(c).left: " + bdr.getBorderInsets(c).left + "\n");
    }
    return c.getInsets().left;
  }

  private Component makePanel(JTextField field) {
    JPanel p = new JPanel(new BorderLayout());
    String title = "left margin = " + getLeftMargin(field);
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(field);
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      Insets m = UIManager.getInsets("TextField.margin");
      Insets ins = new InsetsUIResource(m.top, m.left + 5, m.bottom, m.right);
      UIManager.put("TextField.margin", ins);
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
