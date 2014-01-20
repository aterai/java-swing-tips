package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.*;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(1,2));

        JTree tree = makeTree(getDefaultTreeModel2());
        tree.setCellRenderer(new MultiLineCellRenderer());

        add(makeTitledPanel("Html", makeTree(getDefaultTreeModel())));
        add(makeTitledPanel("TextAreaRenderer", tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(c));
        return p;
    }
    private static JTree makeTree(TreeModel model) {
        JTree tree = new JTree(model);
        tree.setRowHeight(0);
        for(int i=0;i<tree.getRowCount();i++) {
            tree.expandRow(i);
        }
        return tree;
    }
    private static TreeModel getDefaultTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("JTree");
        DefaultMutableTreeNode parent;

        parent = new DefaultMutableTreeNode("colors");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("<html>blue<br>&nbsp;&nbsp;blue, blue"));
        parent.add(new DefaultMutableTreeNode("<html>violet<br>&ensp;&ensp;violet"));
        parent.add(new DefaultMutableTreeNode("<html>red<br>&emsp;red<br>&emsp;red"));
        parent.add(new DefaultMutableTreeNode("<html>yellow<br>\u3000yellow"));

        parent = new DefaultMutableTreeNode("sports");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("basketball"));
        parent.add(new DefaultMutableTreeNode("soccer"));
        parent.add(new DefaultMutableTreeNode("football"));
        parent.add(new DefaultMutableTreeNode("hockey"));

        parent = new DefaultMutableTreeNode("food");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("hot dogs"));
        parent.add(new DefaultMutableTreeNode("pizza"));
        parent.add(new DefaultMutableTreeNode("ravioli"));
        parent.add(new DefaultMutableTreeNode("bananas"));
        return new DefaultTreeModel(root);
    }

    private static TreeModel getDefaultTreeModel2() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("JTree");
        DefaultMutableTreeNode parent;

        parent = new DefaultMutableTreeNode("colors");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("blue\n  blue, blue"));
        parent.add(new DefaultMutableTreeNode("violet\n    violet"));
        parent.add(new DefaultMutableTreeNode("red\n red\n red"));
        parent.add(new DefaultMutableTreeNode("yellow\n\u3000yellow"));

        parent = new DefaultMutableTreeNode("sports");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("basketball"));
        parent.add(new DefaultMutableTreeNode("soccer"));
        parent.add(new DefaultMutableTreeNode("football"));
        parent.add(new DefaultMutableTreeNode("hockey"));

        parent = new DefaultMutableTreeNode("food");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("hot dogs"));
        parent.add(new DefaultMutableTreeNode("pizza"));
        parent.add(new DefaultMutableTreeNode("ravioli"));
        parent.add(new DefaultMutableTreeNode("bananas"));
        return new DefaultTreeModel(root);
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

class LeafTreeCellEditor extends DefaultTreeCellEditor {
    public LeafTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
    }
    @Override public boolean isCellEditable(EventObject e) {
        boolean b = super.isCellEditable(e);
        Object o = tree.getLastSelectedPathComponent();
        if(b && o!=null && o instanceof TreeNode) {
            return ((TreeNode)o).isLeaf();
        }else{
            return b;
        }
    }
}

class MultiLineCellRenderer extends JPanel implements TreeCellRenderer {
    private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    private final JLabel icon = new JLabel();
    private final JTextArea text = new CellTextArea2();

    public MultiLineCellRenderer() {
        super(new BorderLayout(0, 0));
        //text.setLineWrap(true);
        //text.setWrapStyleWord(true);
        text.setOpaque(true);
        text.setFont(icon.getFont());
        text.setBorder(BorderFactory.createEmptyBorder());
        icon.setOpaque(true);
        icon.setBorder(BorderFactory.createEmptyBorder(1,1,1,2));
        icon.setVerticalAlignment(JLabel.TOP);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        add(icon, BorderLayout.WEST);
        add(text);
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        //String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, hasFocus);
        //setText(stringValue);
        JLabel l = (JLabel)renderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
        //setEnabled(tree.isEnabled());
        Color bColor, fColor;
        if(isSelected) {
            bColor = renderer.getBackgroundSelectionColor();
            fColor = renderer.getTextSelectionColor();
        }else{
            bColor = renderer.getBackgroundNonSelectionColor();
            fColor = renderer.getTextNonSelectionColor();
            if(bColor == null) { bColor = renderer.getBackground(); }
            if(fColor == null) { fColor = renderer.getForeground(); }
        }
        text.setFont(l.getFont());
        text.setText(l.getText());
        text.setForeground(fColor);
        text.setBackground(bColor);
        //text.setBorder(hasFocus?renderer.getBorder():emptyBorder);

        icon.setIcon(l.getIcon());
        icon.setBackground(bColor);
        return this;
    }
    @Override public void updateUI() {
        super.updateUI();
        renderer = new DefaultTreeCellRenderer();
    }
}

class CellTextArea extends JTextArea {
    private Dimension preferredSize;
    @Override public void setPreferredSize(Dimension d) {
        if(d != null) {
            preferredSize = d;
        }
    }
    @Override public Dimension getPreferredSize() {
        return preferredSize;
    }
    //Multi-line tree items
    //http://www.codeguru.com/java/articles/141.shtml
    @Override public void setText(String str) {
        FontMetrics fm = getFontMetrics(getFont());
        BufferedReader br = new BufferedReader(new StringReader(str));
        String line;
        int maxWidth = 0, lines = 0;
        try{
            while((line = br.readLine()) != null) {
                int width = SwingUtilities.computeStringWidth(fm, line);
                if(maxWidth < width) {
                    maxWidth = width;
                }
                lines++;
            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        lines = lines < 1 ? 1 : lines;
        int height = fm.getHeight() * lines;
        Insets i = getInsets();
        setPreferredSize(new Dimension(maxWidth + i.left+i.right, height+i.top+i.bottom));
        super.setText(str);
    }
}

class CellTextArea2 extends JTextArea {
    @Override public Dimension getPreferredSize() {
        Dimension d = new Dimension(10, 10);
        //d = (d == null) ? new Dimension(400, 400) : d;
        Insets i = getInsets();
        d.width  = Math.max(d.width,  getColumns()*getColumnWidth() + i.left + i.right);
        d.height = Math.max(d.height, getRows()*getRowHeight() + i.top + i.bottom);
        return d;
    }
    @Override public void setText(String str) {
        super.setText(str);
        FontMetrics fm = getFontMetrics(getFont());
        Document doc   = getDocument();
        Element root   = doc.getDefaultRootElement();
        int lineCount  = root.getElementCount(); //= root.getElementIndex(doc.getLength());
        int maxWidth   = 10;
        try{
            for(int i = 0; i < lineCount; i++) {
                Element e = root.getElement(i);
                int rangeStart = e.getStartOffset();
                int rangeEnd = e.getEndOffset();
                String line = doc.getText(rangeStart, rangeEnd - rangeStart);
                int width = fm.stringWidth(line);
                if(maxWidth < width) {
                    maxWidth = width;
                }
            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        setRows(lineCount);
        setColumns(1+maxWidth/getColumnWidth());
    }
}
