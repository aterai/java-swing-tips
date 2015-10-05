package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private JFileChooser fileChooser;
    public MainPanel() {
        super(new GridBagLayout());
        JButton button = new JButton(new AbstractAction("Open JFileChooser") {
            @Override public void actionPerformed(ActionEvent e) {
                if (fileChooser == null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setAccessory(new ImagePreview(fileChooser));
                }
                fileChooser.showOpenDialog(getRootPane());
            }
        });
        add(button);
        setPreferredSize(new Dimension(320, 240));
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

//How to Use File Choosers
//http://docs.oracle.com/javase/tutorial/uiswing/examples/components/index.html#FileChooserDemo2
//  http://docs.oracle.com/javase/tutorial/uiswing/examples/components/FileChooserDemo2Project/src/components/ImagePreview.java
class ImagePreview extends JComponent implements PropertyChangeListener {
    private static final int PREVIEW_WIDTH  = 90;
    private static final int PREVIEW_MARGIN = 5;
    private ImageIcon thumbnail;
    private File file;
    public ImagePreview(JFileChooser fc) {
        super();
        fc.addPropertyChangeListener(this);
        //setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, SystemColor.inactiveCaption));
    }
    @Override public Dimension getPreferredSize() {
        return new Dimension(PREVIEW_WIDTH + PREVIEW_MARGIN * 2, 50);
    }
    private void loadImage() {
        if (file == null) {
            thumbnail = null;
            return;
        }
        ImageIcon tmpIcon = new ImageIcon(file.getPath());
        if (tmpIcon.getIconWidth() > PREVIEW_WIDTH) {
            //Image img = tmpIcon.getImage().getScaledInstance(PREVIEW_WIDTH, -1, Image.SCALE_DEFAULT);
            //The Perils of Image.getScaledInstance() | Java.net
            //http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
            float scale = PREVIEW_WIDTH / (float) tmpIcon.getIconWidth();
            int newW = (int) (tmpIcon.getIconWidth()  * scale);
            int newH = (int) (tmpIcon.getIconHeight() * scale);
            BufferedImage img = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(tmpIcon.getImage(), 0, 0, newW, newH, null);
            g2.dispose();
            thumbnail = new ImageIcon(img);
        } else {
            thumbnail = tmpIcon;
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
                loadImage();
                repaint();
            }
        }
    }
    @Override protected void paintComponent(Graphics g) {
        if (thumbnail == null) {
            loadImage();
        }
        if (thumbnail != null) {
            int x = getWidth()  / 2 - thumbnail.getIconWidth()  / 2;
            int y = getHeight() / 2 - thumbnail.getIconHeight() / 2;
            if (y < 0) {
                y = 0;
            }
            if (x < PREVIEW_MARGIN) {
                x = PREVIEW_MARGIN;
            }
            thumbnail.paintIcon(this, g, x, y);
        }
    }
}
