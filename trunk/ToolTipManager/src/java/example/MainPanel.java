package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JLabel label1 = new JLabel("label");
    private final JTextField field = new JTextField(20);
    private final JButton button = new JButton("button");
    public MainPanel() {
        super(new BorderLayout());
        label1.setToolTipText("label - ToolTip");
        field.setToolTipText("aaaaaaaaaaaaaaaaaaaaa");
        button.setToolTipText("button - ToolTip");

        ToolTipManager.sharedInstance().unregisterComponent(field);

        JPanel p = new JPanel();
        p.add(label1);
        p.add(field);
        p.add(button);
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("ToolTip Test"));
        panel.add(p, BorderLayout.NORTH);
        panel.add(new JScrollPane(new JTextArea("dummy")));
        add(makeToolPanel(), BorderLayout.NORTH);
        add(panel);
        setPreferredSize(new Dimension(320, 200));
    }

    private final JRadioButton onRadio  = new JRadioButton("on");
    private final JRadioButton offRadio = new JRadioButton("off");
    private final ButtonGroup group = new ButtonGroup();
    private JComponent makeToolPanel() {
        JPanel panel = new JPanel();
        group.add(onRadio);
        group.add(offRadio);
        onRadio.setSelected(true);
        ActionListener al = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                ToolTipManager.sharedInstance().setEnabled(onRadio.isSelected());
            }
        };
        onRadio.addActionListener(al);
        offRadio.addActionListener(al);
        panel.add(new JLabel("ToolTip enabled:"));
        panel.add(onRadio);
        panel.add(offRadio);
        panel.setBorder(BorderFactory.createTitledBorder("ToolTipManager"));
        return panel;
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
