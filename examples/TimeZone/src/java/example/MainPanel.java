// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();
  private final JTextField field = new JTextField(30);

  @SuppressWarnings("JavaUtilDate")
  private MainPanel() {
    super(new BorderLayout());
    log.setEditable(false);

    SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
    DateFormat df = DateFormat.getDateTimeInstance();
    // df.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
    df.setTimeZone(TimeZone.getDefault());
    field.setText(format.format(new Date()));

    JPanel bp = new JPanel(new GridLayout(1, 0, 2, 2));
    JButton formatButton = new JButton("format");
    formatButton.addActionListener(e -> field.setText(format.format(new Date())));
    bp.add(formatButton);

    JButton parseButton = new JButton("parse");
    parseButton.addActionListener(e -> parseDate(format, df));
    bp.add(parseButton);

    GridBagConstraints c = new GridBagConstraints();
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder("DateFormat"));

    c.insets = new Insets(2, 2, 2, 2);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_END;
    c.weightx = 1d;
    p.add(field, c);

    c.insets = new Insets(2, 0, 2, 2);
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0d;
    c.gridy = 1;
    p.add(bp, c);

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private void parseDate(SimpleDateFormat format, DateFormat df) {
    String str = field.getText().trim();
    Date date = format.parse(str, new ParsePosition(0));
    // String o = Objects.nonNull(date) ? df.format(date) : "error";
    String o = Optional.ofNullable(date).map(df::format).orElse("error");
    log.append(o + "\n");
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
