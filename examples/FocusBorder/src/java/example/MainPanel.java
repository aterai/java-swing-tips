// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextField field1 = new JTextField(20);
    field1.setText("1111111111111111");

    JTextField field2 = new JTextField(20) {
      private transient FocusListener handler;

      @Override public void updateUI() {
        // setCaret(null);
        removeFocusListener(handler);
        super.updateUI();
        setOpaque(false);
        setBorder(new RoundedCornerBorder());
        // setCaret(new DefaultCaret() {
        //   @Override public void focusGained(FocusEvent e) {
        //     super.focusGained(e);
        //     getComponent().repaint();
        //   }
        //
        //   @Override public void focusLost(FocusEvent e) {
        //     super.focusLost(e);
        //     getComponent().repaint();
        //   }
        // });
        handler = new FocusBorderListener();
        addFocusListener(handler);
      }

      @Override protected void paintComponent(Graphics g) {
        Border b = getBorder();
        if (!isOpaque() && b instanceof RoundedCornerBorder) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setPaint(getBackground());
          int w = getWidth() - 1;
          int h = getHeight() - 1;
          g2.fill(((RoundedCornerBorder) b).getBorderShape(0, 0, w, h));
          g2.dispose();
        }
        super.paintComponent(g);
      }
    };
    field2.setText("2222222222222");

    JCheckBox check = new JCheckBox("setEnabled", true);
    check.addActionListener(e -> {
      boolean b = check.isSelected();
      field1.setEnabled(b);
      field2.setEnabled(b);
    });
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(check);

    JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
    p.add(makeTitledPanel("Default:", field1));
    p.add(makeTitledPanel("setBorder(new RoundedCornerBorder())", field2));
    p.add(box);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(p, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.setOpaque(false);
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
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

class RoundedCornerBorder extends AbstractBorder {
  private static final Paint ALPHA_ZERO = new Color(0x0, true);
  private static final int ARC = 4;

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Shape border = getBorderShape(x, y, width - 1, height - 1);

    g2.setPaint(ALPHA_ZERO);
    Area corner = new Area(new Rectangle2D.Double(x, y, width, height));
    corner.subtract(new Area(border));
    g2.fill(corner);

    Color borderColor;
    if (c.hasFocus()) {
      borderColor = new Color(0x4F_C1_E9);
    } else if (c.isEnabled()) {
      borderColor = Color.LIGHT_GRAY;
    } else {
      borderColor = Color.WHITE;
    }
    g2.setPaint(borderColor);
    g2.draw(border);
    g2.dispose();
  }

  public Shape getBorderShape(int x, int y, int w, int h) {
    return new RoundRectangle2D.Double(x, y, w, h, ARC, ARC);
  }

  @Override public Insets getBorderInsets(Component c) {
    return new Insets(ARC, ARC, ARC, ARC);
  }

  @Override public Insets getBorderInsets(Component c, Insets insets) {
    insets.set(ARC, ARC, ARC, ARC);
    return insets;
  }
}

class FocusBorderListener implements FocusListener {
  @Override public void focusGained(FocusEvent e) {
    update(e.getComponent());
  }

  @Override public void focusLost(FocusEvent e) {
    update(e.getComponent());
  }

  private void update(Component c) {
    c.repaint();
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
