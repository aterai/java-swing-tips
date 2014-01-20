package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;

public class MainPanel extends JPanel {
    private final JTextField pf1 = new JTextField(30);
    private final JTextField pf2 = new JTextField(30);
    public MainPanel() {
        super(new BorderLayout());
        pf1.setComponentPopupMenu(new TextFieldPopupMenu(pf1));
        pf2.setComponentPopupMenu(new TextFieldPopupMenu(pf2));

        Box panel = Box.createVerticalBox();
        panel.setBorder(BorderFactory.createTitledBorder("E-mail Address"));
        panel.add(pf1);
        panel.add(Box.createVerticalStrut(5));
        panel.add(new JLabel("Please enter your email adress twice for confirmation:"));
        panel.add(pf2);
        panel.add(Box.createVerticalStrut(5));
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea("Dummy")));
        setPreferredSize(new Dimension(320, 200));

//         Box panel = Box.createVerticalBox();
//         panel.setBorder(BorderFactory.createTitledBorder("E-mail Address"));
//         panel.add(pf1);
//         panel.add(Box.createVerticalStrut(5));
//         panel.add(new JLabel("Please enter your email adress twice for confirmation"));
//         panel.add(pf2);
//         add(panel);
    }

    private class TextFieldPopupMenu extends JPopupMenu {
        private final Action cutAction = new DefaultEditorKit.CutAction();
        private final Action copyAction = new DefaultEditorKit.CopyAction();
        private final Action pasteAction = new DefaultEditorKit.PasteAction();
        private final Action deleteAction;
        private final Action cut2Action;
        //private final JTextField field;
        public TextFieldPopupMenu(final JTextField field) {
            super();
            //this.field = field;
            add(cutAction);
            add(copyAction);
            add(pasteAction);
            add(deleteAction = new AbstractAction("delete") {
                @Override public void actionPerformed(ActionEvent evt) {
                    field.replaceSelection(null);
                }
            });
            addSeparator();
            add(cut2Action = new AbstractAction("cut2") {
                @Override public void actionPerformed(ActionEvent evt) {
                    field.cut();
                }
            });
        }
        @Override public void show(Component c, int x, int y) {
            JTextField field = (JTextField)c;
            boolean flg = field.getSelectedText()!=null;
            cutAction.setEnabled(flg);
            copyAction.setEnabled(flg);
            deleteAction.setEnabled(flg);
            cut2Action.setEnabled(flg);
            super.show(c, x, y);
        }
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
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
