// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    // not supported:
    // table {
    //   border-collapse: collapse;
    // }
    // th, td {
    //   border:1px solid #ccc;
    // }
    String tds1 = "border-right:1px solid green;border-top:1px solid blue";
    String tbs1 = "border-left:1px solid red;border-bottom:1px solid red;background:yellow";
    String padding1 = "cellspacing='0px' cellpadding='5px'";
    String html1 = makeHtml(tbs1, padding1, tds1);
    add(makeTitledPanel("border-left, border-bottom", new JLabel(html1)));

    String tds2 = "border-right:1px solid red;border-bottom:1px solid blue";
    String tbs2 = "border-left:1px solid red;border-top:1px solid red;background:yellow";
    String padding2 = "cellspacing='0px' cellpadding='5px'";
    String html2 = makeHtml(tbs2, padding2, tds2);
    add(makeTitledPanel("border-left, border-top", new JLabel(html2)));

    // https://stackoverflow.com/questions/3355469/1-pixel-table-border-in-jtextpane-using-html
    String style3 = "border:0px;background:red";
    String padding3 = "cellspacing='1px' cellpadding='5px'";
    String html3 = makeHtml(style3, padding3, "");
    add(makeTitledPanel("cellspacing", new JLabel(html3)));

    setPreferredSize(new Dimension(320, 240));
  }

  private static String makeHtml(String tbs, String padding, String tds) {
    String txt = "123456789012345678901234567890";
    String tr = String.format("<tr><td style='background:white;%s'>%s</td></tr>", tds, txt);
    return String.format("<html><table style='%s' %s>%s%s</table>", tbs, padding, tr, tr);
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
