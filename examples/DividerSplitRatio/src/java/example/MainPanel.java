// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setTopComponent(new JScrollPane(new JTextArea()));
    splitPane.setBottomComponent(new JScrollPane(new JTree()));
    EventQueue.invokeLater(() -> splitPane.setDividerLocation(.5));

    SplitPaneWrapper spw = new SplitPaneWrapper();
    spw.add(splitPane);

    JCheckBox check = new JCheckBox("MAXIMIZED_BOTH: keep the same splitting ratio", true);
    check.addActionListener(e -> spw.setTestFlag(check.isSelected()));

    add(check, BorderLayout.NORTH);
    add(spw);
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

class SplitPaneWrapper extends JPanel {
  private boolean flag = true;
  private int prevState = Frame.NORMAL;

  protected SplitPaneWrapper() {
    super(new BorderLayout());
  }

  public void setTestFlag(boolean f) {
    this.flag = f;
  }

  private static int getOrientedSize(JSplitPane sp) {
    int s = sp.getOrientation() == JSplitPane.VERTICAL_SPLIT ? sp.getHeight() : sp.getWidth();
    return s - sp.getDividerSize();
  }

  @Override public void doLayout() {
    Component sp = getComponent(0);
    if (flag && sp instanceof JSplitPane) {
      JSplitPane splitPane = (JSplitPane) sp;
      int size = getOrientedSize(splitPane);
      float proportionalLoc = splitPane.getDividerLocation() / (float) size;
      super.doLayout();
      int state = Optional.ofNullable(getTopLevelAncestor())
          .filter(Frame.class::isInstance).map(Frame.class::cast)
          .map(Frame::getExtendedState).orElse(Frame.NORMAL);
      if (splitPane.isShowing() && state != prevState) {
        EventQueue.invokeLater(() -> {
          int s = getOrientedSize(splitPane);
          int iv = Math.round(s * proportionalLoc);
          Component c = SwingUtilities.getDeepestComponentAt(splitPane.getTopComponent(), 8, 8);
          if (c instanceof JTextArea) {
            ((JTextArea) c).append(String.format("DividerLocation: %d%n", iv));
          }
          splitPane.setDividerLocation(iv);
        });
        prevState = state;
      }
    } else {
      super.doLayout();
    }
  }
}
