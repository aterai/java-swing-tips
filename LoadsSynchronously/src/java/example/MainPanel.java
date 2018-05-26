package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Collections;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

public final class MainPanel extends JPanel {
    private static final String TEXT = "<span style='background-color:red'>aaaaaaaaaaaaaaaaaaa</span><br />";
    private final JLabel label = new JLabel("screenshot");
    private final JTabbedPane tabs = new JTabbedPane();
    private final JEditorPane editor0 = new JEditorPane();
    private final JEditorPane editor1 = new JEditorPane();
    private final JEditorPane editor2 = new JEditorPane();

    private MainPanel() {
        super(new BorderLayout());

        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setHorizontalTextPosition(SwingConstants.CENTER);

        // URL path = getClass().getResource("CRW_3857_JFR.jpg");
        String path = "https://raw.githubusercontent.com/aterai/java-swing-tips/master/LoadsSynchronously/src/java/example/CRW_3857_JFR.jpg";

        int w = 2048;
        int h = 1360;
        // Image img = Toolkit.getDefaultToolkit().createImage(path);
        // MediaTracker tracker = new MediaTracker((Container) this);
        // tracker.addImage(img, 0);
        // try {
        //     tracker.waitForID(0);
        // } catch (InterruptedException ex) {
        //     ex.printStackTrace();
        // } finally {
        //     if (!tracker.isErrorID(0)) {
        //         w = img.getWidth(this);
        //         h = img.getHeight(this);
        //     }
        //     tracker.removeImage(img);
        // }

        String str = String.join("\n", Collections.nCopies(50, TEXT));
        StringBuilder sb0 = new StringBuilder(str);
        StringBuilder sb1 = new StringBuilder(str);

        sb0.append(String.format("<p><img src='%s'></p>", path));
        sb1.append(String.format("<p><img src='%s' width='%d' height='%d'></p>", path, w, h));
        IntStream.range(0, 3).forEach(i -> {
            sb0.append(TEXT);
            sb1.append(TEXT);
        });

        String st0 = sb0.toString();
        editor0.setEditorKit(new HTMLEditorKit());
        editor0.setText(st0);
        tabs.addTab("default", new JScrollPane(editor0));

        String st1 = sb1.toString();
        editor1.setEditorKit(new HTMLEditorKit());
        editor1.setText(st1);
        tabs.addTab("<img width='%d' ...", new JScrollPane(editor1));

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
            double s = .02;
            int w = (int) (c.getWidth() * s);
            int h = (int) (c.getHeight() * s);
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            g2.scale(s, s);
            c.paint(g2);
            g2.dispose();
            try {
                File tmp = File.createTempFile("jst_tmp", ".jpg");
                tmp.deleteOnExit();
                ImageIO.write(image, "jpeg", tmp);
                label.setIcon(new ImageIcon(tmp.getAbsolutePath()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
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
    //     Document doc = super.createDefaultDocument ();
    //     ((HTMLDocument) doc).setAsynchronousLoadPriority(-1);
    //     return doc;
    // }
}
