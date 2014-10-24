package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JComboBox combo = makeComboBox();
    private final JCheckBox check = new JCheckBox("<html>addAuxiliaryLookAndFeel<br>(Disable Right Click)");
    public MainPanel() {
        super(new BorderLayout());
        final LookAndFeel auxLookAndFeel = new AuxiliaryWindowsLookAndFeel();

        UIManager.put("ComboBox.font", combo.getFont());
        UIManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals("lookAndFeel")) {
                    String lnf = e.getNewValue().toString();
                    if (lnf.contains("Windows")) {
                        if (check.isSelected()) {
                            UIManager.addAuxiliaryLookAndFeel(auxLookAndFeel);
                        }
                        check.setEnabled(true);
                    } else {
                        UIManager.removeAuxiliaryLookAndFeel(auxLookAndFeel);
                        check.setEnabled(false);
                    }
                }
            }
        });
        check.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox check = (JCheckBox) e.getSource();
                String lnf = UIManager.getLookAndFeel().getName();
                if (check.isSelected() && lnf.contains("Windows")) {
                    UIManager.addAuxiliaryLookAndFeel(auxLookAndFeel);
                } else {
                    UIManager.removeAuxiliaryLookAndFeel(auxLookAndFeel);
                }
                SwingUtilities.updateComponentTreeUI(MainPanel.this);
            }
        });

        combo.setEditable(true);

        Box box = Box.createVerticalBox();
        box.add(check);
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(combo));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(makeComboBox()));
        box.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(box, BorderLayout.NORTH);
        add(new JScrollPane(new JTree()));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent createPanel(JComponent cmp) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(cmp);
        return panel;
    }
    private static JComboBox<String> makeComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("1354123451234513512");
        model.addElement("bbb1");
        model.addElement("bbb12");
        return new JComboBox<String>(model);
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
        JMenuBar mb = new JMenuBar();
        mb.add(LookAndFeelUtil.createLookAndFeelMenu());

        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setJMenuBar(mb);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
    private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private LookAndFeelUtil() { /* Singleton */ }
    public static JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        ButtonGroup lookAndFeelRadioGroup = new ButtonGroup();
        for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lookAndFeelRadioGroup));
        }
        return menu;
    }
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, final ButtonGroup lookAndFeelRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.setAction(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                ButtonModel m = lookAndFeelRadioGroup.getSelection();
                try {
                    setLookAndFeel(m.getActionCommand());
                } catch (ClassNotFoundException | InstantiationException
                       | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if (!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            //firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
