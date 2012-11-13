package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(new JLabel("JLabel:"), BorderLayout.WEST);
        p.add(new JTextField("JTextField"));
        p.setBorder(BorderFactory.createTitledBorder("JLabel+JTextFeild"));

        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.add(makePanel(), BorderLayout.NORTH);
        panel.add(p, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(panel, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 200));
    }
    @SuppressWarnings("unchecked")
    private static JPanel makePanel() {
        final JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(makeComboBox(new String[] {"aaaa", "bbbbbbbbbb", "ccccc"}));

        final Color bgc = UIManager.getColor("ComboBox.background");
        UIManager.put("ComboBox.squareButton", Boolean.FALSE);
        UIManager.put("ComboBox.background",   p.getBackground());
        Object[] items = {"JComboBox 11111:", "JComboBox 222:", "JComboBox 33:"};
        JComboBox comboBox = makeComboBox(items);
        final ListCellRenderer r = comboBox.getRenderer();
        comboBox.setRenderer(new ListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel)r.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setHorizontalAlignment(SwingConstants.RIGHT);
                if(isSelected) {
                    c.setForeground(list.getSelectionForeground());
                    c.setBackground(list.getSelectionBackground());
                }else{
                    c.setForeground(list.getForeground());
                    c.setBackground(bgc);
                }
                return c;
            }
        });
        comboBox.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton button = new JButton(); //.createArrowButton();
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setVisible(false);
                return button;
            }
        });
        comboBox.setOpaque(false);
        //comboBox.setBackground(p.getBackground());
        comboBox.setBorder(BorderFactory.createEmptyBorder(0,2,0,2));
        comboBox.setFocusable(false);

        p.add(comboBox, BorderLayout.WEST);
        p.setBorder(BorderFactory.createTitledBorder("JComboBox+JComboBox"));
        return p;
    }
    @SuppressWarnings("unchecked")
    private static JComboBox makeComboBox(Object[] model) {
        return new JComboBox(model);
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
//         UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Color.BLUE));
//         UIManager.put("ComboBox.selectionBackground", new ColorUIResource(Color.WHITE));

        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
