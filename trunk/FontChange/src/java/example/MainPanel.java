package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;
import javax.swing.*;
import javax.swing.plaf.*;

public final class MainPanel extends JPanel {
    private static final Font FONT12 = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private static final Font FONT24 = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
    private static final Font FONT32 = new Font(Font.SANS_SERIF, Font.PLAIN, 32);
    private final JToolBar toolbar = new JToolBar();
    private final JLabel label = new JLabel("Test:");
    private final JComboBox<String> combo = new JComboBox<>(new String[] {"Test"});
    private final JButton button = new JButton(new AbstractAction("Dialog") {
        @Override public void actionPerformed(ActionEvent e) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(button, "MessageDialog", "Change All Font Size", JOptionPane.ERROR_MESSAGE);
        }
    });
    public MainPanel() {
        super(new BorderLayout());
        toolbar.add(new AbstractAction("12") {
            @Override public void actionPerformed(ActionEvent e) {
                updateFont(FONT12);
            }
        });
        toolbar.add(new AbstractAction("24") {
            @Override public void actionPerformed(ActionEvent e) {
                updateFont(FONT24);
            }
        });
        toolbar.add(new AbstractAction("32") {
            @Override public void actionPerformed(ActionEvent e) {
                updateFont(FONT32);
            }
        });

        add(toolbar, BorderLayout.NORTH);
        add(createCompButtonPanel());
        updateFont(FONT12);
        setPreferredSize(new Dimension(320, 240));
    }
    private void updateFont(Font font) {
        FontUIResource fontUIResource = new FontUIResource(font);
        for(Object o: UIManager.getLookAndFeelDefaults().keySet()) {
            if(o.toString().toLowerCase(Locale.ENGLISH).endsWith("font")) {
                UIManager.put(o, fontUIResource);
            }
        }
        recursiveUpdateUI(this); //SwingUtilities.updateComponentTreeUI(this);
        //Container c = getTopLevelAncestor();
        //if(c!=null && c instanceof Window) {
        //    ((Window)c).pack();
        //}
        Window window = SwingUtilities.getWindowAncestor(this);
        if(window != null) {
            window.pack();
        }
    }
    private void recursiveUpdateUI(JComponent p) {
        for(Component c: p.getComponents()) {
            if(c instanceof JToolBar) {
                continue;
            }else if(c instanceof JComponent) {
                JComponent jc = (JComponent)c;
                jc.updateUI();
                if(jc.getComponentCount()>0) {
                    recursiveUpdateUI(jc);
                }
            }
        }
    }
    private JComponent createCompButtonPanel() {
        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel(new GridBagLayout());

        c.gridheight = 1;
        c.gridwidth  = 1;
        c.gridy = 0;

        c.gridx = 0;
        c.weightx = 0.0;
        c.insets = new Insets(5, 5, 5, 0);
        c.anchor = GridBagConstraints.WEST;
        panel.add(label, c);

        c.gridx = 1;
        c.weightx = 1.0;
        c.insets = new Insets(5, 5, 5, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(combo, c);

        c.gridx = 2;
        c.weightx = 0.0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        panel.add(button, c);

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
