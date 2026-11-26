// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    SpinnerNumberModel model1 = new SpinnerNumberModel(100, 10, 300, 10);
    JScrollPane scroll1 = createScrollPane(new JTree(), model1);
    SpinnerNumberModel model2 = new SpinnerNumberModel(150, 10, 300, 10);
    JTextArea textArea = new JTextArea();
    JScrollPane scroll2 = createScrollPane(textArea, model2);
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll1, scroll2);
    split.setOneTouchExpandable(true);
    split.setContinuousLayout(true);

    JMenu menu = new JMenu("JSplitPane");
    menu.add(createSpinner("L: ", model1));
    menu.add(createSpinner("R: ", model2));
    menu.addSeparator();
    menu.add("resetToPreferredSizes").addActionListener(e -> {
      split.resetToPreferredSizes();
      appendDividerInfo(split, textArea);
    });
    menu.addSeparator();
    menu.add("setDividerLocation(.5)").addActionListener(e -> split.setDividerLocation(.5));
    menu.add("selectMin").addActionListener(e -> selectMinMax(split, "selectMin"));
    menu.add("selectMax").addActionListener(e -> selectMinMax(split, "selectMax"));

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(menu);
    menuBar.add(createResizeWeightMenu(split));
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private static JMenu createResizeWeightMenu(JSplitPane split) {
    JMenu menu = new JMenu("ResizeWeight");
    ButtonGroup group = new ButtonGroup();
    for (double weight : new double[] {0d, .5, 1d}) {
      // Select the item that matches the current resize weight (default: 0.0)
      boolean selected = Double.compare(weight, split.getResizeWeight()) == 0;
      JMenuItem item = new JRadioButtonMenuItem(String.valueOf(weight), selected);
      item.addActionListener(e -> split.setResizeWeight(weight));
      group.add(item);
      menu.add(item);
    }
    return menu;
  }

  private static void appendDividerInfo(JSplitPane split, JTextArea textArea) {
    // resetToPreferredSizes() only revalidates the JSplitPane, so wait for the layout
    EventQueue.invokeLater(() -> {
      int w1 = split.getLeftComponent().getPreferredSize().width;
      int w2 = split.getRightComponent().getPreferredSize().width;
      double rw = split.getResizeWeight();
      int loc = split.getDividerLocation();
      textArea.append(String.format("%d:%d,loc:%d,weight:%.1f%n", w1, w2, loc, rw));
    });
  }

  private static void selectMinMax(JSplitPane splitPane, String cmd) {
    // The "selectMin"/"selectMax" actions of BasicSplitPaneUI assume that
    // the JSplitPane has the focus, so request it first and invoke the action
    // after the focus change has been processed
    splitPane.requestFocusInWindow();
    new SwingWorker<Void, Void>() {
      @Override protected Void doInBackground() {
        return null;
      }

      @Override protected void done() {
        Action a = splitPane.getActionMap().get(cmd);
        if (a != null) {
          a.actionPerformed(new ActionEvent(splitPane, ActionEvent.ACTION_PERFORMED, cmd));
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
      }
    }.execute();
  }

  private static JScrollPane createScrollPane(Component c, SpinnerNumberModel m) {
    return new JScrollPane(c) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = m.getNumber().intValue();
        return d;
      }
    };
  }

  private static Component createSpinner(String title, SpinnerModel model) {
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box.add(new JLabel(title));
    box.add(new JSpinner(model));
    return box;
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
