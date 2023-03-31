package it.unibo.caesena.view.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import it.unibo.caesena.model.Player;
import it.unibo.caesena.view.GUI;
import it.unibo.caesena.view.LocaleHelper;

public class LeaderBoardComponentImpl extends JPanel implements LeaderBoardComponent<JPanel> {
    
    private static final int PLAYER_IMAGE_RATIO = 80;
    private final GUI userInterface;
    private final JPanel playersPanel;
    private final int playerImageSize;

    public LeaderBoardComponentImpl(final GUI userInterface) {
        super();

        this.userInterface = userInterface;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (screenSize.getHeight() > screenSize.getWidth()) {
            playerImageSize = (int) Math.round(screenSize.getWidth() / PLAYER_IMAGE_RATIO);
        } else {
            playerImageSize = (int) Math.round(screenSize.getHeight() / PLAYER_IMAGE_RATIO);
        }

        final JLabel titleLabel = new JLabel(LocaleHelper.getLeaderboardName());
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));

        this.add(titleLabel);
        final JSeparator separator = new JSeparator();
        separator.setForeground(Color.black);
        this.add(separator);

        this.playersPanel = new JPanel();
        this.playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        this.add(playersPanel);

        updateLeaderBoard();

        this.setVisible(true);
    }

    @Override
    public void updateLeaderBoard() {
        playersPanel.removeAll();
        playersPanel.revalidate();
        playersPanel.repaint();

        final List<Player> players = userInterface.getController().getPlayers().stream()
            .sorted((p1, p2) -> Integer.compare(p1.getScore(), p2.getScore())).toList();

        for (final var player : players) {
            final var playerPanel = new JPanel();
            
            final var playerColorPanel = new PlayerImageImpl();
            playerColorPanel.setColor(userInterface.getPlayerColor(player));
            playerColorPanel.forceSize(playerImageSize);

            final JLabel playerLabel = new JLabel(player.getName() + " " + player.getScore());
            playerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
            playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            playerPanel.add(playerColorPanel);
            playerPanel.add(playerLabel);
            playersPanel.add(playerPanel);
        }
    }

    @Override
    public JPanel getComponent() {
        return this;
    }
}