// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextPane textPane = new JTextPane();
    textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    textPane.replaceSelection(" Default: ");
    textPane.insertComponent(new JCheckBox("CheckBox"));
    // textPane.setEditable(false);

    JCheckBox check1 = new JCheckBox("JComponent#setAlignmentY(...)");
    Dimension d = check1.getPreferredSize();
    int baseline = check1.getBaseline(d.width, d.height);
    // System.out.println(check1.getAlignmentY());
    check1.setAlignmentY(baseline / (float) d.height);
    textPane.replaceSelection("\n\n Baseline: ");
    textPane.insertComponent(check1);

    JCheckBox check2 = new JCheckBox("setAlignmentY+setCursor+...");
    check2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    check2.setOpaque(false);
    check2.setFocusable(false);
    d = check2.getPreferredSize();
    baseline = check2.getBaseline(d.width, d.height);
    check2.setAlignmentY(baseline / (float) d.height);
    textPane.replaceSelection("\n\n Baseline+Cursor: ");
    textPane.insertComponent(check2);

    add(new JScrollPane(textPane));
    setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    setPreferredSize(new Dimension(320, 240));
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
