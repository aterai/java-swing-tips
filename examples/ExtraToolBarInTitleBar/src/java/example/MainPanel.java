// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new JScrollPane(new JTree()));
    setPreferredSize(new Dimension(320, 240));
  }

  // How to add icon to JFrame's title bar - Oracle Forums
  // https://forums.oracle.com/ords/apexds/post/how-to-add-icon-to-jframe-s-title-bar-5581
  private static JButton makeButton(String title, Icon icon) {
    JButton extraButton = new JButton(title, icon) {
      @Override public Dimension getPreferredSize() {
        Icon icon = UIManager.getIcon("InternalFrame.closeIcon");
        return new Dimension(icon.getIconWidth(), icon.getIconHeight());
      }
    };
    extraButton.setFocusPainted(false);
    // extraButton.setBorder(BorderFactory.createEmptyBorder());
    extraButton.setOpaque(false);
    extraButton.setBorderPainted(false);
    extraButton.setToolTipText("Extra JButton: " + title);
    extraButton.addActionListener(e -> {
      String msg = "Extra JButton was clicked!";
      JOptionPane.showMessageDialog(extraButton.getRootPane(), msg);
    });
    return extraButton;
  }

  private static JWindow makeExtraBarWindow(JFrame frame) {
    // JToolBar bar = new JToolBar();
    // bar.setFloatable(false);
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 4));
    box.setOpaque(false);
    box.add(makeButton("...", null));
    box.add(Box.createHorizontalStrut(5));
    box.add(makeButton(null, new ExtraIcon()));
    JWindow window = new JWindow(frame);
    // window.setAlwaysOnTop(true); // XXX
    // window.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
    window.setBackground(UIManager.getColor("activeCaption"));
    window.getContentPane().setBackground(new Color(0x0, true));
    window.getContentPane().add(box);
    window.pack();
    return window;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame.setDefaultLookAndFeelDecorated(true);
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.setMinimumSize(new Dimension(240, 120));
    frame.pack();
    frame.setLocationRelativeTo(null);
    JWindow bar = makeExtraBarWindow(frame);
    ExtraBarPositionHandler handler = new ExtraBarPositionHandler(bar);
    frame.addComponentListener(handler);
    frame.addWindowListener(handler);
    frame.setVisible(true);
  }
}

@SuppressWarnings("PMD.TooManyMethods")
class ExtraBarPositionHandler extends WindowAdapter implements ComponentListener {
  private final JWindow extraBar;

  protected ExtraBarPositionHandler(JWindow extraBar) {
    super();
    this.extraBar = extraBar;
  }

  @Override public void windowActivated(WindowEvent e) {
    extraBar.setBackground(UIManager.getColor("activeCaption"));
  }

  @Override public void windowDeactivated(WindowEvent e) {
    extraBar.setBackground(UIManager.getColor("inactiveCaption"));
  }

  @Override public void windowOpened(WindowEvent e) {
    setLocationRelativeTo(e.getWindow());
    extraBar.setVisible(true);
  }

  @Override public void windowClosed(WindowEvent e) {
    extraBar.setVisible(false);
  }

  @Override public void windowIconified(WindowEvent e) {
    extraBar.setVisible(false);
  }

  @Override public void windowDeiconified(WindowEvent e) {
    setLocationRelativeTo(e.getWindow());
    extraBar.setVisible(true);
  }

  @Override public void componentResized(ComponentEvent e) {
    // System.out.println("componentResized");
    // ???: setLocationRelativeTo(e.getComponent());
    EventQueue.invokeLater(() -> setLocationRelativeTo(e.getComponent()));
  }

  @Override public void componentMoved(ComponentEvent e) {
    setLocationRelativeTo(e.getComponent());
  }

  @Override public void componentShown(ComponentEvent e) {
    setLocationRelativeTo(e.getComponent());
    extraBar.setVisible(true);
  }

  @Override public void componentHidden(ComponentEvent e) {
    extraBar.setVisible(false);
  }

  private void setLocationRelativeTo(Component p) {
    EventQueue.invokeLater(() -> updateExtraBarLocation(p));
  }

  private void updateExtraBarLocation(Component p) {
    JRootPane root = SwingUtilities.getRootPane(p);
    Icon iconifyIcon = UIManager.getIcon("InternalFrame.iconifyIcon");
    Insets zeroIns = new Insets(0, 0, 0, 0);
    Border bdr = root.getBorder();
    Insets ins = bdr == null ? zeroIns : bdr.getBorderInsets(root);
    if (p instanceof Frame && ((Frame) p).getExtendedState() == Frame.MAXIMIZED_BOTH) {
      ins = zeroIns;
    }
    JButton iconifyIconButton = SwingUtils
        .descendants(root)
        .filter(JButton.class::isInstance)
        .map(JButton.class::cast)
        .filter(b -> Objects.equals(b.getIcon(), iconifyIcon))
        .findFirst()
        .orElse(null);
    Point pt = iconifyIconButton == null ? new Point() : iconifyIconButton.getLocation();
    SwingUtilities.convertPointToScreen(pt, root);
    int x = pt.x - extraBar.getWidth();
    int y = p.getY() + ins.top + 1;
    extraBar.setLocation(x, y);
  }
}

class ExtraIcon implements Icon {
  private final int size;

  protected ExtraIcon() {
    Icon icon = UIManager.getIcon("InternalFrame.closeIcon");
    this.size = icon == null ? 16 : icon.getIconHeight();
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Color color = ((JButton) c).getModel().isPressed() ? Color.RED : Color.GREEN;
    g2.setColor(color);
    g2.fillOval(x + 2, y + 2, size - 4, size - 4);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return size;
  }

  @Override public int getIconHeight() {
    return size;
  }
}

final class SwingUtils {
  private SwingUtils() {
    /* Singleton */
  }

  public static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
  }
}
