// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String path = "example/16x16.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Icon icon = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return UIManager.getIcon("html.missingImage");
      }
    }).orElseGet(() -> UIManager.getIcon("html.missingImage"));

    // JComboBox<String> combo01 = new JComboBox<>(makeModel());

    JComboBox<String> combo02 = new JComboBox<>(makeModel());
    ComboBoxUtils.initComboBoxRenderer(combo02, icon);

    JComboBox<String> combo03 = new JComboBox<>(makeModel());
    combo03.setEditable(true);
    ComboBoxUtils.initComboBoxRenderer(combo03, icon);

    // JComboBox<String> combo04 = new JComboBox<String>(makeModel()) {
    //   @Override public void updateUI() {
    //     setBorder(null);
    //     super.updateUI();
    //     setEditable(true);
    //     ComboBoxUtils.initComboBoxRenderer(this, icon);
    //     setBorder(ComboBoxUtils.makeIconComboBorder(this, icon));
    //   }
    // };

    JComboBox<String> combo05 = new JComboBox<String>(makeModel()) {
      @Override public void updateUI() {
        setBorder(null);
        setRenderer(null);
        super.updateUI();
        setEditable(true);
        ComboBoxUtils.initComboBoxRenderer(this, icon);
        ComboBoxUtils.initIconComboBorder1(this, icon);
      }
    };

    JComboBox<String> combo06 = new JComboBox<String>(makeModel()) {
      @Override public void updateUI() {
        setBorder(null);
        setRenderer(null);
        super.updateUI();
        setEditable(true);
        ComboBoxUtils.initComboBoxRenderer(this, icon);
        ComboBoxUtils.initIconComboBorder2(this, icon);
      }
    };

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("setEditable(false)", combo02));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("setEditable(true)", combo03, combo05, combo06));
    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<String> makeModel() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("aaa");
    model.addElement("aaa, bbb");
    model.addElement("aaa, bbb, cc");
    model.addElement("ccc, ccc, ccc, ccc, ccc");
    model.addElement("bbb1");
    model.addElement("bbb12");
    return model;
  }

  private static Component makeTitledPanel(String title, Component... list) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    c.weightx = 1d;
    c.gridx = GridBagConstraints.REMAINDER;
    for (Component cmp : list) {
      p.add(cmp, c);
    }
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

final class ComboBoxUtils {
  private ComboBoxUtils() {
    /* Singleton */
  }

  // public static Border makeIconComboBorder(JComponent comp, ImageIcon icon) {
  //   Icon wrappedIcon = new Icon() {
  //     @Override public void paintIcon(Component c, Graphics g, int x, int y) {
  //       g.translate(x, y);
  //       int ih = icon.getIconHeight();
  //       int ch = getIconHeight();
  //       int yy = Math.max((ch - ih) / 2, 0); // ch - ih > 0 ? (ch - ih) / 2 : 0;
  //       g.drawImage(icon.getImage(), 0, yy, c);
  //       g.translate(-x, -y);
  //     }
  //
  //     @Override public int getIconWidth() {
  //       return icon.getIconWidth();
  //     }
  //
  //     @Override public int getIconHeight() {
  //       Insets is = comp.getInsets();
  //       return comp.getPreferredSize().height - is.top - is.bottom;
  //     }
  //   };
  //   Border b1 = BorderFactory.createMatteBorder(0, icon.getIconWidth(), 0, 0, wrappedIcon);
  //   Border b2 = BorderFactory.createEmptyBorder(0, 5, 0, 0);
  //   Border b3 = BorderFactory.createCompoundBorder(b1, b2);
  //   return BorderFactory.createCompoundBorder(comp.getBorder(), b3);
  // }

  public static void initIconComboBorder1(JComboBox<?> comboBox, Icon icon) {
    JTextField comp = (JTextField) comboBox.getEditor().getEditorComponent();
    Icon wrappedIcon = new Icon() {
      @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        int ih = icon.getIconHeight();
        int ch = getIconHeight();
        int yy = Math.max(Math.round((ch - ih) / 2f), 0);
        icon.paintIcon(c, g2, 0, yy);
        g2.dispose();
      }

      @Override public int getIconWidth() {
        return icon.getIconWidth();
      }

      @Override public int getIconHeight() {
        Insets is = comp.getInsets();
        return comp.getPreferredSize().height - is.top - is.bottom;
      }
    };
    Border b1 = BorderFactory.createMatteBorder(0, icon.getIconWidth(), 0, 0, wrappedIcon);
    Border b2 = BorderFactory.createEmptyBorder(0, 5, 0, 0);
    Border b3 = BorderFactory.createCompoundBorder(b1, b2);
    comp.setBorder(BorderFactory.createCompoundBorder(comp.getBorder(), b3));
  }

  public static void initIconComboBorder2(JComboBox<?> comboBox, Icon icon) {
    EventQueue.invokeLater(() -> {
      Border margin = BorderFactory.createEmptyBorder(0, icon.getIconWidth() + 2, 0, 2);

      JTextField c = (JTextField) comboBox.getEditor().getEditorComponent();
      Border b = c.getBorder();
      c.setBorder(BorderFactory.createCompoundBorder(b, margin));

      JLabel label = new JLabel(icon);
      label.setCursor(Cursor.getDefaultCursor());
      label.setBorder(BorderFactory.createEmptyBorder());
      c.add(label);

      int ih = icon.getIconHeight();
      int ch = comboBox.getPreferredSize().height;
      // int ch = c.getPreferredSize().height; // Nimbus???
      int yy = Math.max(Math.round((ch - ih) / 2f), 0);
      label.setBounds(b.getBorderInsets(c).left, yy, icon.getIconWidth(), icon.getIconHeight());
    });
  }

  public static <E> void initComboBoxRenderer(JComboBox<E> combo, Icon icon) {
    ListCellRenderer<? super E> renderer = combo.getRenderer();
    combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component c = renderer.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      if (c instanceof JLabel) {
        ((JLabel) c).setIcon(icon);
      }
      return c;
    });
  }
}
