// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String PROTOTYPE = String.join("", Collections.nCopies(20, "M"));

  private MainPanel() {
    super(new BorderLayout());
    ComboBoxModel<String> model1 = new DefaultComboBoxModel<>(new String[] {"a", "b", "c"});
    JComboBox<String> combo1 = new JComboBox<>(model1);
    combo1.setEditable(false);
    // ((JTextField) combo1.getEditor().getEditorComponent()).setColumns(20);

    JComboBox<String> combo2 = new JComboBox<>(model1);
    combo2.setPrototypeDisplayValue(PROTOTYPE);

    JComboBox<String> combo3 = new JComboBox<>(model1);
    combo3.setPrototypeDisplayValue(PROTOTYPE);
    combo3.setEditable(true);

    ComboBoxModel<WebSite> model2 = new DefaultComboBoxModel<>(new WebSite[] {
        new WebSite("a", new ColorIcon(Color.RED)),
        new WebSite("b", new ColorIcon(Color.GREEN)),
        new WebSite("c", new ColorIcon(Color.BLUE))});
    JComboBox<WebSite> combo4 = new JComboBox<WebSite>(model2) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        setRenderer(new SiteListCellRenderer<>());
      }
    };

    JComboBox<WebSite> combo5 = new JComboBox<WebSite>(model2) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        setRenderer(new SiteListCellRenderer<>());
        setPrototypeDisplayValue(new WebSite(PROTOTYPE, new ColorIcon(Color.GRAY)));
      }
    };

    JComboBox<WebSite> combo6 = new JComboBox<WebSite>() {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        setRenderer(new SiteListCellRenderer<>());
        setPrototypeDisplayValue(new WebSite(PROTOTYPE, new ColorIcon(Color.GRAY)));
      }
    };
    add(makeListPanel(combo1, combo2, combo3, combo4, combo5, combo6));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel makeListPanel(Component... l) {
    SpringLayout layout = new SpringLayout();
    JPanel p = new JPanel(layout);
    layout.putConstraint(SpringLayout.WEST, l[0], 10, SpringLayout.WEST, p);
    layout.putConstraint(SpringLayout.WEST, l[1], 10, SpringLayout.WEST, p);
    layout.putConstraint(SpringLayout.WEST, l[2], 10, SpringLayout.WEST, p);
    layout.putConstraint(SpringLayout.WEST, l[3], 10, SpringLayout.WEST, p);
    layout.putConstraint(SpringLayout.WEST, l[4], 10, SpringLayout.WEST, p);
    layout.putConstraint(SpringLayout.WEST, l[5], 10, SpringLayout.WEST, p);
    layout.putConstraint(SpringLayout.NORTH, l[0], 10, SpringLayout.NORTH, p);
    layout.putConstraint(SpringLayout.NORTH, l[1], 10, SpringLayout.SOUTH, l[0]);
    layout.putConstraint(SpringLayout.NORTH, l[2], 10, SpringLayout.SOUTH, l[1]);
    layout.putConstraint(SpringLayout.NORTH, l[3], 10, SpringLayout.SOUTH, l[2]);
    layout.putConstraint(SpringLayout.NORTH, l[4], 10, SpringLayout.SOUTH, l[3]);
    layout.putConstraint(SpringLayout.NORTH, l[5], 10, SpringLayout.SOUTH, l[4]);
    Arrays.asList(l).forEach(p::add);
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

class WebSite {
  private final Icon favicon;
  private final String title;

  protected WebSite(String title, Icon favicon) {
    this.title = title;
    this.favicon = favicon;
  }

  public String getTitle() {
    return title;
  }

  public Icon getFavicon() {
    return favicon;
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
    g2.fillOval(4, 4, getIconWidth() - 8, getIconHeight() - 8);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 24;
  }

  @Override public int getIconHeight() {
    return 24;
  }
}

class SiteListCellRenderer<E extends WebSite> implements ListCellRenderer<E> {
  private final DefaultListCellRenderer renderer = new DefaultListCellRenderer();

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c = renderer.getListCellRendererComponent(
        list, value, index, isSelected, cellHasFocus);
    c.setEnabled(list.isEnabled());
    c.setFont(list.getFont());
    if (c instanceof JLabel && Objects.nonNull(value)) {
      JLabel l = (JLabel) c;
      l.setOpaque(index >= 0);
      l.setText(value.getTitle());
      l.setIcon(value.getFavicon());
    }
    if (isSelected) {
      c.setBackground(list.getSelectionBackground());
      c.setForeground(list.getSelectionForeground());
    } else {
      c.setBackground(list.getBackground());
      c.setForeground(list.getForeground());
    }
    return c;
  }
}
