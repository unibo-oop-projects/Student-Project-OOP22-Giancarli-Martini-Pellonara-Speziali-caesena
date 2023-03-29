package it.unibo.caesena.view.components;

public interface FooterComponent<X> {
    void updateCurrentTile();
    
    X getComponent();

    void updateFooter();

    TileImage getCurrentTileImage();

    public void updateRemainingTiles();
}
