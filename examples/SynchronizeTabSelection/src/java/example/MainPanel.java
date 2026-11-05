// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String RED = "rgb(170, 17, 17)";
  private static final String GREEN = "rgb(17, 119, 0)";

  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs1 = new JTabbedPane();
    tabs1.addTab("Java", createEditor(createJava1(), 36));
    tabs1.addTab("Kotlin", createEditor(createKotlin1(), 36));

    JTabbedPane tabs2 = new JTabbedPane();
    tabs2.addTab("Java", createEditor(createJava2(), 160));
    tabs2.addTab("Kotlin", createEditor(createKotlin2(), 160));
    // Both tabbed panes must have the same number of tabs: otherwise, selecting
    // a tab that exists only in one of them throws an IndexOutOfBoundsException.
    tabs2.setModel(tabs1.getModel());

    Box box = Box.createVerticalBox();
    box.add(tabs1);
    box.add(Box.createVerticalStrut(5));
    box.add(tabs2);
    box.add(Box.createVerticalGlue());
    add(new JScrollPane(box) {
      @Override public void updateUI() {
        super.updateUI();
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
      }
    }, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component createEditor(String html, int height) {
    JEditorPane editor = new JEditorPane();
    editor.setContentType("text/html");
    editor.setEditable(false);
    editor.setText(html);
    return new JScrollPane(editor) {
      @Override public void updateUI() {
        super.updateUI();
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
      }

      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = height;
        return d;
      }
    };
  }

  private static String span(String color, String text) {
    return String.format("<span style='color: %s;'>%s</span>", color, text);
  }

  private static String createJava1() {
    return String.join("",
        "<html><pre><code>",
        "btn.setSelected((i &amp; (" + span(RED, "1"),
        " &lt;&lt; " + span(RED, "2") + "))",
        " != " + span(RED, "0") + ");"
    );
  }

  private static String createKotlin1() {
    return String.join("",
        "<html><pre><code>",
        "btn.setSelected(i and (" + span(RED, "1"),
        " shl " + span(RED, "2") + ")",
        " != " + span(RED, "0") + ")"
    );
  }

  private static String createJava2() {
    return String.join("\n",
        "<html><pre><code>BufferedImage bi = Optional.ofNullable(path)",
        "  .map(url -&gt; {",
        "    " + span(RED, "try") + " {",
        "      " + span(RED, "return") + " ImageIO.read(url);",
        "    } " + span(RED, "catch") + " (IOException ex) {",
        "      " + span(RED, "return") + " createMissingImage();",
        "    }",
        "  }).orElseGet(() -&gt; createMissingImage());"
    );
  }

  private static String createKotlin2() {
    return String.join("\n",
        "<html><pre><code>val bi = runCatching {",
        "  ImageIO." + span(GREEN, "read") + "(" + span(GREEN, "path") + ")",
        "}.getOrNull() ?: createMissingImage()"
    );
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
