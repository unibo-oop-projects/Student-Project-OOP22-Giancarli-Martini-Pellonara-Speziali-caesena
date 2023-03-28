package it.unibo.caesena.view;

import javax.swing.*;
import java.awt.*;

import it.unibo.caesena.utils.Direction;
import it.unibo.caesena.utils.Pair;
import it.unibo.caesena.view.components.FooterComponent;
import it.unibo.caesena.view.components.FooterComponentImpl;
import it.unibo.caesena.view.components.MainComponent;
import it.unibo.caesena.view.components.MainComponentImpl;
import it.unibo.caesena.view.components.SideBarComponent;
import it.unibo.caesena.view.components.SideBarComponentImpl;
import it.unibo.caesena.view.components.TileButton;

public class GameView extends JPanel implements View<JPanel> {

    private final GUI userInterface;
    private MainComponent<JPanel> mainComponent;
    private FooterComponent<JPanel> footer;
    private SideBarComponent<JPanel> sidebar;

    public GameView(GUI userInterface) {
        super();
        this.userInterface = userInterface;

        this.userInterface.getController().startGame();
        this.mainComponent = new MainComponentImpl(this);
        this.setLayout(new BorderLayout());
        this.footer = new FooterComponentImpl(this);
        this.sidebar = new SideBarComponentImpl(this);
        this.add(sidebar.getComponent(), BorderLayout.EAST);
        this.add(mainComponent.getComponent(), BorderLayout.CENTER);
        this.add(footer.getComponent(), BorderLayout.SOUTH);
    }

    public void updateHUD() {
        this.footer.updateFooter();
        this.sidebar.update();
    }

    public void placeMeeple() {
        mainComponent.toggleComponents();
        updateHUD();
    }

    public boolean placeTile() {
        TileButton<JButton> placeTileButton = mainComponent.getBoard().getPlacedTileButton();
        Pair<Integer, Integer> position = mainComponent.getBoard().getTileButtonPosition(placeTileButton);
        if (this.userInterface.getController().placeCurrentTile(position)) {
            updateHUD();
            return true;
        }
        return false;
    }

    public void endTurn() {
        this.mainComponent.endTurn();
        this.userInterface.getController().endTurn();
        if (this.userInterface.getController().isGameOver()) {
            userInterface.showGameOverView();
        } else {
            updateHUD();
        }
    }

    public void zoomIn() {
        this.mainComponent.getBoard().zoomIn();
    }

    public void zoomOut() {
        this.mainComponent.getBoard().zoomOut();
    }

    public void move(Direction direction) {
        this.mainComponent.getBoard().move(direction);
    }

    public boolean canMove(Direction direction) {
        return this.mainComponent.getBoard().canMove(direction);
    }

    public void removePlacedTile() {
        this.mainComponent.getBoard().removePlacedTile();
    }

    public void updateComponents() {
        this.mainComponent.getBoard().draw();
        this.mainComponent.getBoard().draw();
    }

    @Override
    public JPanel getComponent() {
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GUI getUserInterface() {
        return this.userInterface;
    }
}
