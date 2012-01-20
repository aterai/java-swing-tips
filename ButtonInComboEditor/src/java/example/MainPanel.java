package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

class MainPanel extends JPanel {
    private final JComboBox combo01;
    private final JComboBox combo02;
    private final ImageIcon image1;
    private final ImageIcon image2;
    private final ImageIcon rss;
    public MainPanel() {
        super(new GridLayout(2,1));
        image1  = new ImageIcon(getClass().getResource("favicon.png"));
        image2  = new ImageIcon(getClass().getResource("16x16.png"));
        rss     = new ImageIcon(getClass().getResource("feed-icon-14x14.png")); //http://feedicons.com/
        combo01 = makeTestComboBox(makeModel());
        combo02 = makeTestComboBox(makeModel());

        final JTextField field = (JTextField) combo02.getEditor().getEditorComponent();
        final JButton button = new JButton(rss);
        final JLabel label = new JLabel(image1);
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
        label.setBorder(BorderFactory.createEmptyBorder(0,1,0,2));
        button.setRolloverIcon(makeFilteredImage(rss));
        button.setRolloverIcon(makeFilteredImage2(rss));
        button.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                System.out.println("clicked...");
            }
        });
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(0,1,0,2));
        combo02.add(button);
        combo02.add(label);
//         field.setBorder(BorderFactory.createEmptyBorder(0,16+4,0,14+2));
//         field.addComponentListener(new ComponentAdapter() {
//             @Override public void componentResized(ComponentEvent e) {
//                 Rectangle r = field.getBounds();
//                 label.setBounds(1, 0, 16, r.height);
//                 button.setBounds(r.width-14, 0, 14, r.height);
//             }
//         });
        combo02.setLayout(new LayoutManager() {
            @Override public void addLayoutComponent(String name, Component comp) {}
            @Override public void removeLayoutComponent(Component comp) {}
            @Override public Dimension preferredLayoutSize(Container parent) {
                return parent.getPreferredSize();
            }
            @Override public Dimension minimumLayoutSize(Container parent) {
                return parent.getMinimumSize();
            }
            @Override public void layoutContainer(Container parent) {
                if(!(parent instanceof JComboBox)) return;
                JComboBox cb     = (JComboBox)parent;
                int width        = cb.getWidth();
                int height       = cb.getHeight();
                Insets insets    = cb.getInsets();
                int buttonHeight = height - (insets.top + insets.bottom);
                int buttonWidth  = buttonHeight;
                int labelWidth   = buttonHeight;
                int loupeWidth   = buttonHeight;

                JButton arrowButton = (JButton)cb.getComponent(0);
                if(arrowButton != null) {
                    Insets arrowInsets = arrowButton.getInsets();
                    buttonWidth = arrowButton.getPreferredSize().width + arrowInsets.left + arrowInsets.right;
                    arrowButton.setBounds(width - (insets.right + buttonWidth), insets.top, buttonWidth, buttonHeight);
                }
                if(label != null) {
                    Insets labelInsets = label.getInsets();
                    labelWidth = label.getPreferredSize().width + labelInsets.left + labelInsets.right;
                    label.setBounds(insets.left, insets.top, labelWidth, buttonHeight);
                }
                JButton rssButton = button;
//                 for(Component c: cb.getComponents()) {
//                     if("ComboBox.rssButton".equals(c.getName())) {
//                         rssButton = (JButton)c;
//                         break;
//                     }
//                 }
                if(rssButton != null && rssButton.isVisible()) {
                    Insets loupeInsets = rssButton.getInsets();
                    loupeWidth = rssButton.getPreferredSize().width + loupeInsets.left + loupeInsets.right;
                    rssButton.setBounds(width - (insets.right + loupeWidth + buttonWidth), insets.top, loupeWidth, buttonHeight);
                }else{
                    loupeWidth = 0;
                }

                Component editor = cb.getEditor().getEditorComponent();
                if( editor != null ) {
                    editor.setBounds(insets.left + labelWidth, insets.top,
                                     width  - (insets.left + insets.right + buttonWidth + labelWidth + loupeWidth),
                                     height - (insets.top  + insets.bottom));
                }
            }
        });
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                //field.setBorder(BorderFactory.createEmptyBorder(0,16+4,0,0));
                button.setVisible(false);
            }
            @Override public void focusLost(FocusEvent e) {
                TestItem item = getTestItemFromModel(field.getText());
                label.setIcon(item.favicon);
                button.setVisible(item.hasRSS);
                //if(item.hasRSS) {
                //    field.setBorder(BorderFactory.createEmptyBorder(0,16+4,0,14+2));
                //}else{
                //    field.setBorder(BorderFactory.createEmptyBorder(0,16+4,0,0));
                //}
                combo02.setSelectedIndex(0);
            }
        });
        combo02.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()!=ItemEvent.SELECTED) return;
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        Object o = combo02.getSelectedItem();
                        TestItem i = (o instanceof TestItem)?(TestItem)o:getTestItemFromModel(o.toString());
                        label.setIcon(i.favicon);
                    }
                });
            }
        });
        add(makeTitlePanel("setEditable(true)", Arrays.asList(combo01, combo02)));
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 200));
    }
    private TestItem getTestItemFromModel(String text) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) combo02.getModel();
        TestItem item = null;
        for(int i=0;i<model.getSize();i++) {
            TestItem tmp = (TestItem)model.getElementAt(i);
            if(tmp.url.equals(text)) {
                item = tmp;
                break;
            }
        }
        if(item!=null) {
            model.removeElement(item);
            model.insertElementAt(item, 0);
        }else{
            ImageIcon icon = getFavicon(text);
            boolean hasRSS = hasRSS(text);
            model.insertElementAt(item = new TestItem(text, icon, hasRSS), 0);
        }
        return item;
    }

    private ImageIcon getFavicon(String url) {
        if(url.startsWith("http://terai.xrea.jp/")) {
            return image1;
        }else{
            return image2;
        }
    }
    private boolean hasRSS(String url) {
        return url.startsWith("http://terai.xrea.jp/");
    }
    static class SelectedImageFilter extends RGBImageFilter {
        //public SelectedImageFilter() {
        //    canFilterIndexColorModel = true;
        //}
        private static final float scale = 1.2f;
        @Override public int filterRGB(int x, int y, int argb) {
            //int a = (argb >> 24) & 0xff;
            int r = (argb >> 16) & 0xff;
            int g = (argb >> 8)  & 0xff;
            int b = (argb)       & 0xff;
            r = (int)(r*scale);
            g = (int)(g*scale);
            b = (int)(b*scale);
            if(r > 255) r = 255;
            if(g > 255) g = 255;
            if(b > 255) b = 255;
            return (argb & 0xff000000) | (r<<16) | (g<<8) | (b);
        }
    }
    private static ImageIcon makeFilteredImage(ImageIcon srcIcon) {
        ImageProducer ip = new FilteredImageSource(srcIcon.getImage().getSource(), new SelectedImageFilter());
        return new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
    }
    private static ImageIcon makeFilteredImage2(ImageIcon srcIcon) {
        RescaleOp op = new RescaleOp(
            new float[] { 1.2f,1.2f,1.2f,1.0f },
            new float[] { 0f,0f,0f,0f }, null);
        BufferedImage img = new BufferedImage(
            srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        //g.drawImage(srcIcon.getImage(), 0, 0, null);
        srcIcon.paintIcon(null, g, 0, 0);
        g.dispose();
        return new ImageIcon(op.filter(img, null));
    }
    private DefaultComboBoxModel makeModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(new TestItem("http://terai.xrea.jp/", image1, true));
        model.addElement(new TestItem("http://terai.xrea.jp/Swing.html", image1, true));
        model.addElement(new TestItem("http://terai.xrea.jp/JavaWebStart.html", image1, true));
        model.addElement(new TestItem("http://d.hatena.ne.jp/aterai/", image2, true));
        model.addElement(new TestItem("http://java-swing-tips.blogspot.com/", image2, true));
        model.addElement(new TestItem("http://www.example.com/", image2, false));
        return model;
    }
    private static JComboBox makeTestComboBox(DefaultComboBoxModel model) {
        JComboBox combo = new JComboBox(model);
        combo.setEditable(true);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                c.setIcon(((TestItem)value).favicon);
                return c;
            }
        });
        return combo;
    }
    private static class TestItem{
        public final String url;
        public final ImageIcon favicon;
        public final boolean hasRSS;
        public TestItem(String url, ImageIcon icon, boolean hasRSS) {
            this.url = url;
            this.favicon = icon;
            this.hasRSS = hasRSS;
        }
        @Override public String toString() {
            return url;
        }
    }
    private JComponent makeTitlePanel(String title, java.util.List<? extends JComponent> list) {
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder(title));
        for(JComponent cmp:list) {
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
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
