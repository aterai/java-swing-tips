// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String code = "BC89FE5A";
    JFormattedTextField field0 = new JFormattedTextField(code);
    field0.setHorizontalAlignment(SwingConstants.RIGHT);
    field0.setColumns(8);

    JFormattedTextField field1 = new JFormattedTextField(code);
    field1.setHorizontalAlignment(SwingConstants.RIGHT);
    Font mono = new Font(Font.MONOSPACED, Font.PLAIN, field1.getFont().getSize());
    field1.setFont(mono);
    field1.setColumns(8);

    JFormattedTextField field2 = new JFormattedTextField(code);
    field2.setHorizontalAlignment(SwingConstants.RIGHT);
    Map<TextAttribute, Object> attr = new ConcurrentHashMap<>();
    attr.put(TextAttribute.TRACKING, -.011f);
    field2.setFont(mono.deriveFont(attr));
    field2.setColumns(8);

    JFormattedTextField field3 = new JFormattedTextField(code) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        // int caretMargin = UIManager.getInt("Caret.width");
        d.width += 1; // 1: caret width
        return d;
      }
    };
    field3.setHorizontalAlignment(SwingConstants.RIGHT);
    field3.setFont(mono);
    field3.setColumns(8);

    JFormattedTextField field4 = new JFormattedTextField(code);
    field4.setHorizontalAlignment(SwingConstants.RIGHT);
    field4.setFont(mono);
    field4.setColumns(9);

    AlignedLabel l0 = new AlignedLabel("Default:");
    AlignedLabel l1 = new AlignedLabel("MONOSPACED:");
    AlignedLabel l2 = new AlignedLabel("TRACKING-.011f:");
    AlignedLabel l3 = new AlignedLabel("PreferredSize+1:");
    AlignedLabel l4 = new AlignedLabel("Columns+1:");
    AlignedLabel.groupLabels(l0, l1, l2, l3, l4);

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel(l0, field0));
    box.add(makeTitledPanel(l1, field1));
    box.add(makeTitledPanel(l2, field2));
    box.add(makeTitledPanel(l3, field3));
    box.add(makeTitledPanel(l4, field4));

    JPanel p = new JPanel(new BorderLayout());
    p.add(box, BorderLayout.WEST);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(makeInfoTextArea(code, field0, field1, field2)));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTextArea makeInfoTextArea(String code, JTextField... list) {
    JTextArea log = new JTextArea();
    EventQueue.invokeLater(() -> {
      for (JTextField field : list) {
        Font font = field.getFont();
        FontRenderContext frc = field.getFontMetrics(font).getFontRenderContext();
        Rectangle2D r2 = font.getStringBounds(code, frc);
        Rectangle r = SwingUtilities.calculateInnerArea(field, null);
        log.append(String.format("%s%n  %s%n  %s%n", font, r, r2));
      }
    });
    return log;
  }

  private static Container makeTitledPanel(Component title, Component c) {
    JPanel box = new JPanel();
    box.add(title);
    box.add(c);
    return box;
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

class AlignedLabel extends JLabel {
  private static final int INDENT = 10;
  // private AlignedLabel[] group;
  private List<AlignedLabel> group;
  private int maxWidth;

  protected AlignedLabel(String text) {
    super(text, RIGHT);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    // Align the width with all other labels in group.
    d.width = getMaxWidth() + INDENT;
    return d;
  }

  private int getMaxWidth() {
    if (maxWidth == 0 && Objects.nonNull(group)) {
      int max = group.stream()
          .map(AlignedLabel::getSuperPreferredWidth)
          .reduce(0, Integer::max);
      group.forEach(al -> al.maxWidth = max);
    }
    return maxWidth;
  }

  private int getSuperPreferredWidth() {
    return super.getPreferredSize().width;
  }

  public static void groupLabels(AlignedLabel... list) {
    List<AlignedLabel> group = Arrays.asList(list);
    group.forEach(al -> al.group = group);
  }
}
