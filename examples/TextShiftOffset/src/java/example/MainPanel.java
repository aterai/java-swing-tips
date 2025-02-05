// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    // System.out.println(UIManager.getInt("Button.textShiftOffset"));
    UIManager.put("Button.textShiftOffset", 0);

    List<JRadioButton> rl = Arrays.asList(
        new JRadioButton(new TextShiftOffsetAction(0)),
        new JRadioButton(new TextShiftOffsetAction(1)),
        new JRadioButton(new TextShiftOffsetAction(-1)));
    ButtonGroup bg = new ButtonGroup();
    Box box = Box.createHorizontalBox();
    String title = "UIManager.put(\"Button.textShiftOffset\", offset)";
    box.setBorder(BorderFactory.createTitledBorder(title));
    box.add(new JLabel("offset = "));
    boolean isFirst = true;
    for (JRadioButton rb : rl) {
      if (isFirst) {
        rb.setSelected(true);
        isFirst = false;
      }
      bg.add(rb);
      box.add(rb);
      box.add(Box.createHorizontalStrut(5));
    }
    box.add(Box.createHorizontalGlue());

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(new JButton("JButton"));
    p.add(new JButton(UIManager.getIcon("FileChooser.detailsViewIcon")));
    p.add(new JButton("<html>JButton<br>html<br>tag<br>test"));
    p.add(new JToggleButton("JToggleButton"));

    add(box, BorderLayout.NORTH);
    add(p);
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

class TextShiftOffsetAction extends AbstractAction {
  private final int offset;

  protected TextShiftOffsetAction(int offset) {
    super(String.format(" %d ", offset));
    this.offset = offset;
  }

  @Override public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    if (o instanceof JComponent) {
      JComponent c = (JComponent) o;
      UIManager.put("Button.textShiftOffset", offset);
      SwingUtilities.updateComponentTreeUI(c.getTopLevelAncestor());
    }
  }
}
