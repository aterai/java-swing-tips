// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(4, 1, 5, 5));
    setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
    p.add(new JLabel("Default JComboBox"));
    p.add(new JComboBox<>(new String[] {"Google", "Yahoo!", "Bing"}));
    p.add(new JLabel("SearchBar JComboBox"));

    DefaultComboBoxModel<SearchEngine> m = new SearchEngineComboBoxModel<>();
    m.addElement(new SearchEngine("Google", "https://www.google.com/", makeIcon("google")));
    m.addElement(new SearchEngine("Yahoo!", "https://www.yahoo.com/", makeIcon("yahoo")));
    m.addElement(new SearchEngine("Bing", "https://www.bing.com/", makeIcon("bing")));

    JComboBox<SearchEngine> combo = new SearchBarComboBox<>(m);
    combo.getEditor().setItem("java swing");

    // JComboBox combo = new JComboBox(m);
    // combo.setUI(new BasicSearchBarComboBoxUI());
    // EventQueue.invokeLater(new Runnable() {
    //   @Override public void run() {
    //     SearchEngine se = combo.getItemAt(0);
    //     JButton arrowButton = (JButton) combo.getComponent(0);
    //     arrowButton.setIcon(se.favicon);
    //     combo.getEditor().setItem("java swing");
    //   }
    // });

    p.add(combo);
    // p.add(new SearchBarComboBox(makeModel()));
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static ImageIcon makeIcon(String name) {
    String path = "example/" + name + ".png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource(path);
    return Optional.ofNullable(url)
        .map(ImageIcon::new)
        .orElseGet(MainPanel::makeMissingImage);
  }

  private static ImageIcon makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (16 - iw) / 2, (16 - ih) / 2);
    g2.dispose();
    return new ImageIcon(bi);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.put("example.SearchBarComboBox", "SearchBarComboBoxUI");
      UIManager.put("SearchBarComboBoxUI", "example.BasicSearchBarComboBoxUI");
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

class SearchEngine {
  private final String name;
  private final String url;
  private final ImageIcon favicon;

  protected SearchEngine(String name, String url, ImageIcon icon) {
    this.name = name;
    this.url = url;
    this.favicon = icon;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public ImageIcon getFavicon() {
    return favicon;
  }

  @Override public String toString() {
    return getName();
  }
}

class SearchEngineComboBoxModel<E extends SearchEngine> extends DefaultComboBoxModel<E> {
  @Override public void setSelectedItem(Object anObject) {
    // System.out.println("model: " + anObject);
  }
}

class SearchBarComboBox<E extends SearchEngine> extends JComboBox<E> {
  private static final String UI_CLASS_ID = "SearchBarComboBoxUI";

  // protected SearchBarComboBox() {
  //   super();
  //   setModel(new DefaultComboBoxModel<>());
  //   init();
  // }

  protected SearchBarComboBox(ComboBoxModel<E> model) {
    super(model);
    // setModel(model);
    // init();
  }

  @SafeVarargs protected SearchBarComboBox(E... items) {
    super(items);
    // setModel(new DefaultComboBoxModel<>(items));
    // init();
  }

  // protected SearchBarComboBox(Vector<?> items) {
  //   super();
  //   setModel(new DefaultComboBoxModel(items));
  //   init();
  // }

  @Override public String getUIClassID() {
    return UI_CLASS_ID;
  }

  @Override public SearchBarComboBoxUI getUI() {
    return (SearchBarComboBoxUI) ui;
  }

  // @Override public void setUI(SearchBarComboBoxUI newUI) {
  //   super.setUI(newUI);
  // }

  @Override public void updateUI() {
    super.updateUI();
    if (Objects.nonNull(UIManager.get(getUIClassID()))) {
      setUI((SearchBarComboBoxUI) UIManager.getUI(this));
    } else {
      setUI(new BasicSearchBarComboBoxUI());
    }
    UIManager.put("ComboBox.font", getFont()); // XXX: ???
    JButton arrowButton = (JButton) getComponent(0);
    SearchEngine se = getItemAt(0);
    if (Objects.nonNull(se)) {
      arrowButton.setIcon(se.getFavicon());
    }
    // ListCellRenderer<? super SearchEngine> renderer = getRenderer();
    // if (renderer instanceof Component) {
    //   SwingUtilities.updateComponentTreeUI((Component) renderer);
    // }
  }

  // private void init() {
  //   installAncestorListener();
  //   // setUIProperty("opaque", true);
  //   updateUI();
  // }

  @Override protected void processFocusEvent(FocusEvent e) {
    // System.out.println("processFocusEvent");
  }
}
