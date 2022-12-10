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

    String img0 = String.format("<p><img src='%s'></p>", path);
    String st0 = html1 + img0 + html2;
    JEditorPane editor0 = new JEditorPane();
    editor0.setEditorKit(new HTMLEditorKit());
    editor0.setText(st0);
    tabs.addTab("default", new JScrollPane(editor0));

    int w = 2048;
    int h = 1360;
    // Image img = Toolkit.getDefaultToolkit().createImage(path);
    // MediaTracker tracker = new MediaTracker((Container) this);
    // tracker.addImage(img, 0);
    // try {
    //   tracker.waitForID(0);
    // } catch (InterruptedException ex) {
    //   ex.printStackTrace();
    // } finally {
    //   if (!tracker.isErrorID(0)) {
    //     w = img.getWidth(this);
    //     h = img.getHeight(this);
    //   }
    //   tracker.removeImage(img);
    // }
    String img1 = String.format("<p><img src='%s' width='%d' height='%d'></p>", path, w, h);
    String st1 = html1 + img1 + html2;
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
      switch (tabs.getSelectedIndex()) {
        case 2:
          editor2.setText(st0);
          saveImage(editor2);
          break;
        case 1:
          editor1.setText(st1);
          saveImage(editor1);
          break;
        default:
          editor0.setText(st0);
          saveImage(editor0);
          break;
      }
    });

    add(tabs);
    add(label, BorderLayout.EAST);
    setPreferredSize(new Dimension(320, 240));
  }

  private void saveImage(JComponent c) {
    EventQueue.invokeLater(() -> {
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
        ex.printStackTrace();
        label.setIcon(null);
        UIManager.getLookAndFeel().provideErrorFeedback(c);
      }
    });
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
