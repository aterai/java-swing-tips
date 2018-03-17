package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        ImageIcon image1 = new ImageIcon(getClass().getResource("favicon.png"));
        ImageIcon image2 = new ImageIcon(getClass().getResource("16x16.png"));
        ImageIcon rss = new ImageIcon(getClass().getResource("feed-icon-14x14.png")); // http://feedicons.com/

        JComboBox<UrlItem> combo01 = new JComboBox<>(makeTestModel(image1, image2));
        initComboBox(combo01);

        JComboBox<UrlItem> combo02 = new UrlItemComboBox(makeTestModel(image1, image2), rss);
        initComboBox(combo02);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder("setEditable(true)"));
        box.add(Box.createVerticalStrut(2));
        box.add(combo01);
        box.add(Box.createVerticalStrut(5));
        box.add(combo02);
        box.add(Box.createVerticalStrut(2));

        add(box, BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea()));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static DefaultComboBoxModel<UrlItem> makeTestModel(ImageIcon image1, ImageIcon image2) {
        DefaultComboBoxModel<UrlItem> model = new DefaultComboBoxModel<>();
        model.addElement(new UrlItem("https://ateraimemo.com/", image1, true));
        model.addElement(new UrlItem("https://ateraimemo.com/Swing.html", image1, true));
        model.addElement(new UrlItem("https://ateraimemo.com/JavaWebStart.html", image1, true));
        model.addElement(new UrlItem("https://github.com/aterai/java-swing-tips", image2, true));
        model.addElement(new UrlItem("https://java-swing-tips.blogspot.com/", image2, true));
        model.addElement(new UrlItem("http://www.example.com/", image2, false));
        return model;
    }
    private static void initComboBox(JComboBox<UrlItem> combo) {
        combo.setEditable(true);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setIcon(((UrlItem) value).favicon);
                return c;
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

class UrlItemComboBox extends JComboBox<UrlItem> {
    protected UrlItemComboBox(DefaultComboBoxModel<UrlItem> model, ImageIcon rss) {
        super(model);

        JTextField field = (JTextField) getEditor().getEditorComponent();
        JButton button = makeRssButton(rss);
        JLabel label = makeLabel(field);
        setLayout(new ComboBoxLayout(label, button));
        add(button);
        add(label);

        field.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) {
                // field.setBorder(BorderFactory.createEmptyBorder(0, 16 + 4, 0, 0));
                button.setVisible(false);
            }
            @Override public void focusLost(FocusEvent e) {
                getUrlItemFromModel(model, field.getText()).ifPresent(item -> {
                    model.removeElement(item);
                    model.insertElementAt(item, 0);
                    label.setIcon(item.favicon);
                    button.setVisible(item.hasRss);
                    setSelectedIndex(0);
                });
            }
        });
        addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateFavicon(model, label);
            }
        });
        updateFavicon(model, label);
    }
    private void updateFavicon(ComboBoxModel<UrlItem> model, JLabel l) {
        EventQueue.invokeLater(() -> getUrlItemFromModel(model, getSelectedItem()).ifPresent(i -> l.setIcon(i.favicon)));
    }
    private static JButton makeRssButton(ImageIcon rss) {
        JButton button = new JButton(rss);
        ImageProducer ip = new FilteredImageSource(rss.getImage().getSource(), new SelectedImageFilter());
        button.setRolloverIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip)));
        // button.setRolloverIcon(makeFilteredImage(rss));
        // button.setRolloverIcon(makeFilteredImage2(rss));
        button.addActionListener(e -> System.out.println("clicked..."));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
        return button;
    }
    private static JLabel makeLabel(JTextField field) {
        JLabel label = new JLabel();
        label.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                EventQueue.invokeLater(() -> {
                    field.requestFocusInWindow();
                    field.selectAll();
                });
            }
        });
        label.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        label.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
        return label;
    }
    protected Optional<UrlItem> getUrlItemFromModel(ComboBoxModel<UrlItem> model, Object o) {
        if (o instanceof UrlItem) {
            return Optional.of((UrlItem) o);
        }
        String str = Objects.toString(o, "");
        return IntStream.range(0, model.getSize())
            .mapToObj(model::getElementAt)
            .filter(ui -> ui.url.equals(str))
            .findFirst();
//         DefaultComboBoxModel<UrlItem> model = (DefaultComboBoxModel<UrlItem>) getModel();
//         UrlItem item = null;
//         for (int i = 0; i < model.getSize(); i++) {
//             UrlItem tmp = model.getElementAt(i);
//             if (tmp.url.equals(text)) {
//                 item = tmp;
//                 break;
//             }
//         }
//         if (Objects.nonNull(item)) {
//             model.removeElement(item);
//             model.insertElementAt(item, 0);
//         }
//         return item;
    }
//     private ImageIcon getFavicon(String url) {
//         if (url.startsWith("https://ateraimemo.com/")) {
//             return image1;
//         } else {
//             return image2;
//         }
//     }
//     private boolean hasRss(String url) {
//         return url.startsWith("https://ateraimemo.com/");
//     }
//     public static ImageIcon makeFilteredImage(ImageIcon srcIcon) {
//         ImageProducer ip = new FilteredImageSource(srcIcon.getImage().getSource(), new SelectedImageFilter());
//         return new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
//     }
//     // Test:
//     public static ImageIcon makeFilteredImage2(ImageIcon srcIcon) {
//         RescaleOp op = new RescaleOp(new float[] { 1.2f, 1.2f, 1.2f, 1f }, new float[] { 0f, 0f, 0f, 0f }, null);
//         BufferedImage img = new BufferedImage(srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
//         // TEST: RescaleOp op = new RescaleOp(1.2f, 0f, null);
//         // BufferedImage img = new BufferedImage(srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
//         Graphics g = img.getGraphics();
//         // g.drawImage(srcIcon.getImage(), 0, 0, null);
//         srcIcon.paintIcon(null, g, 0, 0);
//         g.dispose();
//         return new ImageIcon(op.filter(img, null));
//     }
}

class ComboBoxLayout implements LayoutManager {
    private final JLabel label;
    private final JButton button;
    protected ComboBoxLayout(JLabel label, JButton button) {
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
        JComboBox<?> cb = (JComboBox<?>) parent;
        int width = cb.getWidth();
        int height = cb.getHeight();
        Insets insets = cb.getInsets();
        int buttonHeight = height - insets.top - insets.bottom;
        int buttonWidth = buttonHeight;
        int labelWidth = buttonHeight;
        int loupeWidth; // = buttonHeight;

        JButton arrowButton = (JButton) cb.getComponent(0);
        if (Objects.nonNull(arrowButton)) {
            Insets arrowInsets = arrowButton.getInsets();
            buttonWidth = arrowButton.getPreferredSize().width + arrowInsets.left + arrowInsets.right;
            arrowButton.setBounds(width - insets.right - buttonWidth, insets.top, buttonWidth, buttonHeight);
        }
        if (Objects.nonNull(label)) {
            Insets labelInsets = label.getInsets();
            labelWidth = label.getPreferredSize().width + labelInsets.left + labelInsets.right;
            label.setBounds(insets.left, insets.top, labelWidth, buttonHeight);
        }
        JButton rssButton = button;
        // for (Component c: cb.getComponents()) {
        //     if ("ComboBox.rssButton".equals(c.getName())) {
        //         rssButton = (JButton) c;
        //         break;
        //     }
        // }
        if (Objects.nonNull(rssButton) && rssButton.isVisible()) {
            Insets loupeInsets = rssButton.getInsets();
            loupeWidth = rssButton.getPreferredSize().width + loupeInsets.left + loupeInsets.right;
            rssButton.setBounds(width - insets.right - loupeWidth - buttonWidth, insets.top, loupeWidth, buttonHeight);
        } else {
            loupeWidth = 0;
        }

        Component editor = cb.getEditor().getEditorComponent();
        if (Objects.nonNull(editor)) {
            editor.setBounds(insets.left + labelWidth, insets.top,
                             width  - insets.left - insets.right - buttonWidth - labelWidth - loupeWidth,
                             height - insets.top  - insets.bottom);
        }
    }
}

class UrlItem {
    public final String url;
    public final ImageIcon favicon;
    public final boolean hasRss;
    protected UrlItem(String url, ImageIcon icon, boolean hasRss) {
        this.url = url;
        this.favicon = icon;
        this.hasRss = hasRss;
    }
    @Override public String toString() {
        return url;
    }
}

class SelectedImageFilter extends RGBImageFilter {
    // public SelectedImageFilter() {
    //     canFilterIndexColorModel = false;
    // }
    private static final float SCALE = 1.2f;
    @Override public int filterRGB(int x, int y, int argb) {
        // int a = (argb >> 24) & 0xFF;
        int r = (int) Math.min(0xFF, ((argb >> 16) & 0xFF) * SCALE);
        int g = (int) Math.min(0xFF, ((argb >>  8) & 0xFF) * SCALE);
        int b = (int) Math.min(0xFF, ((argb)       & 0xFF) * SCALE);
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | (b);
    }
}
