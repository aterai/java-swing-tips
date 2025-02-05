// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea("JFrame Conditional Close Test");
    JButton exitButton = new JButton(SaveHandler.CMD_EXIT);
    JButton saveButton = new JButton(SaveHandler.CMD_SAVE);

    EventQueue.invokeLater(() -> {
      Component c = getTopLevelAncestor();
      if (c instanceof Frame) {
        Frame frame = (Frame) c;
        SaveHandler handler = new SaveHandler(frame);
        handler.addEnabledFlagComponent(saveButton);
        frame.addWindowListener(handler);
        textArea.getDocument().addDocumentListener(handler);
        exitButton.addActionListener(handler);
        saveButton.addActionListener(handler);
      }
    });
    exitButton.setActionCommand(SaveHandler.CMD_EXIT);
    saveButton.setActionCommand(SaveHandler.CMD_SAVE);
    saveButton.setEnabled(false);

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(exitButton);
    box.add(Box.createHorizontalStrut(5));
    box.add(saveButton);

    add(box, BorderLayout.SOUTH);
    add(new JScrollPane(textArea));
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
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    // frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class SaveHandler extends WindowAdapter implements DocumentListener, ActionListener {
  public static final String LOGGER_NAME = MethodHandles.lookup().lookupClass().getName();
  public static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);
  // public static final String ASTERISK_TITLE_BAR = "unsaved";
  public static final String CMD_SAVE = "save";
  public static final String CMD_EXIT = "exit";
  private final Frame frame;
  private final String title;
  private final List<JComponent> list = new ArrayList<>();

  protected SaveHandler(Frame frame) {
    super();
    this.frame = frame;
    this.title = frame.getTitle();
  }

  // WindowAdapter
  @Override public void windowClosing(WindowEvent e) {
    LOGGER.info(() -> "windowClosing");
    maybeExit();
  }

  // @SuppressWarnings("PMD.DoNotCallSystemExit")
  // @Override public void windowClosed(WindowEvent e) {
  //   MainPanel.LOGGER.info(() -> "windowClosed");
  //   System.exit(0); // webStart
  // }

  // ActionListener
  @Override public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (CMD_EXIT.equals(cmd)) {
      maybeExit();
    } else if (CMD_SAVE.equals(cmd)) {
      fireUnsavedFlagChangeEvent(false);
    }
  }

  // DocumentListener
  @Override public void insertUpdate(DocumentEvent e) {
    fireUnsavedFlagChangeEvent(true);
  }

  @Override public void removeUpdate(DocumentEvent e) {
    fireUnsavedFlagChangeEvent(true);
  }

  @Override public void changedUpdate(DocumentEvent e) {
    /* not needed */
  }

  private void maybeExit() {
    if (title.equals(frame.getTitle())) {
      LOGGER.info(() -> "The document has already been saved, exit without doing anything.");
      frame.dispose();
      return;
    }
    Toolkit.getDefaultToolkit().beep();
    Object[] options = {"Save", "Discard", "Cancel"};
    int retValue = JOptionPane.showOptionDialog(
        frame,
        "<html>Save: Exit & Save Changes<br>Discard: Exit & Discard Changes<br>Cancel: Continue",
        "Exit Options",
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.INFORMATION_MESSAGE,
        null,
        options,
        options[0]);
    if (retValue == JOptionPane.YES_OPTION) {
      LOGGER.info(() -> "exit");
      // boolean ret = sampleDocumentSaveMethod();
      // if (ret) { // saved and exit
      //   frame.dispose();
      // } else { // error and cancel exit
      //   return;
      // }
      frame.dispose();
    } else if (retValue == JOptionPane.NO_OPTION) {
      LOGGER.info(() -> "Exit without save");
      frame.dispose();
    } else if (retValue == JOptionPane.CANCEL_OPTION) {
      LOGGER.info(() -> "Cancel exit");
    }
  }

  public void addEnabledFlagComponent(JComponent c) {
    list.add(c);
  }

  // public void removeEnabledFlagComponent(JComponent c) {
  //   list.remove(c);
  // }

  private void fireUnsavedFlagChangeEvent(boolean unsaved) {
    frame.setTitle(String.format("%s%s", unsaved ? "* " : "", title));
    for (Component c : list) {
      c.setEnabled(unsaved);
    }
  }
}
