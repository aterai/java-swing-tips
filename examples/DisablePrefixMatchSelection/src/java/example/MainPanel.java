// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.Position;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2, 5, 5));
    DefaultListModel<String> model = new DefaultListModel<>();
    model.addElement("aaa aaa aaa aaa");
    model.addElement("abb bbb bbb bbb bbb bb bb");
    model.addElement("abc ccc ccc ccc");
    model.addElement("bbb bbb");
    model.addElement("ccc bbb");
    model.addElement("ddd ddd ddd ddd");
    model.addElement("eee eee eee eee eee");
    model.addElement("fff fff fff fff fff fff");

    JList<String> list = new JList<String>(model) {
      @Override public int getNextMatch(String prefix, int startIndex, Position.Bias bias) {
        return -1;
      }
    };

    add(makeTitledPanel("Default", new JScrollPane(new JList<>(model))));
    add(makeTitledPanel("Disable prefixMatchSelection", new JScrollPane(list)));
    // add(check, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
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
