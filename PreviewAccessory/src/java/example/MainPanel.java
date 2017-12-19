package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.util.Objects;
import javax.swing.*;

public class MainPanel extends JPanel {
    protected transient JFileChooser fileChooser;

    public MainPanel() {
        super(new GridBagLayout());
        JButton button = new JButton("Open JFileChooser");
        button.addActionListener(e -> {
            fileChooser.showOpenDialog(getRootPane());
        });
        add(button);
        setPreferredSize(new Dimension(320, 240));
    }
    @Override public void updateUI() {
        super.updateUI();
        fileChooser = new JFileChooser();
        fileChooser.setAccessory(new ImagePreview(fileChooser));
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

// How to Use File Choosers (The Java™ Tutorials > Creating a GUI With JFC/Swing > Using Swing Components)
// https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
// https://docs.oracle.com/javase/tutorial/uiswing/examples/components/index.html#FileChooserDemo2
// https://docs.oracle.com/javase/tutorial/uiswing/examples/components/FileChooserDemo2Project/src/components/ImagePreview.java
class ImagePreview extends JComponent implements PropertyChangeListener {
    private static final int PREVIEW_WIDTH  = 90;
    private static final int PREVIEW_MARGIN = 5;
    private ImageIcon thumbnail;
    private File file;
    protected ImagePreview(JFileChooser fc) {
        super();
        fc.addPropertyChangeListener(this);
        //setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, SystemColor.inactiveCaption));
    }
    @Override public Dimension getPreferredSize() {
        return new Dimension(PREVIEW_WIDTH + PREVIEW_MARGIN * 2, 50);
    }
    private static ImageIcon getImageThumbnail(File file) {
        if (Objects.isNull(file)) {
            return null;
        }
        ImageIcon tmpIcon = new ImageIcon(file.getPath());
        if (tmpIcon.getIconWidth() > PREVIEW_WIDTH) {
            // Image img = tmpIcon.getImage().getScaledInstance(PREVIEW_WIDTH, -1, Image.SCALE_DEFAULT);
            // The Perils of Image.getScaledInstance() | Java.net
            // <del>http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html</del>
            // The Perils of Image.getScaledInstance() Blog | Oracle Community
            // https://community.oracle.com/docs/DOC-983611
            float scale = PREVIEW_WIDTH / (float) tmpIcon.getIconWidth();
            int newW = (int) (tmpIcon.getIconWidth()  * scale);
            int newH = (int) (tmpIcon.getIconHeight() * scale);
            BufferedImage img = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(tmpIcon.getImage(), 0, 0, newW, newH, null);
            g2.dispose();
            return new ImageIcon(img);
        } else {
            return tmpIcon;
        }
    }
    @Override public void propertyChange(PropertyChangeEvent e) {
        boolean update = false;
        String prop = e.getPropertyName();
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
            file = null;
            update = true;
        } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
            file = (File) e.getNewValue();
            update = true;
        }
        if (update) {
            thumbnail = null;
            if (isShowing()) {
                thumbnail = getImageThumbnail(file);
                repaint();
            }
        }
    }
    @Override protected void paintComponent(Graphics g) {
        if (Objects.isNull(thumbnail)) {
            thumbnail = getImageThumbnail(file);
        }
        if (Objects.nonNull(thumbnail)) {
            int x = Math.max(PREVIEW_MARGIN, getWidth() / 2 - thumbnail.getIconWidth() / 2);
            int y = Math.max(0, getHeight() / 2 - thumbnail.getIconHeight() / 2);
            thumbnail.paintIcon(this, g, x, y);
        }
    }
}
