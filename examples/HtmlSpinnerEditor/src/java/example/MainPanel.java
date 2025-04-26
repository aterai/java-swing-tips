// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] items = {
        "<html><font color='red'>Sunday</font> <font color='gray'>(Sun.)",
        "<html><font color='black'>Monday</font> <font color='gray'>(Mon.)",
        "<html><font color='black'>Tuesday</font> <font color='gray'>(Tue.)",
        "<html><font color='black'>Wednesday</font> <font color='gray'>(Wed.)",
        "<html><font color='black'>Thursday</font> <font color='gray'>(Thu.)",
        "<html><font color='black'>Friday</font> <font color='gray'>(Fri.)",
        "<html><font color='blue'>Saturday</font> <font color='gray'>(Sat.)"};

    JPanel p1 = new JPanel(new BorderLayout(5, 5));
    p1.add(new JSpinner(new SpinnerListModel(items)));
    p1.setBorder(BorderFactory.createTitledBorder("ListEditor(default)"));

    JPanel p2 = new JPanel(new BorderLayout(5, 5));
    JSpinner spinner = new JSpinner(new SpinnerListModel(items)) {
      @Override public void setEditor(JComponent editor) {
        JComponent oldEditor = getEditor();
        if (!editor.equals(oldEditor) && oldEditor instanceof HtmlListEditor) {
          ((HtmlListEditor) oldEditor).dismiss(this);
        }
        super.setEditor(editor);
      }
    };
    spinner.setEditor(new HtmlListEditor(spinner));
    p2.add(spinner);
    p2.setBorder(BorderFactory.createTitledBorder("HtmlListEditor"));

    JPanel panel = new JPanel(new BorderLayout(25, 25));
    panel.add(p1, BorderLayout.NORTH);
    panel.add(p2, BorderLayout.SOUTH);
    panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
    add(panel, BorderLayout.NORTH);
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

class HtmlListEditor extends JLabel implements ChangeListener {
  protected HtmlListEditor(JSpinner spinner) {
    super(Objects.toString(spinner.getValue()));
    if (!(spinner.getModel() instanceof SpinnerListModel)) {
      throw new IllegalArgumentException("model not a SpinnerListModel");
    }
    spinner.addChangeListener(this);
  }

  @Override public void updateUI() {
    super.updateUI();
    setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    setOpaque(true);
    setBackground(Color.WHITE);
    setInheritsPopupMenu(true);
  }

  @Override public void stateChanged(ChangeEvent e) {
    JSpinner spinner = (JSpinner) e.getSource();
    String txt = Objects.toString(spinner.getValue());
    setText(txt);
    setToolTipText(txt);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.width = 200;
    return d;
  }

  // @see javax/swing/JSpinner.DefaultEditor.html#dismiss(JSpinner)
  public void dismiss(JSpinner spinner) {
    spinner.removeChangeListener(this);
  }
}
