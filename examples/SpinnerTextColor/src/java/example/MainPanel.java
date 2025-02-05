// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(0, 1));
    String[] items = {
        "<html><font color='red'>Sunday</font> <font color='gray'>(Sun.)",
        "<html><font color='black'>Monday</font> <font color='gray'>(Mon.)",
        "<html><font color='black'>Tuesday</font> <font color='gray'>(Tue.)",
        "<html><font color='black'>Wednesday</font> <font color='gray'>(Wed.)",
        "<html><font color='black'>Thursday</font> <font color='gray'>(Thu.)",
        "<html><font color='black'>Friday</font> <font color='gray'>(Fri.)",
        "<html><font color='blue'>Saturday</font> <font color='gray'>(Sat.)"};

    // // TEST:
    // JSpinner spinner = new JSpinner(new SpinnerListModel(items)) {
    //   @Override public void setEditor(JComponent editor) {
    //     JComponent oldEditor = getEditor();
    //     if (!editor.equals(oldEditor) && oldEditor instanceof HTMLListEditor) {
    //       ((HTMLListEditor) oldEditor).dismiss(this);
    //     }
    //     super.setEditor(editor);
    //   }
    // };
    // spinner.setEditor(new HTMLListEditor(spinner));

    add(makeTitledPanel("JSpinner", new JSpinner(new SpinnerListModel(items))));
    add(makeTitledPanel("ColorSpinner(JComboBox)", makeColorSpinner(items)));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeColorSpinner(String... items) {
    UIManager.put("ComboBox.squareButton", Boolean.FALSE);
    JComboBox<String> comboBox = new JComboBox<String>(items) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new NoPopupComboBoxUI());
        setFocusable(false);
      }
    };
    JButton nb = createArrowButton(SwingConstants.NORTH);
    nb.addActionListener(e -> {
      e.setSource(comboBox);
      comboBox.getActionMap().get("selectPrevious2").actionPerformed(e);
    });
    JButton sb = createArrowButton(SwingConstants.SOUTH);
    sb.addActionListener(e -> {
      e.setSource(comboBox);
      comboBox.getActionMap().get("selectNext2").actionPerformed(e);
    });
    Box box = Box.createVerticalBox();
    box.add(nb);
    box.add(sb);

    JPanel p = new JPanel(new BorderLayout()) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = 20;
        return d;
      }
    };
    p.add(comboBox);
    p.add(box, BorderLayout.EAST);
    p.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    return p;
  }

  private static JButton createArrowButton(int direction) {
    return new BasicArrowButton(direction) {
      @Override public void updateUI() {
        super.updateUI();
        Border buttonBorder = UIManager.getBorder("Spinner.arrowButtonBorder");
        if (buttonBorder instanceof UIResource) {
          // Wrap the border to avoid having the UIResource be replaced by
          // the ButtonUI. This is the opposite of using BorderUIResource.
          setBorder(new CompoundBorder(buttonBorder, null));
        } else {
          setBorder(buttonBorder);
        }
        setInheritsPopupMenu(true);
      }
    };
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
    return p;
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

class NoPopupComboBoxUI extends BasicComboBoxUI {
  @Override protected JButton createArrowButton() {
    JButton button = new JButton(); // .createArrowButton();
    button.setBorder(BorderFactory.createEmptyBorder());
    button.setVisible(false);
    return button;
  }

  // @Override public void setPopupVisible(JComboBox c, boolean v) {
  //   System.out.println("setPopupVisible: " + v);
  //   if (v) {
  //     popup.show();
  //   } else {
  //     popup.hide();
  //   }
  // }

  @Override protected ComboPopup createPopup() {
    return new BasicComboPopup(comboBox) {
      @Override public void show() {
        // System.out.println("disable togglePopup");
        // super.show();
      }
    };
  }
}

// // TEST:
// class HTMLListEditor extends JLabel implements ChangeListener {
//   private final JSpinner spinner;
//
//   protected HTMLListEditor(JSpinner spinner) {
//     super();
//     if (!(spinner.getModel() instanceof SpinnerListModel)) {
//       throw new IllegalArgumentException("model not a SpinnerListModel");
//     }
//     this.spinner = spinner;
//     spinner.addChangeListener(this);
//
//     setText(Objects.toString(spinner.getValue()));
//     setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
//     setOpaque(true);
//     setBackground(Color.WHITE);
//     setInheritsPopupMenu(true);
//
//     String toolTipText = spinner.getToolTipText();
//     if (toolTipText != null) {
//       setToolTipText(toolTipText);
//     }
//   }
//
//   @Override public Dimension getPreferredSize() {
//     Dimension d = super.getPreferredSize();
//     d.width = 200;
//     return d;
//   }
//
//   @Override public void stateChanged(ChangeEvent e) {
//     JSpinner spinner = (JSpinner) e.getSource();
//     setText(Objects.toString(spinner.getValue()));
//   }
//
//   public void dismiss(JSpinner spinner) {
//     spinner.removeChangeListener(this);
//   }
// }
