package it.unibo.caesena.view.components;

import java.awt.Color;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.unibo.caesena.utils.Pair;
import it.unibo.caesena.view.LocaleHelper;

public class PlayerInputImpl extends JPanel implements PlayerInput<JPanel> {

    private final static int TEXT_FIELD_COLUMNS = 4;

    private final PlayerImageImpl playerColorPanel;
    private final JColorChooser playerColorChooser;
    private final JDialog playerColorDialog;
    private final JTextField playerName;

    private Color playerColor;

    public PlayerInputImpl() {
        super();

        final JLabel nameLabel = new JLabel(LocaleHelper.getNameText());

        this.playerName = new JTextField();
        this.playerName.setColumns(TEXT_FIELD_COLUMNS);

        final JLabel colorLabel = new JLabel(LocaleHelper.getColorText());

        this.playerColorPanel = new PlayerImageImpl();
        this.playerColorPanel.setColor(getBackground());

        this.playerColorChooser = new JColorChooser();
        this.playerColorChooser.setPreviewPanel(new JPanel());
        this.playerColorChooser.setLocale(Locale.ITALIAN);
        for (final var chooserPanel : playerColorChooser.getChooserPanels()) {
            if (!chooserPanel.getDisplayName().equals(LocaleHelper.getSwatchesColorPanelName())
                    && !"RGB".equals(chooserPanel.getDisplayName())) {
                this.playerColorChooser.removeChooserPanel(chooserPanel);
            }
        }

        this.playerColorDialog = JColorChooser.createDialog(this, LocaleHelper.getPickColorDialogTitle(), true,
                this.playerColorChooser,
                (e) -> updateColor(this.playerColorChooser.getColor()),
                (e) -> updateColor(getBackground()));

        final JButton playerColorButton = new JButton(LocaleHelper.getPickColorText());
        playerColorButton.addActionListener((e) -> this.playerColorDialog.setVisible(true));

        this.add(nameLabel);
        this.add(playerName);
        this.add(colorLabel);
        this.add(playerColorPanel);
        this.add(playerColorButton);
    }

    @Override
    public void setColorPanelSize(final int size) {
        this.playerColorPanel.forceSize(size);
    }

    private void updateColor(final Color color) {
        this.playerColorPanel.setColor(color);
        this.playerColor = color;
    }

    @Override
    public final Pair<String, Color> getPlayerData() {
        return new Pair<>(playerName.getText(), playerColor);
    }

    @Override
    public final JPanel getComponent() {
        return this;
    }

}
