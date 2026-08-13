// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Optional;
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
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    JMenu file = new JMenu("File");
    file.add("New");
    file.add("Open");
    file.addSeparator();
    file.add(createRecentFilesMenu());
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(file);
    menuBar.add(new JMenu("Edit"));
    return menuBar;
  }

  private static JMenu createRecentFilesMenu() {
    JMenu menu = new JMenu("Recent Files");
    // Add the filter field as the first child of the popup menu
    JTextField filterField = new JTextField(20);
    menu.add(filterField);
    filterField.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) {
        filterMenuItems(menu, filterField);
      }

      @Override public void removeUpdate(DocumentEvent e) {
        filterMenuItems(menu, filterField);
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

  // Returns empty if the input is blank or is not a valid regular expression
  private static Pattern compilePattern(JTextField field) {
    String regex = field.getText();
    Pattern pattern = null;
    if (regex != null && !regex.isEmpty()) {
      try {
        pattern = Pattern.compile(regex);
      } catch (PatternSyntaxException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback(field);
      }
    }
    return pattern;
  }

  // All items stay visible while no valid pattern is available
  private static boolean isVisible(Pattern pattern, JMenuItem item) {
    return Optional.ofNullable(pattern)
        .map(p -> p.matcher(item.getText()).find())
        .orElse(Boolean.TRUE);
  }

  private static void filterMenuItems(JMenu menu, JTextField field) {
    Pattern pattern = compilePattern(field);
    JPopupMenu popup = menu.getPopupMenu();
    // JPopupMenu#getSubElements() returns only MenuElements: the filter field is skipped
    Stream.of(popup.getSubElements())
        .filter(JMenuItem.class::isInstance)
        .map(JMenuItem.class::cast)
        .forEach(item -> item.setVisible(isVisible(pattern, item)));
    popup.pack(); // Resize the popup because the number of visible items has changed
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
