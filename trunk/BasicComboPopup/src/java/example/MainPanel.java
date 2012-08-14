package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.text.*;

public class MainPanel extends JPanel{
    private final JTextPane jtp = new JTextPane();
    private final BasicComboPopup popup;
    private final JComboBox combo = makeComboBox();

    public MainPanel() {
        super(new BorderLayout());
        JScrollPane scroll = new JScrollPane(jtp);

        popup = new BasicComboPopup(combo) {
            @Override public boolean isFocusable() {
                return true;
            }
            MouseAdapter listener = null;
            @Override protected void installListListeners() {
                super.installListListeners();
                listener = new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        hide();
                        System.out.println(comboBox.getSelectedItem());
                        append((String)combo.getSelectedItem());
                    }
                };
                if(listener!=null) {
                    list.addMouseListener(listener);
                }
            }
            //void uninstallListListeners() {
            @Override public void uninstallingUI() {
                if(listener != null) {
                    list.removeMouseListener(listener);
                    listener = null;
                }
                super.uninstallingUI();
            }
        };
        //popup.setFocusCycleRoot(true);

        Action up = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int index = combo.getSelectedIndex();
                combo.setSelectedIndex((index==0)?combo.getItemCount()-1:index-1);
            }
        };
        Action down = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int index = combo.getSelectedIndex();
                combo.setSelectedIndex((index==combo.getItemCount()-1)?0:index+1);
            }
        };
        Action ent = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                append((String)combo.getSelectedItem());
            }
        };

        ActionMap amc = popup.getActionMap();
        amc.put("myUp",   up);
        amc.put("myDown", down);
        amc.put("myEnt",  ent);

        InputMap imc = popup.getInputMap();
        imc.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),     "myUp");
        imc.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),   "myDown");
        imc.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),  "myEnt");

        ActionMap am = jtp.getActionMap();
        Action popAct = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                popupMenu(e);
            }
        };
        am.put("myPop", popAct);
        jtp.setActionMap(am);
        KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
        jtp.getInputMap().put(k, "myPop");

        add(scroll);
        setPreferredSize(new Dimension(320, 240));
    }
    @SuppressWarnings("unchecked")
    private static JComboBox makeComboBox() {
        String[] model = {
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "bbbbbbbb", "cccccccc",
            "dddddddd", "eeeeeeeee",
            "fff", "ggg", "hhhhhhhhh", "iii",
        };
        return new JComboBox(model);
    }
    private void popupMenu(ActionEvent e) {
        Rectangle rect = getRect();
        popup.show(jtp, rect.x, rect.y + rect.height);
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                SwingUtilities.getWindowAncestor(popup).toFront();
                popup.requestFocusInWindow();
            }
        });
    }
//     private void test_popupMenu(ActionEvent e) {
//         //System.out.println(EventQueue.isDispatchThread());
//         Rectangle rect = getRect();
//         Rectangle r = this.getBounds();
// //         System.out.println(popup.getWidth());
// //         System.out.println(rect.toString());
// //         System.out.println(r.toString());
//         if(!r.contains(rect.x+popup.getWidth(), rect.y+rect.height+popup.getHeight())) {
//             System.out.println("----------------------------");
//             popup.show(jtp, rect.x, rect.y + rect.height);
//             EventQueue.invokeLater(new Runnable() {
//                 @Override public void run() {
//                     JPanel parent = (JPanel)popup.getParent();
//                     System.out.println(parent);
//                     SwingUtilities.getWindowAncestor(popup).toFront();
//                     popup.requestFocusInWindow();
//                 }
//             });
//         }else{
//             popup.show(jtp, rect.x, rect.y + rect.height);
//             popup.requestFocusInWindow();
//         }
//     }
    private Rectangle getRect() {
        Rectangle rect = new Rectangle();
        try{
            rect = jtp.modelToView(jtp.getCaretPosition());
        }catch(BadLocationException ble) {
            ble.printStackTrace();
        }
        return rect;
    }
    private void append(final String str) {
        popup.hide();
        try{
            Document doc = jtp.getDocument();
            doc.insertString(jtp.getCaretPosition(), str, null);
        }catch(BadLocationException e) { e.printStackTrace(); }
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
