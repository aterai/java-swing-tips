// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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
  private final JPanel tagContainer = new JPanel(new FlowLayout(FlowLayout.LEFT)) {
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
    JScrollPane scroll = new JScrollPane(tagContainer) {
      @Override public void updateUI() {
        super.updateUI();
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        setBorder(BorderFactory.createEmptyBorder());
        setViewportBorder(BorderFactory.createEmptyBorder());
      }
    };
    add(scroll);
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
    return tags;
  }

  public Container getTagContainer() {
    return tagContainer;
  }

  private void addTag(String text) {
    JPanel tag = new JPanel(new BorderLayout(5, 0));
    tag.setName(text);
    tag.setBackground(new Color(230, 245, 255));
    // tag.setBackground(UIManager.getColor("Table.dropLineColor"));
    tag.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UIManager.getColor("Table.selectionBackground")),
        BorderFactory.createEmptyBorder(3, 5, 3, 5)
    ));
    tags.add(text);
    tag.add(new JLabel(text));
    tag.add(makeCloseButton(tag), BorderLayout.EAST);
    tagContainer.add(tag, tagContainer.getComponentCount() - 1);
    resizeAndRepaint();
  }

  private JButton makeCloseButton(JPanel tag) {
    JButton closeBtn = new JButton("×") {
      @Override public void updateUI() {
        super.updateUI();
        setContentAreaFilled(false);
        setFocusPainted(false);
        setFocusable(false);
        setBorder(BorderFactory.createEmptyBorder());
      }
    };
    closeBtn.addActionListener(e -> {
      tags.remove(tag.getName());
      tagContainer.remove(tag);
      resizeAndRepaint();
    });
    closeBtn.addMouseListener(new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        e.getComponent().setForeground(Color.RED);
      }

      @Override public void mouseExited(MouseEvent e) {
        e.getComponent().setForeground(UIManager.getColor("Button.foreground"));
      }
    });
    return closeBtn;
  }

  private void removeLastTag() {
    int count = tagContainer.getComponentCount();
    boolean moreThanOne = count > 1;
    if (moreThanOne) {
      tags.remove(tags.size() - 1); // Java 21: .removeLast();
      tagContainer.remove(count - 2);
      resizeAndRepaint();
    }
  }

  private void resizeAndRepaint() {
    revalidate();
    repaint();
  }
}
