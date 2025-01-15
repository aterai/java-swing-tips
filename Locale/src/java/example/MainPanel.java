// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Locale;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final Locale[] LOCALE_ARRAY = {
      Locale.ENGLISH,
      Locale.FRENCH,
      Locale.GERMAN,
      Locale.ITALIAN,
      Locale.JAPANESE,
      Locale.KOREAN,
      Locale.CHINESE,
      Locale.SIMPLIFIED_CHINESE,
      Locale.TRADITIONAL_CHINESE,
      Locale.FRANCE,
      Locale.GERMANY,
      Locale.ITALY,
      Locale.JAPAN,
      Locale.KOREA,
      Locale.CHINA,
      Locale.PRC,
      Locale.TAIWAN,
      Locale.UK,
      Locale.US,
      Locale.CANADA,
      Locale.CANADA_FRENCH,
  };

  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();

    UIManager.put("FileChooser.readOnly", Boolean.TRUE);
    // Locale.setDefault(new Locale("en", "US"));
    // Locale defaultLocale = JFileChooser.getDefaultLocale();
    // JFileChooser.setDefaultLocale(defaultLocale);
    // JFileChooser.setDefaultLocale(Locale.ENGLISH);
    // JFileChooser.setDefaultLocale(new Locale("en", "US"));
    // fileChooser.setLocale(new Locale("fr", "FR"));

    JComboBox<Locale> combo = new JComboBox<>(LOCALE_ARRAY);
    JFileChooser fileChooser = new JFileChooser();

    JButton button = new JButton("<-");
    button.addActionListener(e -> {
      fileChooser.setLocale(combo.getItemAt(combo.getSelectedIndex()));
      SwingUtilities.updateComponentTreeUI(fileChooser);
      int retValue = fileChooser.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.append(String.format("%s%n", fileChooser.getSelectedFile()));
      }
    });

    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBorder(BorderFactory.createTitledBorder("Open JFileChooser"));
    p.add(combo);
    p.add(button, BorderLayout.EAST);

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  // private static void printLocale(JFileChooser fileChooser) {
  //   System.out.println("Locale: " + fileChooser.getLocale());
  //   System.out.println("DefaultLocale: " + fileChooser.getDefaultLocale());
  // }

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
