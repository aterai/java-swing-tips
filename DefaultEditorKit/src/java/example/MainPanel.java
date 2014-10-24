package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final JTextField pf1 = new JTextField(30);
    private final JTextField pf2 = new JTextField(30);

    public MainPanel() {
        super(new BorderLayout());
        pf1.setComponentPopupMenu(new TextFieldPopupMenu());
        pf2.setComponentPopupMenu(new TextFieldPopupMenu());

        Box panel = Box.createVerticalBox();
        panel.setBorder(BorderFactory.createTitledBorder("E-mail Address"));
        panel.add(pf1);
        panel.add(Box.createVerticalStrut(5));
        panel.add(new JLabel("Please enter your email adress twice for confirmation:"));
        panel.add(pf2);
        panel.add(Box.createVerticalStrut(5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea("Dummy")));
        setPreferredSize(new Dimension(320, 240));
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
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class TextFieldPopupMenu extends JPopupMenu {
    private final Action cutAction = new DefaultEditorKit.CutAction();
    private final Action copyAction = new DefaultEditorKit.CopyAction();
    private final Action pasteAction = new DefaultEditorKit.PasteAction();
    private final Action deleteAction = new AbstractAction("delete") {
        @Override public void actionPerformed(ActionEvent e) {
            Component c = getInvoker();
            if (c instanceof JTextComponent) {
                ((JTextComponent) c).replaceSelection(null);
            }
        }
    };
    private final Action cut2Action = new AbstractAction("cut2") {
        @Override public void actionPerformed(ActionEvent e) {
            Component c = getInvoker();
            if (c instanceof JTextComponent) {
                ((JTextComponent) c).cut();
            }
        }
    };
    public TextFieldPopupMenu() {
        super();
        add(cutAction);
        add(copyAction);
        add(pasteAction);
        add(deleteAction);
        addSeparator();
        add(cut2Action);
    }
    @Override public void show(Component c, int x, int y) {
        boolean flg;
        if (c instanceof JTextComponent) {
            JTextComponent field = (JTextComponent) c;
            flg = field.getSelectedText() != null;
        } else {
            flg = false;
        }
        cutAction.setEnabled(flg);
        copyAction.setEnabled(flg);
        deleteAction.setEnabled(flg);
        cut2Action.setEnabled(flg);
        super.show(c, x, y);
    }
}
