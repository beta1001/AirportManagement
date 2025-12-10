
package GUI.src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ControlPanel extends JPanel {
    private final JComboBox<String> versionBox;
    private final JSpinner runwaysSpinner;
    private final JSpinner gatesSpinner;

    public ControlPanel( TriConsumer<String,Integer,Integer> startCallback,
                         BiConsumer<Boolean,ActionEvent> addPlaneCallback,
                         Consumer<ActionEvent> pauseCallback,
                         Consumer<ActionEvent> resetCallback) {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        versionBox = new JComboBox<>(new String[]{"Semaphore", "Monitor"});
        add(new JLabel("Version:"));
        add(versionBox);

        runwaysSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 5, 1));
        gatesSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        add(new JLabel("Runways:")); add(runwaysSpinner);
        add(new JLabel("Gates:")); add(gatesSpinner);

        JButton startBtn = new JButton("Start");
        JButton addArrival = new JButton("Add Arrival");
        JButton addDeparture = new JButton("Add Departure");
        JButton pauseBtn = new JButton("Pause/Resume");
        JButton resetBtn = new JButton("Reset");

        add(startBtn); add(addArrival); add(addDeparture); add(pauseBtn); add(resetBtn);

        startBtn.addActionListener(e ->
            startCallback.accept(
                (String) versionBox.getSelectedItem(),
                (Integer) runwaysSpinner.getValue(),
                (Integer) gatesSpinner.getValue()
            )
        );
        addArrival.addActionListener(e -> addPlaneCallback.accept(true, e));
        addDeparture.addActionListener(e -> addPlaneCallback.accept(false, e));
        pauseBtn.addActionListener(pauseCallback::accept);
        resetBtn.addActionListener(resetCallback::accept);
    }

    // small functional interface used for start callback
    @FunctionalInterface
    public interface TriConsumer<A,B,C> {
        void accept(A a, B b, C c);
       }
}