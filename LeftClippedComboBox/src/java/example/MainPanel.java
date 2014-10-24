package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JComboBox<String> combo = makeComboBox();
        initComboBoxRenderer(combo);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(makeTitledBox("Left Clip JComboBox", combo), BorderLayout.NORTH);
        add(makeTitledBox("Default JComboBox", makeComboBox()), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 200));
    }
    private static JButton getArrowButton(JComboBox box) {
        for (Component c: box.getComponents()) {
            if (c instanceof JButton) { //&& "ComboBox.arrowButton".equals(c.getName())) {
                //System.out.println(c.getName());
                return (JButton) c;
            }
        }
        return null;
    }
    private static Box makeTitledBox(String title, JComboBox combo) {
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder(title));
        box.add(Box.createVerticalStrut(2));
        box.add(combo);
        return box;
    }
    private static JComboBox<String> makeComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("1234567890123456789012/3456789012345678901234567890123/456789012345678901234567890.jpg");
        model.addElement("aaaa.tif");
        model.addElement("\\asdfsadfs\\afsdfasdf\\asdfasdfasd.avi");
        model.addElement("aaaabbbcc.pdf");
        model.addElement("c:/b12312343245/643667345624523451/324513/41234125/134513451345135125123412341bb1.mpg");
        model.addElement("file://localhost/1234567890123456789012/3456789012345678901234567890123/456789012345678901234567890.jpg");
        return new JComboBox<String>(model);
    }
    private static void initComboBoxRenderer(final JComboBox<String> combo) {
        final JButton arrowButton = getArrowButton(combo);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                int itb = 0;
                int ilr = 0;
                Insets insets = getInsets();
                itb += insets.top + insets.bottom;
                ilr += insets.left + insets.right;
                insets = combo.getInsets();
                itb += insets.top + insets.bottom;
                ilr += insets.left + insets.right;
                int availableWidth = combo.getWidth() - ilr;
                if (index < 0) {
                    //@see BasicComboBoxUI#rectangleForCurrentValue
                    int buttonSize = combo.getHeight() - itb;
                    if (arrowButton != null) {
                        buttonSize = arrowButton.getWidth();
                    }
                    availableWidth -= buttonSize;
                    JTextField tf = (JTextField) combo.getEditor().getEditorComponent();
                    insets = tf.getMargin();
                    //availableWidth -= insets.left;
                    availableWidth -= insets.left + insets.right;
                }
                String cellText = Objects.toString(value, "");
                //<blockquote cite="http://tips4java.wordpress.com/2008/11/12/left-dot-renderer/">
                //@title Left Dot Renderer
                //@auther Rob Camick
                FontMetrics fm = getFontMetrics(getFont());
                if (fm.stringWidth(cellText) > availableWidth) {
                    String dots = "...";
                    int textWidth = fm.stringWidth(dots);
                    int nChars = cellText.length() - 1;
                    while (nChars > 0) {
                        textWidth += fm.charWidth(cellText.charAt(nChars));
                        if (textWidth > availableWidth) {
                            break;
                        }
                        nChars--;
                    }
                    setText(dots + cellText.substring(nChars + 1));
                }
                //</blockquote>
                return this;
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
