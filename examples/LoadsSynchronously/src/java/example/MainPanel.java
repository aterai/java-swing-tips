// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;

public final class MainPanel extends JPanel {
  private static final String TXT = "1111111111111111111111111";
  private static final String HTML = "<span style='background-color:red'>" + TXT + "</span><br/>";
  private final JLabel label = new JLabel("screenshot");

  private MainPanel() {
    super(new BorderLayout());
    label.setVerticalAlignment(SwingConstants.CENTER);
    label.setVerticalTextPosition(SwingConstants.BOTTOM);
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setHorizontalTextPosition(SwingConstants.CENTER);

    // String path = "https://raw.githubusercontent.com/aterai/java-swing-tips/master/LoadsSynchronously/src/java/example/GIANT_TCR1_2013.jpg";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    String path = Objects.toString(cl.getResource("example/GIANT_TCR1_2013.jpg"));
    String html1 = String.join("\n", Collections.nCopies(50, HTML));
    String html2 = String.join("\n", Collections.nCopies(3, HTML));

    JTabbedPane tabs = new JTabbedPane();

    String st0 = makeHtml0(path, html1, html2);
    JEditorPane editor0 = new JEditorPane();
    editor0.setEditorKit(new HTMLEditorKit());
    editor0.setText(st0);
    tabs.addTab("default", new JScrollPane(editor0));

    String st1 = makeHtml1(path, html1, html2);
    JEditorPane editor1 = new JEditorPane();
    editor1.setEditorKit(new HTMLEditorKit());
    editor1.setText(st1);
    tabs.addTab("<img width='%d' ...", new JScrollPane(editor1));

    // ImageView incorrectly calculates size when synchronously loaded - Java Bug System
    // https://bugs.openjdk.org/browse/JDK-8223384
    JEditorPane editor2 = new JEditorPane();
    editor2.setEditorKit(new ImageLoadSynchronouslyHtmlEditorKit());
    tabs.addTab("LoadsSynchronously", new JScrollPane(editor2));

    tabs.addChangeListener(e -> {
      int i = tabs.getSelectedIndex();
      JEditorPane editor = i == 0 ? editor0 : i == 1 ? editor1 : editor2;
      editor.setText(i == 1 ? st1 : st0);
      EventQueue.invokeLater(() -> saveImage(editor));
    });

    add(tabs);
    add(label, BorderLayout.EAST);
    setPreferredSize(new Dimension(320, 240));
  }

  private static String makeHtml0(String path, String html1, String html2) {
    String img = String.format("<p><img src='%s'></p>", path);
    return html1 + img + html2;
  }

  private static String makeHtml1(String path, String html1, String html2) {
    int w = 2048;
    int h = 1360;
    // Image image = Toolkit.getDefaultToolkit().createImage(path);
    // MediaTracker tracker = new MediaTracker((Container) this);
    // tracker.addImage(image, 0);
    // try {
    //   tracker.waitForID(0);
    // } catch (InterruptedException ex) {
    //   ex.printStackTrace();
    // } finally {
    //   if (!tracker.isErrorID(0)) {
    //     w = image.getWidth(this);
    //     h = image.getHeight(this);
    //   }
    //   tracker.removeImage(image);
    // }
    String img = String.format("<p><img src='%s' width='%d' height='%d'></p>", path, w, h);
    return html1 + img + html2;
  }

  private void saveImage(JComponent c) {
    float s = .02f;
    int w = Math.round(c.getWidth() * s);
    int h = Math.round(c.getHeight() * s);
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();
    g2.scale(s, s);
    c.print(g2);
    g2.dispose();
    try {
      File tmp = File.createTempFile("jst_tmp", ".jpg");
      tmp.deleteOnExit();
      ImageIO.write(image, "jpeg", tmp);
      label.setIcon(new ImageIcon(tmp.getAbsolutePath()));
    } catch (IOException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      label.setIcon(null);
      UIManager.getLookAndFeel().provideErrorFeedback(c);
    }
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

class ImageLoadSynchronouslyHtmlEditorKit extends HTMLEditorKit {
  @Override public ViewFactory getViewFactory() {
    return new HTMLEditorKit.HTMLFactory() {
      @Override public View create(Element elem) {
        View view = super.create(elem);
        if (view instanceof ImageView) {
          ((ImageView) view).setLoadsSynchronously(true);
        }
        return view;
      }
    };
  }

  // @Override public Document createDefaultDocument() {
  //   Document doc = super.createDefaultDocument ();
  //   ((HTMLDocument) doc).setAsynchronousLoadPriority(-1);
  //   return doc;
  // }
}
