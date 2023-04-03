// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Default ButtonGroup", new ButtonGroup()));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Custom ButtonGroup(clears the selection)", new ToggleButtonGroup()));
    box.add(Box.createVerticalGlue());
    add(box);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, ButtonGroup bg) {
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder(title));
    Stream.of("aaa", "bbb", "ccc").map(JToggleButton::new).forEach(r -> {
      p.add(r);
      bg.add(r);
    });
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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

class ToggleButtonGroup extends ButtonGroup {
  private ButtonModel prevModel;

  @Override public void setSelected(ButtonModel m, boolean b) {
    if (m.equals(prevModel)) {
      clearSelection();
    } else {
      super.setSelected(m, b);
    }
    prevModel = getSelection();
  }
}
