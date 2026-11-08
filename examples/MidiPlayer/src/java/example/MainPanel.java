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
  private boolean isSyncingSlider;

  private MainPanel() {
    super(new BorderLayout(5, 5));
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/Mozart_toruko_k.mid");
    boolean loaded = initSequencer(url);

    Timer timer = new Timer(50, e -> updateSlider());
    if (loaded) {
      timer.start();
    }

    addHierarchyListener(e -> {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable()) {
        timer.stop();
        if (sequencer != null) {
          sequencer.close();
        }
      }
    });

    slider.setEnabled(loaded);
    slider.addChangeListener(e -> {
      // Seek on any user operation (drag, track click, or key input), but not on Timer updates
      if (!isSyncingSlider && !slider.getValueIsAdjusting()) {
        sequencer.setTickPosition(slider.getValue());
      }
    });

    JButton playButton = new JButton("Play");
    playButton.setEnabled(loaded);
    playButton.addActionListener(e -> play());

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
    box.add(slider);
    box.add(Box.createHorizontalStrut(2));
    box.add(playButton);

    add(createTitleArea(getBackground()));
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private boolean initSequencer(URL url) {
    boolean loaded = false;
    try {
      sequencer = MidiSystem.getSequencer();
      if (url != null) {
        sequencer.open();
        sequencer.setSequence(MidiSystem.getSequence(url));
        slider.setMaximum((int) sequencer.getTickLength());
        loaded = true;
      }
    } catch (IOException | MidiUnavailableException | InvalidMidiDataException ex) {
      Logger.getGlobal().severe(ex::getMessage);
    }
    return loaded;
  }

  // Keep updating after the playback stops so that the slider reaches the end of the track
  @SuppressWarnings("PMD.UnusedAssignment")
  private void updateSlider() {
    if (!slider.getValueIsAdjusting()) {
      isSyncingSlider = true;
      slider.setValue((int) sequencer.getTickPosition());
      isSyncingSlider = false;
    }
  }

  private void play() {
    if (!sequencer.isRunning()) {
      // Sequencer#start() stops immediately if the position is at the end of the track
      if (sequencer.getTickPosition() >= sequencer.getTickLength()) {
        sequencer.setTickPosition(0L);
      }
      sequencer.start();
    }
  }

  private static Component createTitleArea(Color bgc) {
    String txt = String.join(
        "\n",
        "Wolfgang Amadeus Mozart",
        "Piano Sonata No. 11 in A major, K 331",
        "(Turkish Rondo)");
    JTextArea textArea = new JTextArea(txt);
    textArea.setBorder(BorderFactory.createTitledBorder("MIDI"));
    textArea.setEditable(false);
    textArea.setBackground(bgc);
    return textArea;
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
