// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    UIManager.put("OptionPane.background", Color.LIGHT_GRAY);
    String txt = "<html>JOptionPane:<br><li>messageArea<li>realBody<li>separator<li>body<li>buttonArea";
    String title = "Title";
    int type = JOptionPane.WARNING_MESSAGE;

    JButton b1 = new JButton("default");
    b1.addActionListener(e -> JOptionPane.showMessageDialog(b1.getRootPane(), new JLabel(txt), title, type));

    JLabel label = new JLabel(txt);
    label.addHierarchyListener(e -> {
      Component c = e.getComponent();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
        stream(SwingUtilities.getAncestorOfClass(JOptionPane.class, c))
            .filter(JPanel.class::isInstance)
            .map(JPanel.class::cast) // TEST: .peek(cc -> System.out.println(cc.getName()))
            .forEach(p -> p.setOpaque(false));
      }
    });

    JButton b2 = new JButton("background");
    b2.addActionListener(e -> JOptionPane.showMessageDialog(b2.getRootPane(), label, title, type));

    JButton b3 = new JButton("override");
    b3.addActionListener(e -> showMessageDialog(b3.getRootPane(), new JLabel(txt), title, type));

    add(b1);
    add(b2);
    add(b3);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void showMessageDialog(Component parent, Object message, String title, int messageType) {
    JOptionPane pane = new JOptionPane(message, messageType, JOptionPane.DEFAULT_OPTION, null, null, null) {
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
    pane.setComponentOrientation((parent == null ? JOptionPane.getRootFrame() : parent).getComponentOrientation());
    stream(pane)
        .filter(JPanel.class::isInstance)
        .map(JPanel.class::cast)
        .forEach(p -> p.setOpaque(false));

    JDialog dialog = pane.createDialog(parent, title);
    dialog.setVisible(true);
    dialog.dispose();
  }

  private static Stream<Component> stream(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance)
        .map(c -> stream((Container) c))
        .reduce(Stream.of(parent), Stream::concat);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
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
