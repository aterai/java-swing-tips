package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import org.w3c.dom.*;

class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        IIOMetadataNode root = null;
        final StringBuilder buf = new StringBuilder();
        InputStream source = getClass().getResourceAsStream("test.jpg");
        try{
            Iterator readers = ImageIO.getImageReadersByFormatName("jpeg");
            ImageReader reader = (ImageReader)readers.next();
            //FileInputStream source = new FileInputStream(new File("c:/tmp/test.jpg"));
            ImageInputStream iis = ImageIO.createImageInputStream(source);
            reader.setInput(iis, true);

            //ImageReadParam param = reader.getDefaultReadParam();
            buf.append(String.format("Width: %d\n", reader.getWidth(0)));
            buf.append(String.format("Height: %d\n", reader.getHeight(0)));

            IIOMetadata meta = reader.getImageMetadata(0);
            for(String s:meta.getMetadataFormatNames())
              buf.append(String.format("MetadataFormatName: %s\n",s));

            root = (IIOMetadataNode)meta.getAsTree("javax_imageio_jpeg_image_1.0");
            //root = (IIOMetadataNode) meta.getAsTree("javax_imageio_1.0");

            NodeList com = root.getElementsByTagName("com");
            if(com!=null && com.getLength()>0) {
                String comment = ((IIOMetadataNode)com.item(0)).getAttribute("comment");
                buf.append(String.format("Comment: %s\n", comment));
            }
            buf.append("------------\n");
            print(buf, root, 0);
        }catch(Exception e) {
            e.printStackTrace();
        }finally{
            try{
                source.close();
            }catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
        JTextArea log = new JTextArea(buf.toString());
        JTree tree = new JTree(new DefaultTreeModel(new XMLTreeNode(root)));
        JTabbedPane tab = new JTabbedPane();
        tab.add("text", new JScrollPane(log));
        tab.add("tree", new JScrollPane(tree));
        add(tab);
        setPreferredSize(new Dimension(320, 240));
    }

    private void print(StringBuilder buf, Node node, int level) {
        StringBuilder indent = new StringBuilder();
        int l = level * 2;
        while(l>0) {
            indent.append(" ");
            l--;
        }
        buf.append(String.format("%s%s\n", indent, node.getNodeName()));
        if(node.hasAttributes()) {
            for(int i=0;i<node.getAttributes().getLength();i++) {
                Node attr = node.getAttributes().item(i);
                buf.append(String.format("%s    #%s=%s\n", indent, attr.getNodeName(), attr.getNodeValue()));
            }
        }
        if(node.hasChildNodes()) {
            for(int i=0;i<node.getChildNodes().getLength();i++) {
                Node child = node.getChildNodes().item(i);
                print(buf, child, level+1);
            }
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

//Swing - XMLViewer
//http://forums.sun.com/thread.jspa?threadID=5131638
class XMLTreeNode implements TreeNode {
    private Node xmlNode;
    private XMLTreeNode parent;
    private ArrayList<XMLTreeNode> children;
    private Boolean showAttributes;
    public XMLTreeNode(Node xmlNode) {
        this.xmlNode = xmlNode;
        showAttributes = Boolean.TRUE;
    }
    protected XMLTreeNode(Node xmlNode, XMLTreeNode parent) {
        this(xmlNode);
        this.parent = parent;
    }
    private boolean getShowAttributes() {
        if(showAttributes != null) {
            return showAttributes.booleanValue();
        }
        if(parent != null) {
            return parent.getShowAttributes();
        }
        return false;
    }
    private void setShowAttributes(Boolean set) {
        showAttributes = set;
    }
    private String getXMLTag() {
        boolean includeAttributes = getShowAttributes();
        if(xmlNode instanceof Element && includeAttributes) {
            Element e = (Element)xmlNode;
            StringBuilder buf = new StringBuilder();
            buf.append(e.getTagName());
            if(e.hasAttributes()) {
                NamedNodeMap attr = e.getAttributes();
                int count = attr.getLength();
                for(int i=0;i<count;i++) {
                    Node a = attr.item(i);
                    if(i==0) {
                        buf.append(" [");
                    }else{
                        buf.append(", ");
                    }
                    buf.append(a.getNodeName()).append("=").append(a.getNodeValue());
                }
                buf.append(']');
            }
            return buf.toString();
        }else if(xmlNode instanceof Text) {
            return xmlNode.getNodeValue();
        }
        return xmlNode.getNodeName();
    }
    private java.util.List<XMLTreeNode> getChildren() {
        if(children==null) {
            loadChildren();
        }
        return new ArrayList<XMLTreeNode>(children);
    }
    private void loadChildren() {
        NodeList cn = xmlNode.getChildNodes();
        int count = cn.getLength();
        children = new ArrayList<XMLTreeNode>(count);
        for(int i=0;i<count;i++) {
            Node c = cn.item(i);
            if(c instanceof Text && c.getNodeValue().trim().length()==0) {
                continue;
            }
            children.add(new XMLTreeNode(cn.item(i), this));
        }
    }
    @Override public Enumeration children() {
        if(children==null) {
            loadChildren();
        }
        final Iterator<XMLTreeNode> iter = children.iterator();
        return new Enumeration() {
            @Override public boolean hasMoreElements() { return iter.hasNext(); }
            @Override public Object nextElement() { return iter.next(); }
        };
    }
    @Override public boolean getAllowsChildren() {
        return true;
    }
    @Override public TreeNode getChildAt(int childIndex) {
        if(children==null) {
            loadChildren();
        }
        return children.get(childIndex);
    }
    @Override public int getChildCount() {
        if(children==null) {
            loadChildren();
        }
        return children.size();
    }
    @Override public int getIndex(TreeNode node) {
        if(children==null) {
            loadChildren();
        }
        int i=0;
        for(XMLTreeNode c : children) {
            if(xmlNode==c.xmlNode) {
                return i;
            }
            i++;
        }
        return -1;
    }
    @Override public TreeNode getParent() {
        return parent;
    }
    @Override public boolean isLeaf() {
        if(xmlNode instanceof Element) {
            return false;
        }
        if(children==null) {
            loadChildren();
        }
        return children.isEmpty();
    }
    @Override public String toString() {
        return getXMLTag();
    }
}
