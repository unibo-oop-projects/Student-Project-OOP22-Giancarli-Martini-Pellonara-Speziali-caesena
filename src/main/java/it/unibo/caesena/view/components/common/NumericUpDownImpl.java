package it.unibo.caesena.view.components.common;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.DefaultEditor;

import it.unibo.caesena.view.GUI;

/**
 * A class implementing the NumericUpDown interface by using a
 * {@link javax.swing.JSpinner}.
 */
public class NumericUpDownImpl implements NumericUpDown<JSpinner> {
    private final SpinnerNumberModel model;
    private final JSpinner spinner;

    /**
     * Public constructor.
     *
     * @param start the value from where the NumericUpDown starts
     * @param min the minimum value for the NumericUpDown
     * @param max the maximum value for the NumericUpDown
     * @param step the amount to increase/decrease the value at every click
     */
    public NumericUpDownImpl(final int start, final int min, final int max, final int step) {
        this.model = new SpinnerNumberModel(start, min, max, step);
        this.spinner = new JSpinner(model);

        final DefaultEditor editor = new DefaultEditor(this.spinner);
        editor.getTextField().setEditable(false);
        editor.getTextField().setFont(GUI.MEDIUM_BOLD_FONT);
        this.spinner.setEditor(editor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getValueAsInt() {
        return this.model.getNumber().intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSpinner getComponent() {
        return this.spinner;
    }

}