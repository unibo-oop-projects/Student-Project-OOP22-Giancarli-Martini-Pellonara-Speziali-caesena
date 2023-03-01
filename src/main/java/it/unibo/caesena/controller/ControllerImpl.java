package it.unibo.caesena.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.unibo.caesena.model.*;
import it.unibo.caesena.model.meeple.*;
import it.unibo.caesena.model.tile.*;
import it.unibo.caesena.model.gameset.*;
import it.unibo.caesena.utils.*;

public class ControllerImpl implements Controller {
    private static final String FILE_TILES_PATH = "it/unibo/caesena/tile.conf";
    private final List<Meeple> meeples = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
    private final List<Tile> tiles = new ArrayList<>();
    private Tile currentTile;
    private Player currentPlayer;

    @Override
    public void startGame() {
        //currentPlayer = players.get(0);
        try {
            buildAllTiles();
        } catch (Exception e) {}
    }

    private void buildAllTiles() throws IOException {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(ClassLoader.getSystemResource(FILE_TILES_PATH).toURI()));
            for (String line : lines) {
                String imageName = line.split(";")[0];
                int cardinality = Integer.parseInt(line.split(";")[1]);
                for (int i = 0; i < cardinality; i++) {
                    tiles.add(makeTileFromImagePath(imageName));
                }
            }
        } catch (Exception e) { }
    }

    private Tile makeTileFromImagePath(String imageName) {
        TileFactory tileFactory = new TileFactoryWithBuilder();
        try {
            Method method = TileFactory.class.getMethod(getMethodNameFromString(imageName));
            return (Tile)method.invoke(tileFactory);
        } catch (Exception e) {}
        return null;
    }

    private String getMethodNameFromString(String string) {
        String methodName = "create";
        char[] charArray = string.toCharArray();
        boolean nextIsUpperCase = true;
        for (char c : charArray) {
            String currentCharAString = String.valueOf(c);
            if (nextIsUpperCase) {
                currentCharAString = currentCharAString.toUpperCase();
                nextIsUpperCase = false;
            }
            if (c == '-') {
                nextIsUpperCase = true;
            }
            else {
                methodName += currentCharAString;
            }
        }
        return methodName;
    }

    @Override
    public void addPlayer(String name, Color color) {
        players.add(new PlayerImpl(name, color));
    }

    @Override
    public Tile getCurrentTile() {
        drawNewTile();
        return currentTile;
    }

    private void drawNewTile() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rotateCurrentTile'");
    }

    @Override
    public void rotateCurrentTile() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rotateCurrentTile'");
    }

    @Override
    public boolean placeCurrentTile() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'placeCurrentTile'");
    }

    @Override
    public List<Player> getPlayers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlayers'");
    }

    @Override
    public List<Tile> getPlacedTiles() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlacedTiles'");
    }

    @Override
    public List<Meeple> getMeeples() {
        return new ArrayList<>(meeples);
    }

    @Override
    public boolean isGameOver() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isGameOver'");
    }

    @Override
    public void endTurn() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'endTurn'");
    }

    @Override
    public void endGame() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'endGame'");
    }

    @Override
    public void exitGame() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'exitGame'");
    }

    @Override
    public void saveGame() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveGame'");
    }

    @Override
    public Player getCurrentPlayer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCurrentPlayer'");
    }

    @Override
    public boolean placeMeeple(Meeple meeple) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'placeMeeple'");
    }

}
