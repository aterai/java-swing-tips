package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final ImageIcon image1;
    private final ImageIcon image2;
    public MainPanel() {
        super(new GridLayout(2, 1));
        image1  = new ImageIcon(getClass().getResource("favicon.png"));
        image2  = new ImageIcon(getClass().getResource("16x16.png"));
        ImageIcon rss = new ImageIcon(getClass().getResource("feed-icon-14x14.png")); //http://feedicons.com/

        JComboBox<URLItem> combo01 = new JComboBox<>(makeTestModel());
        initComboBox(combo01);

        JComboBox<URLItem> combo02 = new URLItemComboBox(makeTestModel(), rss);
        initComboBox(combo02);

        add(makeTitlePanel("setEditable(true)", Arrays.asList(combo01, combo02)));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private DefaultComboBoxModel<URLItem> makeTestModel() {
        DefaultComboBoxModel<URLItem> model = new DefaultComboBoxModel<>();
        model.addElement(new URLItem("http://terai.xrea.jp/", image1, true));
        model.addElement(new URLItem("http://terai.xrea.jp/Swing.html", image1, true));
        model.addElement(new URLItem("http://terai.xrea.jp/JavaWebStart.html", image1, true));
        model.addElement(new URLItem("http://d.hatena.ne.jp/aterai/", image2, true));
        model.addElement(new URLItem("http://java-swing-tips.blogspot.com/", image2, true));
        model.addElement(new URLItem("http://www.example.com/", image2, false));
        return model;
    }
    private static void initComboBox(JComboBox<URLItem> combo) {
        combo.setEditable(true);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setIcon(((URLItem) value).favicon);
                return c;
            }
        });
    }
    private static JComponent makeTitlePanel(String title, List<? extends JComponent> list) {
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder(title));
        for (JComponent cmp:list) {
            box.add(Box.createVerticalStrut(5));
            box.add(cmp);
        }
        box.add(Box.createVerticalStrut(2));
        JPanel p = new JPanel(new BorderLayout());
        p.add(box, BorderLayout.NORTH);
        return p;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class URLItemComboBox extends JComboBox<URLItem> {
    public URLItemComboBox(DefaultComboBoxModel<URLItem> model, ImageIcon rss) {
        super(model);

        final JTextField field = (JTextField) getEditor().getEditorComponent();
        final JButton button = makeRssButton(rss);
        final JLabel label = makeLabel(field);
        setLayout(new ComboBoxLayout(label, button));
        add(button);
        add(label);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                //field.setBorder(BorderFactory.createEmptyBorder(0, 16 + 4, 0, 0));
                button.setVisible(false);
            }
            @Override public void focusLost(FocusEvent e) {
                URLItem item = getURLItemFromModel(field.getText());
                label.setIcon(item.favicon);
                button.setVisible(item.hasRSS);
                setSelectedIndex(0);
            }
        });
        addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() != ItemEvent.SELECTED) {
                    return;
                }
                updateFavicon(label);
            }
        });
        updateFavicon(label);
    }
    private void updateFavicon(final JLabel label) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                Object o = getSelectedItem();
                URLItem i = o instanceof URLItem ? (URLItem) o : getURLItemFromModel(o.toString());
                label.setIcon(i.favicon);
            }
        });
    }
    private static JButton makeRssButton(ImageIcon rss) {
        JButton button = new JButton(rss);
        ImageProducer ip = new FilteredImageSource(rss.getImage().getSource(), new SelectedImageFilter());
        button.setRolloverIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip)));
        //button.setRolloverIcon(makeFilteredImage(rss));
        //button.setRolloverIcon(makeFilteredImage2(rss));
        button.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                System.out.println("clicked...");
            }
        });
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
        return button;
    }
    private static JLabel makeLabel(final JTextField field) {
        JLabel label = new JLabel();
        label.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        field.requestFocusInWindow();
                        field.selectAll();
                    }
                });
            }
        });
        label.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        label.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
        return label;
    }
    private URLItem getURLItemFromModel(String text) {
        DefaultComboBoxModel<URLItem> model = (DefaultComboBoxModel<URLItem>) getModel();
        URLItem item = null;
        for (int i = 0; i < model.getSize(); i++) {
            URLItem tmp = model.getElementAt(i);
            if (tmp.url.equals(text)) {
                item = tmp;
                break;
            }
        }
        if (item != null) {
            model.removeElement(item);
            model.insertElementAt(item, 0);
        }
        return item;
    }
//     private ImageIcon getFavicon(String url) {
//         if (url.startsWith("http://terai.xrea.jp/")) {
//             return image1;
//         } else {
//             return image2;
//         }
//     }
//     private boolean hasRSS(String url) {
//         return url.startsWith("http://terai.xrea.jp/");
//     }
//     public static ImageIcon makeFilteredImage(ImageIcon srcIcon) {
//         ImageProducer ip = new FilteredImageSource(srcIcon.getImage().getSource(), new SelectedImageFilter());
//         return new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
//     }
//     //Test:
//     public static ImageIcon makeFilteredImage2(ImageIcon srcIcon) {
//         RescaleOp op = new RescaleOp(new float[] { 1.2f, 1.2f, 1.2f, 1f }, new float[] { 0f, 0f, 0f, 0f }, null);
//         BufferedImage img = new BufferedImage(srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
//         //TEST: RescaleOp op = new RescaleOp(1.2f, 0.0f, null);
//         //BufferedImage img = new BufferedImage(srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
//         Graphics g = img.getGraphics();
//         //g.drawImage(srcIcon.getImage(), 0, 0, null);
//         srcIcon.paintIcon(null, g, 0, 0);
//         g.dispose();
//         return new ImageIcon(op.filter(img, null));
//     }
}

class ComboBoxLayout implements LayoutManager {
    private final JLabel label;
    private final JButton button;
    public ComboBoxLayout(JLabel label, JButton button) {
        this.label = label;
        this.button = button;
    }
    @Override public void addLayoutComponent(String name, Component comp) { /* not needed */ }
    @Override public void removeLayoutComponent(Component comp) { /* not needed */ }
    @Override public Dimension preferredLayoutSize(Container parent) {
        return parent.getPreferredSize();
    }
    @Override public Dimension minimumLayoutSize(Container parent) {
        return parent.getMinimumSize();
    }
    @Override public void layoutContainer(Container parent) {
        if (!(parent instanceof JComboBox)) {
            return;
        }
        JComboBox cb     = (JComboBox) parent;
        int width        = cb.getWidth();
        int height       = cb.getHeight();
        Insets insets    = cb.getInsets();
        int buttonHeight = height - insets.top - insets.bottom;
        int buttonWidth  = buttonHeight;
        int labelWidth   = buttonHeight;
        int loupeWidth; //   = buttonHeight;

        JButton arrowButton = (JButton) cb.getComponent(0);
        if (arrowButton != null) {
            Insets arrowInsets = arrowButton.getInsets();
            buttonWidth = arrowButton.getPreferredSize().width + arrowInsets.left + arrowInsets.right;
            arrowButton.setBounds(width - insets.right - buttonWidth, insets.top, buttonWidth, buttonHeight);
        }
        if (label != null) {
            Insets labelInsets = label.getInsets();
            labelWidth = label.getPreferredSize().width + labelInsets.left + labelInsets.right;
            label.setBounds(insets.left, insets.top, labelWidth, buttonHeight);
        }
        JButton rssButton = button;
        //for (Component c: cb.getComponents()) {
        //    if ("ComboBox.rssButton".equals(c.getName())) {
        //        rssButton = (JButton) c;
        //        break;
        //    }
        //}
        if (rssButton != null && rssButton.isVisible()) {
            Insets loupeInsets = rssButton.getInsets();
            loupeWidth = rssButton.getPreferredSize().width + loupeInsets.left + loupeInsets.right;
            rssButton.setBounds(width - insets.right - loupeWidth - buttonWidth, insets.top, loupeWidth, buttonHeight);
        } else {
            loupeWidth = 0;
        }

        Component editor = cb.getEditor().getEditorComponent();
        if (editor != null) {
            editor.setBounds(insets.left + labelWidth, insets.top,
                             width  - insets.left - insets.right - buttonWidth - labelWidth - loupeWidth,
                             height - insets.top  - insets.bottom);
        }
    }
}

class URLItem {
    public final String url;
    public final ImageIcon favicon;
    public final boolean hasRSS;
    public URLItem(String url, ImageIcon icon, boolean hasRSS) {
        this.url = url;
        this.favicon = icon;
        this.hasRSS = hasRSS;
    }
    @Override public String toString() {
        return url;
    }
}

class SelectedImageFilter extends RGBImageFilter {
    //public SelectedImageFilter() {
    //    canFilterIndexColorModel = false;
    //}
    private static final float SCALE = 1.2f;
    @Override public int filterRGB(int x, int y, int argb) {
        //int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >>  8) & 0xff;
        int b = (argb)       & 0xff;
        r = (int) (r * SCALE);
        g = (int) (g * SCALE);
        b = (int) (b * SCALE);
        if (r > 255) { r = 255; }
        if (g > 255) { g = 255; }
        if (b > 255) { b = 255; }
        return (argb & 0xff000000) | (r << 16) | (g << 8) | (b);
    }
}
