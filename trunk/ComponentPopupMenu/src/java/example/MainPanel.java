package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());
        JTextArea textArea = new JTextArea("ComponentPopupMenu Test\naaaaaaaaaaa\nbbbbbbbbbbbbbb\ncccccccccccccc");
        textArea.setComponentPopupMenu(new TextComponentPopupMenu());
        add(new JScrollPane(textArea));
        setPreferredSize(new Dimension(320, 240));
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

class TextComponentPopupMenu extends JPopupMenu {
    private final Action cutAction = new DefaultEditorKit.CutAction();
    private final Action copyAction = new DefaultEditorKit.CopyAction();
    private final Action pasteAction = new DefaultEditorKit.PasteAction();
    private final Action deleteAction;
    private final Action selectAllAction;
    public TextComponentPopupMenu() {
        super();

//         addPopupMenuListener(new PopupMenuListener() {
//             public void popupMenuCanceled(PopupMenuEvent e) {}
//             public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
//             public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
//                 boolean flg = textArea.getSelectedText()!=null;
//                 cutAction.setEnabled(flg);
//                 copyAction.setEnabled(flg);
//                 deleteAction.setEnabled(flg);
//             }
//         });

        add(cutAction);
        add(copyAction);
        add(pasteAction);
        addSeparator();
        add(deleteAction = new AbstractAction("delete") {
            @Override public void actionPerformed(ActionEvent e) {
                ((JTextComponent)getInvoker()).replaceSelection(null);
            }
        });
        addSeparator();
        add(selectAllAction = new AbstractAction("select all") {
            @Override public void actionPerformed(ActionEvent e) {
                ((JTextComponent)getInvoker()).selectAll();
            }
        });
    }
    @Override public void show(Component c, int x, int y) {
        JTextComponent textArea = (JTextComponent)c;
        boolean flg = textArea.getSelectedText()!=null;
        cutAction.setEnabled(flg);
        copyAction.setEnabled(flg);
        deleteAction.setEnabled(flg);
        super.show(c, x, y);
    }
}
