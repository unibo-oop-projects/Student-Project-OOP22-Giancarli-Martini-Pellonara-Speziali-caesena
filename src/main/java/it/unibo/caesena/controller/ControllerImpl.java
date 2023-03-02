package it.unibo.caesena.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unibo.caesena.model.*;
import it.unibo.caesena.model.meeple.*;
import it.unibo.caesena.model.tile.*;
import it.unibo.caesena.model.gameset.*;
import it.unibo.caesena.utils.*;

public class ControllerImpl implements Controller {
    private static final String FILE_TILES_PATH = "it/unibo/caesena/tile.conf";
    private static final int MEEPLES_PER_PLAYER = 8; //TODO guardare le regole del gioco
    private final List<Meeple> meeples = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
    private final List<Tile> tiles = new ArrayList<>();
    private Tile currentTile;
    private Player currentPlayer;
    private int turn;

    @Override
    public void startGame() throws IllegalStateException {
        if (players.isEmpty()) {
            //TODO sti controlli son da fare per tutti i metodi
            throw new IllegalStateException("Can't start the game without players");
        }
        Collections.shuffle(players);
        currentPlayer = players.get(0);
        buildAllTiles();
    }

    private void buildAllTiles() {
        List<String> lines;
        try {
            URI uri = ClassLoader.getSystemResource(FILE_TILES_PATH).toURI();
            lines = Files.readAllLines(Paths.get(uri));
            for (String line : lines) {
                String imageName = line.split(";")[0];
                int cardinality = Integer.parseInt(line.split(";")[1]);
                for (int i = 0; i < cardinality; i++) {
                    tiles.add(makeTileFromImagePath(imageName));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error reading tiles from file, maybe it's missing");
        }
    }

    private Tile makeTileFromImagePath(String imageName) {
        TileFactory tileFactory = new TileFactoryWithBuilder();
        try {
            String methodName = getMethodNameFromString(imageName);
            Method method = TileFactory.class.getMethod(methodName);
            return (Tile)method.invoke(tileFactory);
        } catch (Exception e) {
            throw new IllegalStateException("Error using reflection, devs fault");
        }
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
        Player newPlayer = new PlayerImpl(name, color);
        players.add(newPlayer);
        for (int i = 0; i < MEEPLES_PER_PLAYER; i++) {
            meeples.add(new NormalMeeple(newPlayer));
        }
    }

    @Override
    public Tile getCurrentTile() {
        return currentTile;
    }

    @Override
    public void rotateCurrentTile() {
        currentTile.rotateClockwise();
    }

    @Override
    public boolean placeCurrentTile() {
        // TODO JANKA
        throw new UnsupportedOperationException("Unimplemented method 'placeCurrentTile'");
    }

    @Override
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    @Override
    public List<Tile> getPlacedTiles() {
        return tiles.stream()
            .filter(x -> x.isPlaced())
            .toList();
    }

    @Override
    public List<Tile> getNotPlacedTiles() {
        return tiles.stream()
            .filter(x -> !x.isPlaced())
            .toList();
    }

    @Override
    public List<Meeple> getCurrentPlayerMeeples() {
        return meeples.stream().filter(m -> m.getOwner().equals(currentPlayer)).toList();
    }

    @Override
    public boolean isGameOver() {
        return getPlacedTiles().size() == tiles.size();
    }

    @Override
    public void endTurn() {
        // TODO ALE
        throw new UnsupportedOperationException("Unimplemented method 'endGame'");
    }

    @Override
    public void endGame() {
        // TODO ALE
        throw new UnsupportedOperationException("Unimplemented method 'endGame'");
    }

    @Override
    public void exitGame() {
        // TODO ALE
        throw new UnsupportedOperationException("Unimplemented method 'exitGame'");
    }

    @Override
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public boolean placeMeeple(Meeple meeple, TileSection section) {
        return meeple.isPlaced() ? false : meeple.place(section, currentTile);
    }

}
