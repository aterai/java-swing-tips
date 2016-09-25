package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

public class MainPanel extends JPanel {
    private static final String SHOW_MNEMONICS = "Button.showMnemonics";
    private final JCheckBox showMnemonicsCheck = new JCheckBox(SHOW_MNEMONICS);

    public MainPanel() {
        super();
        showMnemonicsCheck.setSelected(UIManager.getBoolean(SHOW_MNEMONICS));
        showMnemonicsCheck.setMnemonic('B');
        showMnemonicsCheck.addActionListener(e -> {
            UIManager.put(SHOW_MNEMONICS, ((JCheckBox) e.getSource()).isSelected());
            if (UIManager.getLookAndFeel() instanceof WindowsLookAndFeel) {
                System.out.println("isMnemonicHidden: " + WindowsLookAndFeel.isMnemonicHidden());
                WindowsLookAndFeel.setMnemonicHidden(true);
                SwingUtilities.getRoot(this).repaint();
            }
        });
        add(showMnemonicsCheck);

        JButton button = new JButton("Dummy");
        button.setMnemonic('D');
        add(button);

        setPreferredSize(new Dimension(320, 240));
    }
    private static JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.add(createMenu("File", Arrays.asList("Open", "Save", "Exit")));
        mb.add(createMenu("Edit", Arrays.asList("Cut", "Copy", "Paste", "Delete")));
        mb.add(LookAndFeelUtil.createLookAndFeelMenu());
        mb.add(Box.createGlue());
        mb.add(createMenu("Help", Arrays.asList("Version", "About")));
        return mb;
    }
    private static JMenu createMenu(String title, List<String> list) {
        JMenu menu = new JMenu(title);
        menu.setMnemonic(title.codePointAt(0));
        for (String s: list) {
            JMenuItem m = new JMenuItem(s);
            m.setMnemonic(s.codePointAt(0));
            menu.add(m);
        }
        return menu;
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
        frame.setJMenuBar(createMenuBar());
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
        menu.setMnemonic('L');
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
        lafItem.setMnemonic(lafName.codePointAt(0));
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
