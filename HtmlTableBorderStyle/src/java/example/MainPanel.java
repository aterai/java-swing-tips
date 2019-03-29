// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public static final String TXT = "aaaaaaaaaa";

  public static final String TD1 =
      "<td style='background-color:white;border-right:1px solid green;border-top:1px solid blue'>%s</td>";
  public static final String TABLE_STYLE1 =
      "style='border-left:1px solid red;border-bottom:1px solid red;background-color:yellow'";
  public static final String TABLE_CELLPD1 = " cellspacing='0px' cellpadding='5px'";

  public static final String TD2 =
      "<td style='background-color:white;border-right:1px solid green;border-bottom:1px solid blue'>%s</td>";
  public static final String TABLE_STYLE2 =
      "style='border-left:1px solid red;border-top:1px solid red;background-color:yellow'";
  public static final String TABLE_CELLPD2 = " cellspacing='0px' cellpadding='5px'";

  // https://stackoverflow.com/questions/3355469/1-pixel-table-border-in-jtextpane-using-html
  public static final String TD3 = "<td style='background-color:white'>%s</td>";
  public static final String TABLE_STYLE3 = "style='border:0px;background-color:red'";
  public static final String TABLE_CELLPD3 = " cellspacing='1px' cellpadding='5px'";

  private MainPanel() {
    super();

    // not supported:
    // table {
    //   border-collapse: collapse;
    // }
    // th, td {
    //   border:1px solid #ccc;
    // }

    String td01 = String.format(TD1, TXT);
    String td02 = String.format(TD1, TXT);
    String td03 = String.format(TD1, TXT);

    String html1 = "<html><table " + TABLE_STYLE1 + TABLE_CELLPD1 + ">" + "<tr>" + td01 + "</tr><tr>" + td01 + "</tr></table>";
    String html2 = "<html><table " + TABLE_STYLE2 + TABLE_CELLPD2 + ">" + "<tr>" + td02 + "</tr><tr>" + td02 + "</tr></table>";
    String html3 = "<html><table " + TABLE_STYLE3 + TABLE_CELLPD3 + ">" + "<tr>" + td03 + "</tr><tr>" + td03 + "</tr></table>";

    add(makeTitledPanel("border-left, border-bottom", new JLabel(html1)));
    add(makeTitledPanel("border-left, border-top", new JLabel(html2)));
    add(makeTitledPanel("cellspacing", new JLabel(html3)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
