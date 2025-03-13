// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    SpinnerNumberModel model1 = new SpinnerNumberModel(100, 10, 300, 10);
    JScrollPane scroll1 = makeScrollPane(new JTree(), model1);
    SpinnerNumberModel model2 = new SpinnerNumberModel(150, 10, 300, 10);
    JTextArea textArea = new JTextArea();
    JScrollPane scroll2 = makeScrollPane(textArea, model2);
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll1, scroll2);
    split.setOneTouchExpandable(true);
    split.setContinuousLayout(true);
    JMenu menu1 = new JMenu("JSplitPane");
    menu1.add(makeSpinner("L: ", model1));
    menu1.add(makeSpinner("R: ", model2));
    menu1.addSeparator();
    menu1.add("resetToPreferredSizes").addActionListener(e -> {
      split.resetToPreferredSizes();
      info(split, textArea);
    });
    menu1.addSeparator();
    menu1.add("setDividerLocation(.5)").addActionListener(e -> split.setDividerLocation(.5));
    menu1.add("selectMin").addActionListener(e -> selectMinMax(split, "selectMin"));
    menu1.add("selectMax").addActionListener(e -> selectMinMax(split, "selectMax"));

    JMenu menu2 = new JMenu("ResizeWeight");
    JRadioButtonMenuItem r0 = new JRadioButtonMenuItem("0.0", true);
    menu2.add(r0).addActionListener(e -> split.setResizeWeight(0d));
    JRadioButtonMenuItem r1 = new JRadioButtonMenuItem("0.5");
    menu2.add(r1).addActionListener(e -> split.setResizeWeight(.5));
    JRadioButtonMenuItem r2 = new JRadioButtonMenuItem("1.0");
    menu2.add(r2).addActionListener(e -> split.setResizeWeight(1d));
    ButtonGroup group = new ButtonGroup();
    for (AbstractButton r : Arrays.asList(r0, r1, r2)) {
      group.add(r);
    }
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(menu1);
    menuBar.add(menu2);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void info(JSplitPane split, JTextArea textArea) {
    EventQueue.invokeLater(() -> {
      int w1 = split.getLeftComponent().getPreferredSize().width;
      int w2 = split.getRightComponent().getPreferredSize().width;
      double rw = split.getResizeWeight();
      int loc = split.getDividerLocation();
      textArea.append(String.format("%d:%d,loc:%d,weight:%.1f%n", w1, w2, loc, rw));
    });
  }

  private void selectMinMax(JSplitPane splitPane, String cmd) {
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

  private static JScrollPane makeScrollPane(Component c, SpinnerNumberModel m) {
    return new JScrollPane(c) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = m.getNumber().intValue();
        return d;
      }
    };
  }

  private static Component makeSpinner(String title, SpinnerModel model) {
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
