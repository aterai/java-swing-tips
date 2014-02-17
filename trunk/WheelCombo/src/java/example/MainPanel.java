package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JTextArea textArea = new JTextArea();
    public MainPanel() {
        super(new BorderLayout());

        JComboBox<String> combo = makeComboBox();
        combo.addMouseWheelListener(new MouseWheelListener() {
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                JComboBox source = (JComboBox)e.getComponent();
                if(!source.hasFocus()) {
                    return;
                }
                int ni = source.getSelectedIndex() + e.getWheelRotation();
                if(ni>=0 && ni<source.getItemCount()) {
                    source.setSelectedIndex(ni);
                }
            }
        });

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("JComboBox"));
        GridBagConstraints c = new GridBagConstraints();
        c.gridheight = 1;

        c.gridx   = 0;
        c.insets  = new Insets(5, 5, 5, 0);
        c.anchor  = GridBagConstraints.WEST;
        c.gridy   = 0; p.add(new JLabel("Wheel:"), c);
        c.gridy   = 1; p.add(new JLabel("Nomal:"), c);

        c.gridx   = 1;
        c.weightx = 1.0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.gridy   = 0; p.add(combo, c);
        c.gridy   = 1; p.add(makeComboBox(), c);

        textArea.setText("dummy");
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(textArea));
        setPreferredSize(new Dimension(320, 200));
    }
    private static JComboBox<String> makeComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("111111");
        model.addElement("22222222");
        model.addElement("3333333333");
        model.addElement("444444444444");
        model.addElement("5555555");
        model.addElement("66666666666");
        model.addElement("77777777");
        model.addElement("88888888888");
        return new JComboBox<String>(model);
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
        }catch(ClassNotFoundException | InstantiationException |
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
