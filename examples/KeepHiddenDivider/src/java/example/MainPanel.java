// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setTopComponent(makeScrollPane(new JTable(5, 3)));
    splitPane.setBottomComponent(makeScrollPane(new JTree()));
    splitPane.setOneTouchExpandable(true);

    JPanel north = new JPanel(new GridLayout(0, 2, 5, 5));
    north.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    north.add(new JButton(new AbstractAction("Min:DividerLocation") {
      @Override public void actionPerformed(ActionEvent e) {
        splitPane.setDividerLocation(0);
      }
    }));
    north.add(new JButton(new AbstractAction("Max:DividerLocation") {
      @Override public void actionPerformed(ActionEvent e) {
        Optional<Insets> ins = Optional.ofNullable(splitPane.getInsets());
        int loc;
        if (splitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
          loc = splitPane.getHeight() - ins.map(i -> i.bottom).orElse(0);
        } else {
          loc = splitPane.getWidth() - ins.map(i -> i.right).orElse(0);
        }
        splitPane.setDividerLocation(loc);
        // test(splitPane);
      }
    }));

    Action minAction = new AbstractAction("selectMin") {
      @Override public void actionPerformed(ActionEvent e) {
        splitPane.requestFocusInWindow();
        EventQueue.invokeLater(() -> {
          String cmd = e.getActionCommand();
          ActionMap am = splitPane.getActionMap();
          am.get(cmd).actionPerformed(new ActionEvent(splitPane, e.getID(), cmd));
        });
      }
    };
    north.add(new JButton(minAction));

    Action maxAction = new AbstractAction("selectMax") {
      @Override public void actionPerformed(ActionEvent e) {
        splitPane.requestFocusInWindow();
        EventQueue.invokeLater(() -> {
          String cmd = e.getActionCommand();
          ActionMap am = splitPane.getActionMap();
          am.get(cmd).actionPerformed(new ActionEvent(splitPane, e.getID(), cmd));
        });
      }
    };
    north.add(new JButton(maxAction));

    JButton minButton = new JButton("Min:keepHidden");
    JButton maxButton = new JButton("Max:keepHidden");
    Container divider = ((BasicSplitPaneUI) splitPane.getUI()).getDivider();
    initDividerButtonModel(divider, minButton, maxButton);
    north.add(minButton);
    north.add(maxButton);

    add(north, BorderLayout.NORTH);
    add(splitPane);
    setPreferredSize(new Dimension(320, 240));
  }

  // private static void test(JSplitPane splitPane) {
  //   int lastLoc = splitPane.getLastDividerLocation();
  //   int currentLoc = splitPane.getDividerLocation();
  //   Container divider = ((BasicSplitPaneUI) splitPane.getUI()).getDivider();
  //   int newLoc;
  //   if (currentLoc == splitPane.getInsets().top) {
  //     int maxLoc = splitPane.getMaximumDividerLocation();
  //     newLoc = Math.min(lastLoc, maxLoc); // splitPaneUI.setKeepHidden(false);
  //   } else {
  //     newLoc = splitPane.getHeight() - divider.getHeight() - splitPane.getInsets().top;
  //     // BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI) splitPane.getUI();
  //     // splitPaneUI.setKeepHidden(true);
  //   }
  //   if (currentLoc != newLoc) {
  //     splitPane.setDividerLocation(newLoc);
  //     splitPane.setLastDividerLocation(currentLoc);
  //   }
  // }

  private static JScrollPane makeScrollPane(Component c) {
    return new JScrollPane(c) {
      @Override public Dimension getMinimumSize() {
        return new Dimension(0, 100);
      }
    };
  }

  private static void initDividerButtonModel(Container divider, JButton minBtn, JButton maxBtn) {
    ButtonModel selectMinModel = null;
    ButtonModel selectMaxModel = null;
    for (Component c : divider.getComponents()) {
      if (c instanceof JButton) {
        ButtonModel m = ((JButton) c).getModel();
        if (Objects.isNull(selectMinModel)) {
          selectMinModel = m;
        } else if (Objects.isNull(selectMaxModel)) {
          selectMaxModel = m;
        }
      }
    }
    minBtn.setModel(selectMinModel);
    maxBtn.setModel(selectMaxModel);
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
