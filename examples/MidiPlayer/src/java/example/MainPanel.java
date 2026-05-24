// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JSlider slider = new JSlider(0, 100, 0);
  private Sequencer sequencer;
  private boolean isMovingSlider;

  private MainPanel() {
    super(new BorderLayout(5, 5));
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/Mozart_toruko_k.mid");
    initSequencer(url);

    Timer timer = new Timer(50, e -> {
      if (!isMovingSlider && sequencer.isRunning()) {
        slider.setValue((int) sequencer.getTickPosition());
      }
    });
    timer.start();

    addHierarchyListener(e -> {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable()) {
        if (sequencer != null) {
          sequencer.close();
        }
        timer.stop();
      }
    });

    slider.addChangeListener(e -> {
      if (slider.getValueIsAdjusting()) {
        isMovingSlider = true;
      } else if (isMovingSlider) {
        sequencer.setTickPosition(slider.getValue());
        isMovingSlider = false;
      }
    });

    JButton playButton = new JButton("Play");
    playButton.setEnabled(url != null);
    playButton.addActionListener(e -> {
      if (!sequencer.isRunning()) {
        sequencer.start();
      }
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
    box.add(slider);
    box.add(Box.createHorizontalStrut(2));
    box.add(playButton);

    add(createTitleBox(getBackground()));
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private void initSequencer(URL url) {
    try {
      sequencer = MidiSystem.getSequencer();
      if (url != null) {
        sequencer.open();
        sequencer.setSequence(MidiSystem.getSequence(url));
        slider.setMaximum((int) sequencer.getTickLength());
      }
    } catch (IOException | MidiUnavailableException | InvalidMidiDataException ex) {
      Logger.getGlobal().severe(ex::getMessage);
    }
  }

  private static Component createTitleBox(Color bgc) {
    String txt = String.join(
        "\n",
        "Wolfgang Amadeus Mozart",
        "Piano Sonata No. 11 in A major, K 331",
        "(Turkish Rondo)");
    JTextArea label = new JTextArea(txt);
    label.setBorder(BorderFactory.createTitledBorder("MIDI"));
    label.setEditable(false);
    label.setBackground(bgc);
    return label;
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
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
