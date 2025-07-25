// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    UIManager.put("OptionPane.background", Color.LIGHT_GRAY);
    String li = "<li>messageArea<li>realBody<li>separator<li>body<li>buttonArea";
    String txt = "<html>JOptionPane:<br>" + li;
    String title = "Title";

    JButton b1 = new JButton("default");
    b1.addActionListener(e -> showDefaultMessageDialog(new JLabel(txt), title));
    add(b1);

    JButton b2 = new JButton("background");
    b2.addActionListener(e -> showDefaultMessageDialog(makeMessage(txt), title));
    add(b2);

    JButton b3 = new JButton("override");
    b3.addActionListener(e -> showMessageDialog(new JLabel(txt), title));
    add(b3);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JLabel makeMessage(String txt) {
    JLabel l2 = new JLabel(txt);
    l2.addHierarchyListener(e -> {
      Component c = e.getComponent();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
        descendants(SwingUtilities.getAncestorOfClass(JOptionPane.class, c))
            .filter(JPanel.class::isInstance)
            .map(JPanel.class::cast) // TEST: .peek(cc -> System.out.println(cc.getName()))
            .forEach(p -> p.setOpaque(false));
      }
    });
    return l2;
  }

  private void showDefaultMessageDialog(Object msg, String title) {
    JRootPane p = getRootPane();
    JOptionPane.showMessageDialog(p, msg, title, JOptionPane.WARNING_MESSAGE);
  }

  private void showMessageDialog(Object msg, String title) {
    JOptionPane op = new JOptionPane(
        msg, JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION,
        null, null, null) {
      private transient Paint texture;

      @Override public void updateUI() {
        super.updateUI();
        texture = TextureUtils.createCheckerTexture(16, new Color(0x64_AA_AA_AA, true));
      }

      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(texture);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
      }
    };
    Component parent = getRootPane();
    Component c = parent == null ? JOptionPane.getRootFrame() : parent;
    op.setComponentOrientation(c.getComponentOrientation());
    descendants(op)
        .filter(JPanel.class::isInstance)
        .map(JPanel.class::cast)
        .forEach(p -> p.setOpaque(false));

    JDialog dialog = op.createDialog(parent, title);
    dialog.setVisible(true);
    dialog.dispose();
  }

  public static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
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

final class TextureUtils {
  private TextureUtils() {
    /* HideUtilityClassConstructor */
  }

  public static TexturePaint createCheckerTexture(int cs, Color color) {
    int size = cs * cs;
    BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setPaint(color);
    g2.fillRect(0, 0, size, size);
    for (int i = 0; i * cs < size; i++) {
      for (int j = 0; j * cs < size; j++) {
        if ((i + j) % 2 == 0) {
          g2.fillRect(i * cs, j * cs, cs, cs);
        }
      }
    }
    g2.dispose();
    return new TexturePaint(img, new Rectangle(size, size));
  }
}
