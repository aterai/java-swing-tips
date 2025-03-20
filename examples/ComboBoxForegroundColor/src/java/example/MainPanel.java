// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ColorItem[] model = {
        new ColorItem(Color.RED, "Red"),
        new ColorItem(Color.GREEN, "Green"),
        new ColorItem(Color.BLUE, "Blue"),
        new ColorItem(Color.CYAN, "Cyan"),
        new ColorItem(Color.ORANGE, "Orange"),
        new ColorItem(Color.MAGENTA, "Magenta")};

    JComboBox<ColorItem> combo00 = new JComboBox<>(model);

    JComboBox<ColorItem> combo01 = new JComboBox<ColorItem>(model) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        setRenderer(new ComboForegroundRenderer<>(this));
      }
    };

    JComboBox<ColorItem> combo02 = new JComboBox<ColorItem>(model) {
      @Override public void updateUI() {
        super.updateUI();
        setRenderer(new ComboHtmlRenderer<>());
      }
    };

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("default:", combo00));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("setForeground:", combo01));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("html tag:", combo02));
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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

class ColorItem implements Serializable {
  private static final long serialVersionUID = 1L;
  private final Color color;
  private final String description;

  protected ColorItem(Color color, String description) {
    this.color = color;
    this.description = description;
  }

  public Color getColor() {
    return color;
  }

  @Override public int hashCode() {
    return Objects.hash(color, description);
  }

  @Override public boolean equals(Object o) {
    return this == o || o instanceof ColorItem && equals2((ColorItem) o);
  }

  private boolean equals2(ColorItem item) {
    boolean b1 = Objects.equals(item.getColor(), color);
    boolean b2 = Objects.equals(item.toString(), description);
    return b1 && b2;
  }

  @Override public String toString() {
    return description;
  }
}

class ComboForegroundRenderer<E extends ColorItem> implements ListCellRenderer<E> {
  private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();
  private final Color color = new Color(0xF0_F5_FA);
  private final JComboBox<E> combo;

  protected ComboForegroundRenderer(JComboBox<E> combo) {
    super();
    this.combo = combo;
  }

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    Color ic = value.getColor();
    if (index < 0 && Objects.nonNull(ic) && !ic.equals(combo.getForeground())) {
      combo.setForeground(ic); // Windows, Motif Look&Feel
      list.setSelectionForeground(ic);
      list.setSelectionBackground(color);
    }
    Component c = renderer.getListCellRendererComponent(
        list, value, index, isSelected, cellHasFocus);
    c.setForeground(ic);
    c.setBackground(isSelected ? color : list.getBackground());
    // ((JLabel) c).setText(item.description);
    return c;
  }
}

class ComboHtmlRenderer<E extends ColorItem> implements ListCellRenderer<E> {
  private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();
  private final Color color = new Color(0xF0_F5_FA);

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    if (index < 0) {
      list.setSelectionBackground(color);
    }
    Component c = renderer.getListCellRendererComponent(
        list, value, index, isSelected, cellHasFocus);
    c.setBackground(isSelected ? color : list.getBackground());
    if (c instanceof JLabel) {
      int rgb = value.getColor().getRGB() & 0xFF_FF_FF;
      String description = Objects.toString(value);
      ((JLabel) c).setText(String.format("<html><font color='#%06X'>%s", rgb, description));
      // ((JLabel) c).setText("<html><font color=" + hex(item.color) + ">" + description);
    }
    return c;
  }

  // private static String hex(Color c) {
  //   return String.format("#%06X", c.getRGB() & 0xFF_FF_FF);
  // }
}
