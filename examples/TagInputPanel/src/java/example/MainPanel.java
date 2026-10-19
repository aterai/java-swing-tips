// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    JTextArea log = new JTextArea();
    TagInputPanel tagInput = new TagInputPanel();
    tagInput.getTagContainer().addContainerListener(new ContainerListener() {
      @Override public void componentAdded(ContainerEvent e) {
        log.setText(String.join(", ", tagInput.getTags()));
      }

      @Override public void componentRemoved(ContainerEvent e) {
        log.setText(String.join(", ", tagInput.getTags()));
      }
    });
    add(tagInput, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
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
      Logger.getGlobal().severe(ex::getMessage);
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

class TagInputPanel extends JPanel {
  private final List<String> tags = new ArrayList<>();
  private final JTextField textField = new JTextField(15);
  private final JPanel tagContainer = new JPanel(new WrapFlowLayout(FlowLayout.LEFT, 5, 5)) {
    @Override public void updateUI() {
      super.updateUI();
      setBackground(UIManager.getColor("TextField.background"));
    }
  };

  protected TagInputPanel() {
    super(new BorderLayout());
    textField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    textField.addActionListener(e -> {
      String text = textField.getText().trim();
      if (!text.isEmpty() && !tags.contains(text)) {
        addTag(text);
        textField.setText("");
      }
    });
    textField.addKeyListener(new KeyAdapter() {
      @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && textField.getText().isEmpty()) {
          removeLastTag();
          e.consume();
        }
      }
    });
    tagContainer.add(textField);
    tagContainer.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        // The preferred height depends on the current width, so lay out
        // the ancestors again after the width has changed
        e.getComponent().revalidate();
      }
    });
    add(tagContainer);
  }

  @Override public void updateUI() {
    super.updateUI();
    setBorder(UIManager.getBorder("TextField.border"));
    setBackground(UIManager.getColor("TextField.background"));
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  public List<String> getTags() {
    // Return a read-only view so that callers cannot desynchronize it from the tag components
    return Collections.unmodifiableList(tags);
  }

  public Container getTagContainer() {
    return tagContainer;
  }

  private void addTag(String text) {
    JPanel tag = new JPanel(new BorderLayout(5, 0));
    tag.setName(text);
    tag.setBackground(new Color(0xE6_F5_FF));
    // tag.setBackground(UIManager.getColor("Table.dropLineColor"));
    tag.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UIManager.getColor("Table.selectionBackground")),
        BorderFactory.createEmptyBorder(3, 5, 3, 5)
    ));
    tags.add(text);
    tag.add(new JLabel(text));
    tag.add(createCloseButton(tag), BorderLayout.EAST);
    tagContainer.add(tag, tagContainer.getComponentCount() - 1);
    resizeAndRepaint();
  }

  private JButton createCloseButton(JPanel tag) {
    JButton closeButton = new JButton("×") {
      @Override public void updateUI() {
        super.updateUI();
        setContentAreaFilled(false);
        setFocusPainted(false);
        setFocusable(false);
        setBorder(BorderFactory.createEmptyBorder());
      }
    };
    closeButton.addActionListener(e -> {
      tags.remove(tag.getName());
      tagContainer.remove(tag);
      resizeAndRepaint();
    });
    closeButton.addMouseListener(new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        e.getComponent().setForeground(Color.RED);
      }

      @Override public void mouseExited(MouseEvent e) {
        e.getComponent().setForeground(UIManager.getColor("Button.foreground"));
      }
    });
    return closeButton;
  }

  private void removeLastTag() {
    if (!tags.isEmpty()) {
      // The tag components are kept in the same order as the tags list,
      // followed by the JTextField, so the last tag shares its index
      int lastIndex = tags.size() - 1;
      tags.remove(lastIndex); // Java 21: .removeLast();
      tagContainer.remove(lastIndex);
      resizeAndRepaint();
    }
  }

  private void resizeAndRepaint() {
    revalidate();
    repaint();
  }
}

class WrapFlowLayout extends FlowLayout {
  protected WrapFlowLayout(int align, int hgap, int vgap) {
    super(align, hgap, vgap);
  }

  @SuppressWarnings("PMD.AvoidSynchronizedStatement")
  @Override public Dimension preferredLayoutSize(Container target) {
    synchronized (target.getTreeLock()) {
      // Before the first layout the width is unknown, so fall back to a single row
      return target.getWidth() == 0
          ? super.preferredLayoutSize(target)
          : wrappedLayoutSize(target);
    }
  }

  private Dimension wrappedLayoutSize(Container target) {
    int targetWidth = target.getWidth();
    Insets insets = target.getInsets();
    int hgap = getHgap();
    int vgap = getVgap();
    // Wrap rows with the same rule as FlowLayout#layoutContainer(...)
    int maxWidth = targetWidth - insets.left - insets.right - hgap * 2;
    int height = insets.top + insets.bottom + vgap;
    int rowWidth = 0;
    int rowHeight = 0;
    for (Component c : target.getComponents()) {
      if (c.isVisible()) {
        Dimension d = c.getPreferredSize();
        if (rowWidth > 0 && rowWidth + d.width > maxWidth) {
          height += rowHeight + vgap;
          rowWidth = 0;
          rowHeight = 0;
        }
        rowWidth += rowWidth > 0 ? hgap + d.width : d.width;
        rowHeight = Math.max(rowHeight, d.height);
      }
    }
    height += rowHeight + vgap;
    return new Dimension(targetWidth, height);
  }
}
