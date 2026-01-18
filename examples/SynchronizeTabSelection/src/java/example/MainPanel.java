// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs1 = new JTabbedPane();
    tabs1.addTab("Java", makeEditor(makeJava1(), 36));
    tabs1.addTab("Kotlin", makeEditor(makeKotlin1(), 36));

    JTabbedPane tabs2 = new JTabbedPane();
    tabs2.addTab("Java", makeEditor(makeJava2(), 160));
    tabs2.addTab("Kotlin", makeEditor(makeKotlin2(), 160));
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

  private static Component makeEditor(String s, int height) {
    JEditorPane editor = new JEditorPane();
    editor.setContentType("text/html");
    editor.setEditable(false);
    editor.setText(s);
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

  private static String makeJava1() {
    return String.join("",
        "<html><pre><code>",
        "btn.setSelected((i &amp; (<span style='color: rgb(170, 17, 17);'>1</span>",
        " &lt;&lt; <span style='color: rgb(170, 17, 17);'>2</span>))",
        " != <span style='color: rgb(170, 17, 17);'>0</span>);"
    );
  }

  //      "<html><pre><code>"
  private static String makeKotlin1() {
    return String.join("",
        "<html><pre><code>",
        "btn.setSelected(i and (<span style='color: rgb(170, 17, 17);'>1</span>",
        " shl <span style='color: rgb(170, 17, 17);'>2</span>)",
        " != <span style='color: rgb(170, 17, 17);'>0</span>)"
    );
  }

  private static String makeJava2() {
    return String.join("\n",
        "<html><pre><code>BufferedImage bi = Optional.ofNullable(path)",
        "  .map(url -&gt; {",
        "    <span style='color: rgb(170, 17, 17);'>try</span> {",
        "      <span style='color: rgb(170, 17, 17);'>return</span> ImageIO.read(url);",
        "    } <span style='color: rgb(170, 17, 17);'>catch</span> (IOException ex) {",
        "      <span style='color: rgb(170, 17, 17);'>return</span> makeMissingImage();",
        "    }",
        "  }).orElseGet(() -&gt; makeMissingImage());"
    );
  }

  private static String makeKotlin2() {
    String span = "<span style='color: rgb(17, 119, 0);'>%s</span>";
    return String.join("\n",
        "<pre><code>val bi = runCatching {",
        "  ImageIO." + String.format(span, "read") + "(" + String.format(span, "path") + ")",
        "}.getOrNull() ?: makeMissingImage()"
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
