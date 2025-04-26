// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JScrollPane s1 = new JScrollPane(new JTree());
    JScrollPane s2 = new JScrollPane(new JTable(2, 3));
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, s1, s2);
    split.setOneTouchExpandable(true);
    split.setContinuousLayout(true);
    split.setBorder(BorderFactory.createMatteBorder(8, 8, 8, 8, Color.WHITE));
    EventQueue.invokeLater(() -> split.setDividerLocation(.3));

    JCheckBox check = new JCheckBox("Show JPopupMenu only on Divider", true);
    JPopupMenu popup = new JPopupMenu() {
      @Override public void show(Component invoker, int x, int y) {
        if (check.isSelected() && invoker instanceof JSplitPane) {
          BasicSplitPaneUI ui = (BasicSplitPaneUI) ((JSplitPane) invoker).getUI();
          if (ui.getDivider().getBounds().contains(x, y)) {
            super.show(invoker, x, y);
          }
        } else {
          super.show(invoker, x, y);
        }
      }
    };
    popup.add("center").addActionListener(e -> split.setDividerLocation(.5));
    popup.add("selectMin").addActionListener(e -> selectMinMax(split, "selectMin"));
    popup.add("selectMax").addActionListener(e -> selectMinMax(split, "selectMax"));
    split.setComponentPopupMenu(popup);

    BasicSplitPaneUI ui = (BasicSplitPaneUI) split.getUI();
    Container divider = ui.getDivider();
    divider.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
          split.setDividerLocation(.5);
        }
      }

      // @Override public void mouseReleased(MouseEvent e) {
      //   if (e.isPopupTrigger()) {
      //     popup.show(divider, e.getX(), e.getY());
      //   }
      // }
    });

    add(check, BorderLayout.NORTH);
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  private void selectMinMax(JSplitPane splitPane, String cmd) {
    // splitPane.requestFocus();
    splitPane.requestFocusInWindow();
    new SwingWorker<Void, Void>() {
      @Override protected Void doInBackground() {
        return null;
      }

      @Override protected void done() {
        super.done();
        Action a = splitPane.getActionMap().get(cmd);
        a.actionPerformed(new ActionEvent(splitPane, ActionEvent.ACTION_PERFORMED, cmd));
        KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
      }
    }.execute();
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
