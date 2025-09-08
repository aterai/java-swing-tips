// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(makeMenuBar()));
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar makeMenuBar() {
    JMenu file = new JMenu("File");
    file.add("New");
    file.add("Open");
    file.addSeparator();
    file.add(makeRecentMenu());
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(file);
    menuBar.add(new JMenu("Edit"));
    return menuBar;
  }

  private static JMenu makeRecentMenu() {
    JMenu menu = new JMenu("Recent Files");
    JTextField field = new JTextField(20);
    menu.add(field);
    field.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) {
        filter(menu, field);
      }

      @Override public void removeUpdate(DocumentEvent e) {
        filter(menu, field);
      }

      @Override public void changedUpdate(DocumentEvent e) {
        /* not needed */
      }
    });
    menu.add("aa001.txt");
    menu.add("aa002.log");
    menu.add("aabb33.txt");
    menu.add("abc4.md");
    menu.add("b5.markdown");
    menu.add("ccc6.txt");
    return menu;
  }

  private static Pattern getPattern(JTextField field) {
    String regex = field.getText();
    Pattern pattern = null;
    if (Objects.nonNull(regex) && !regex.isEmpty()) {
      try {
        pattern = Pattern.compile(regex);
      } catch (PatternSyntaxException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback(field);
      }
    }
    return pattern;
  }

  private static void filter(JMenu menu, JTextField field) {
    Pattern ptn = getPattern(field);
    Stream.of(menu.getPopupMenu().getSubElements())
        .filter(JMenuItem.class::isInstance)
        .map(JMenuItem.class::cast)
        .forEach(mi ->
            mi.setVisible(ptn == null || ptn.matcher(mi.getText()).find()));
    menu.getPopupMenu().pack();
    EventQueue.invokeLater(field::requestFocusInWindow);
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
