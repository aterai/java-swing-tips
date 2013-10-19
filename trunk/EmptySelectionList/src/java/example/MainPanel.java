package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JList list1 = makeList();
    private final JList list2 = makeList();
    private final JList list3 = makeList();

    public MainPanel() {
        super(new GridLayout(1,0));

        list1.setEnabled(false);

        //System.out.println(UIManager.getBorder("List.focusCellHighlightBorder"));
        list2.setFocusable(false);
        list2.setSelectionModel(new DefaultListSelectionModel() {
            @Override public boolean isSelectedIndex(int index) {
                return false;
            }
        });

        setListCellRenderer(list3);

        add(new JScrollPane(list1));
        add(new JScrollPane(list2));
        add(new JScrollPane(list3));
        setPreferredSize(new Dimension(320, 200));
    }
    @SuppressWarnings("unchecked")
    private static JList makeList() {
        DefaultListModel model = new DefaultListModel();
        model.addElement("aaaaaaaaaaaa");
        model.addElement("bbbbb");
        model.addElement("ccc");
        model.addElement("dddddddddddd");
        model.addElement("eeeeeeee");
        model.addElement("fffff");
        model.addElement("gggggggg");
        model.addElement("hhhhhh");
        model.addElement("iiiiiiiiiiii");
        return new JList(model);
    }
    @SuppressWarnings("unchecked")
    private static void setListCellRenderer(JList l) {
        l.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, value, index, false, false);
            }
        });
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
