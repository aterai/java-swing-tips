// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JCheckBox check = new JCheckBox("vertical grid lines", true);
    check.addActionListener(e -> repaint());

    JTextPane textPane = new JTextPane() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (check.isSelected()) {
          int ox = getInsets().left;
          int h = getHeight();
          g.setColor(Color.RED);
          g.drawLine(ox, 0, ox, h);
          g.drawLine(ox + 100, 0, ox + 100, h);
          g.drawLine(ox + 200, 0, ox + 200, h);
          g.drawLine(ox + 300, 0, ox + 300, h);
          g.setColor(Color.ORANGE);
          g.drawLine(ox + 50, 0, ox + 50, h);
          g.drawLine(ox + 150, 0, ox + 150, h);
          g.drawLine(ox + 250, 0, ox + 250, h);
        }
      }
    };
    textPane.setText(String.join("\n",
        String.join("\t", "LEFT1", "CENTER1", "RIGHT1", "3.14"),
        String.join("\t", "LEFT22", "CENTER22", "RIGHT22", "12.3"),
        String.join("\t", "LEFT333", "CENTER333", "RIGHT333", "123.45"),
        String.join("\t", "LEFT4444", "CENTER4444", "RIGHT4444", "0.9876")));

    // MutableAttributeSet attr = new SimpleAttributeSet();
    Style attr = textPane.getStyle(StyleContext.DEFAULT_STYLE);
    StyleConstants.setTabSet(attr, new TabSet(new TabStop[] {
        new TabStop(0f, TabStop.ALIGN_LEFT, TabStop.LEAD_NONE),
        new TabStop(100f, TabStop.ALIGN_CENTER, TabStop.LEAD_NONE),
        new TabStop(200f, TabStop.ALIGN_RIGHT, TabStop.LEAD_NONE),
        new TabStop(250f, TabStop.ALIGN_DECIMAL, TabStop.LEAD_NONE)
        // new TabStop(300f, TabStop.ALIGN_BAR, TabStop.LEAD_NONE)
    }));
    textPane.setParagraphAttributes(attr, false);
    // textPane.getStyledDocument().setParagraphAttributes(0, doc.getLength(), attr, false);

    add(new JScrollPane(textPane));
    add(check, BorderLayout.SOUTH);
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
