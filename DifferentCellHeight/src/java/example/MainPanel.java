package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel{
    private final JList list1 = new JList();
    private final JList list2 = new JList();
    public MainPanel() {
        super(new GridLayout(1,0));
        list1.setCellRenderer(new TextAreaRenderer());

        list1.setModel(makeListModel());
        list2.setModel(makeListModel());

        add(new JScrollPane(list1));
        add(new JScrollPane(list2));
        setPreferredSize(new Dimension(320, 240));
    }
    private class TextAreaRenderer extends JTextArea implements ListCellRenderer {
        private final Border focusBorder = new DotBorder(new Color(~list1.getSelectionBackground().getRGB()),2);
        private final Border nomalBorder = BorderFactory.createEmptyBorder(2,2,2,2);
        private final Color evenColor = new Color(230,255,230);
        @Override public Component getListCellRendererComponent(JList list, Object object, int index, boolean isSelected, boolean cellHasFocus) {
            //setLineWrap(true);
            if(isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }else{
                setBackground(index%2==0 ? evenColor : list.getBackground());
                setForeground(list.getForeground());
            }
            setBorder(cellHasFocus ? focusBorder : nomalBorder);
            setText((object==null) ? "" : object.toString());
            return this;
        }
    }
    private DefaultListModel makeListModel() {
        DefaultListModel model = new DefaultListModel();
        model.addElement("一行");
        model.addElement("一行目\n二行目");
        model.addElement("一行目\n二行目\n三行目");
        model.addElement("四行\n以上ある\nテキスト\nの場合");
        //model.addElement("asdfas以上ある以上ある以上ある以上ある以上ある以上ある以上ある以上ある以上ある以上ある以上あるs");
        return model;
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

class DotBorder extends LineBorder {
    public DotBorder(Color color, int thickness) {
        super(color, thickness);
    }
    @Override public boolean isBorderOpaque() {return true;}
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D)g;
        g2.translate(x,y);
        g2.setPaint(getLineColor());
        BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
        g2.translate(-x,-y);
    }
}

// class DotBorder extends EmptyBorder {
//     private static final BasicStroke dashed = new BasicStroke(
//         1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
//         new float[]{1.0f}, 0.0f);
//     public boolean isBorderOpaque() {return true;}
//     public DotBorder(Insets borderInsets) {
//         super(borderInsets);
//     }
//     public DotBorder(int top, int left, int bottom, int right) {
//         super(top, left, bottom, right);
//     }
//     public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//         Graphics2D g2 = (Graphics2D)g;
//         g2.setPaint(c.getForeground());
//         g2.setStroke(dashed);
//         g2.translate(x,y);
//         g2.drawRect(0, 0, w-1, h-1);
//         g2.translate(-x,-y);
//     }
//     //public Insets getBorderInsets()
//     //public Insets getBorderInsets(Component c)
//     //public Insets getBorderInsets(Component c, Insets insets)
// }
