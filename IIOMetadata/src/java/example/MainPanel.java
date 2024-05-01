// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    IIOMetadataNode root = null;
    Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpeg");
    JTabbedPane tab = new JTabbedPane();
    JTextArea log = new JTextArea();
    tab.addTab("text", new JScrollPane(log));

    // [JDK-8080225]
    // FileInput/OutputStream/FileChannel cleanup should be improved - Java Bug System
    // https://bugs.openjdk.org/browse/JDK-8080225
    // try (InputStream is = getClass().getResourceAsStream("test.jpg");
    //      ImageInputStream iis = ImageIO.createImageInputStream(is)) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/test.jpg");
    // try (InputStream is = Files.newInputStream(Paths.get(url.toURI()));
    //      ImageInputStream iis = ImageIO.createImageInputStream(is)) {
    if (url != null) {
      StringBuilder buf = new StringBuilder();
      try (ImageInputStream iis = ImageIO.createImageInputStream(url.openStream())) {
        // FileInputStream source = new FileInputStream(new File("c:/tmp/test.jpg"));
        ImageReader reader = readers.next();
        reader.setInput(iis, true);

        // ImageReadParam param = reader.getDefaultReadParam();
        buf.append(String.format("Width: %d%n", reader.getWidth(0)))
            .append(String.format("Height: %d%n", reader.getHeight(0)));

        IIOMetadata meta = reader.getImageMetadata(0);
        for (String s : meta.getMetadataFormatNames()) {
          buf.append(String.format("MetadataFormatName: %s%n", s));
        }

        root = (IIOMetadataNode) meta.getAsTree("javax_imageio_jpeg_image_1.0");
        // root = (IIOMetadataNode) meta.getAsTree("javax_imageio_1.0");

        NodeList com = root.getElementsByTagName("com");
        if (com.getLength() > 0) {
          String comment = ((IIOMetadataNode) com.item(0)).getAttribute("comment");
          buf.append(String.format("Comment: %s%n", comment));
        }
        buf.append("------------\n");
        print(buf, root, 0);
      } catch (IOException ex) {
        ex.printStackTrace();
        buf.append(String.format("%s%n", ex.getMessage()));
        UIManager.getLookAndFeel().provideErrorFeedback(this);
      }
      log.setText(buf.toString());
      JTree tree = new JTree(new DefaultTreeModel(new XmlTreeNode(root)));
      tab.addTab("tree", new JScrollPane(tree));
    }
    add(tab);
    setPreferredSize(new Dimension(320, 240));
  }

  private void print(StringBuilder buf, Node node, int level) {
    String indent = String.join("", Collections.nCopies(level * 2, " "));
    buf.append(String.format("%s%s%n", indent, node.getNodeName()));
    if (node.hasAttributes()) {
      for (int i = 0; i < node.getAttributes().getLength(); i++) {
        Node attr = node.getAttributes().item(i);
        String nodeName = attr.getNodeName();
        String nodeValue = attr.getNodeValue();
        buf.append(String.format("%s  #%s=%s%n", indent, nodeName, nodeValue));
      }
    }
    if (node.hasChildNodes()) {
      for (int i = 0; i < node.getChildNodes().getLength(); i++) {
        Node child = node.getChildNodes().item(i);
        print(buf, child, level + 1);
      }
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// https://community.oracle.com/thread/1373824 XmlViewer
class XmlTreeNode implements TreeNode {
  private final Node xmlNode;
  private XmlTreeNode parent;
  private List<XmlTreeNode> list;
  private final Boolean showAttributes;

  protected XmlTreeNode(Node xmlNode) {
    this.xmlNode = xmlNode;
    this.showAttributes = Boolean.TRUE;
  }

  protected XmlTreeNode(Node xmlNode, XmlTreeNode parent) {
    this(xmlNode);
    this.parent = parent;
  }

  public boolean isShowAttributes() {
    return Optional.ofNullable(showAttributes)
        .orElseGet(() -> parent != null && parent.isShowAttributes());
    // if (Objects.nonNull(showAttributes)) {
    //   return showAttributes;
    // }
    // return Objects.nonNull(parent) && parent.isShowAttributes();
  }

  // public void setShowAttributes(Boolean set) {
  //   showAttributes = set;
  // }

  private String getXmlTag() {
    String str;
    // boolean includeAttributes = isShowAttributes();
    if (xmlNode instanceof Element && isShowAttributes()) {
      str = getAttributesString((Element) xmlNode);
    } else if (xmlNode instanceof Text) {
      str = xmlNode.getNodeValue();
    } else {
      str = xmlNode.getNodeName();
    }
    return str;
  }

  private static String getAttributesString(Element e) {
    StringBuilder buf = new StringBuilder();
    buf.append(e.getTagName());
    if (e.hasAttributes()) {
      NamedNodeMap attr = e.getAttributes();
      int count = attr.getLength();
      for (int i = 0; i < count; i++) {
        Node a = attr.item(i);
        if (i == 0) {
          buf.append(" [");
        } else {
          buf.append(", ");
        }
        buf.append(a.getNodeName()).append('=').append(a.getNodeValue());
      }
      buf.append(']');
    }
    return buf.toString();
  }

  // private List<XmlTreeNode> getChildren() {
  //   if (Objects.isNull(list)) {
  //     loadChildren();
  //   }
  //   return new ArrayList<>(list);
  // }

  private void loadChildren() {
    NodeList cn = xmlNode.getChildNodes();
    int count = cn.getLength();
    list = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      Node c = cn.item(i);
      if (c instanceof Text && c.getNodeValue().isEmpty()) {
        continue;
      }
      list.add(makeXmlTreeNode(c));
    }
  }

  private XmlTreeNode makeXmlTreeNode(Node node) {
    return new XmlTreeNode(node, this);
  }

  @Override public Enumeration<XmlTreeNode> children() {
    if (Objects.isNull(list)) {
      loadChildren();
    }
    Iterator<XmlTreeNode> iterator = list.iterator();
    return new Enumeration<XmlTreeNode>() {
      @Override public boolean hasMoreElements() {
        return iterator.hasNext();
      }

      @Override public XmlTreeNode nextElement() {
        return iterator.next();
      }
    };
  }

  @Override public boolean getAllowsChildren() {
    return true;
  }

  @Override public TreeNode getChildAt(int childIndex) {
    if (Objects.isNull(list)) {
      loadChildren();
    }
    return list.get(childIndex);
  }

  @Override public int getChildCount() {
    if (Objects.isNull(list)) {
      loadChildren();
    }
    return list.size();
  }

  @Override public int getIndex(TreeNode node) {
    if (Objects.isNull(list)) {
      loadChildren();
    }
    return IntStream.range(0, list.size())
        .filter(i -> Objects.equals(xmlNode, list.get(i).xmlNode))
        .findFirst()
        .orElse(-1);
    // int i = 0;
    // for (XmlTreeNode c : list) {
    //   if (Objects.equals(xmlNode, c.xmlNode)) {
    //     return i;
    //   }
    //   i++;
    // }
    // return -1;
  }

  @Override public TreeNode getParent() {
    return parent;
  }

  @Override public boolean isLeaf() {
    boolean leaf;
    if (xmlNode instanceof Element) {
      leaf = false;
    } else {
      if (Objects.isNull(list)) {
        loadChildren();
      }
      leaf = list.isEmpty();
    }
    return leaf;
  }

  @Override public String toString() {
    return getXmlTag();
  }
}
