package model;

import util.StreamUtil;


public class Level {
    private model.Tile[][] tiles;

    public model.Tile[][] getTiles() {
        return tiles;
    }

    public void setTiles(model.Tile[][] tiles) {
        this.tiles = tiles;
    }

    public Level() {
    }

    public Level(model.Tile[][] tiles) {
        this.tiles = tiles;
    }

    public static Level readFrom(java.io.InputStream stream) throws java.io.IOException {
        Level result = new Level();
        result.tiles = new model.Tile[StreamUtil.readInt(stream)][];
        for (int i = 0; i < result.tiles.length; i++) {
            result.tiles[i] = new model.Tile[StreamUtil.readInt(stream)];
            for (int j = 0; j < result.tiles[i].length; j++) {
                switch (StreamUtil.readInt(stream)) {
                    case 0:
                        result.tiles[i][j] = model.Tile.EMPTY;
                        result.tiles[i][j].setMoveAction(MoveAction.state.LEFTMOVE);
                        result.tiles[i][j].setMoveAction(MoveAction.state.RIGHTMOVE);

                        break;
                    case 1:
                        result.tiles[i][j] = model.Tile.WALL;
                        break;
                    case 2:
                        result.tiles[i][j] = model.Tile.PLATFORM;
                        result.tiles[i][j + 1].setMoveAction(MoveAction.state.LEFTMOVE);
                        result.tiles[i][j + 1].setMoveAction(MoveAction.state.RIGHTMOVE);
                        result.tiles[i][j + 1].setMoveAction(MoveAction.state.JUMP);
                        break;
                    case 3:
                        result.tiles[i][j] = model.Tile.LADDER;
                        result.tiles[i][j + 1].setMoveAction(MoveAction.state.JUMPDOWN);
                        result.tiles[i][j + 1].setMoveAction(MoveAction.state.JUMP);
                        break;
                    case 4:
                        result.tiles[i][j] = model.Tile.JUMP_PAD;
                        result.tiles[i][j + 1].setMoveAction(MoveAction.state.JUMP);
                        break;
                    default:
                        throw new java.io.IOException("Unexpected discriminant value");
                }
            }
        }

     /*   if (result.tiles[i - 1][j] != null)
            result.tiles[i][j].setNeighbour(result.tiles[i - 1][j]);
        if (result.tiles[i + 1][j] != null)
            result.tiles[i][j].setNeighbour(result.tiles[i + 1][j]);
    */

        return result;
    }

    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeInt(stream, tiles.length);
        for (model.Tile[] tilesElement : tiles) {
            StreamUtil.writeInt(stream, tilesElement.length);
            for (model.Tile tilesElementElement : tilesElement) {
                StreamUtil.writeInt(stream, tilesElementElement.discriminant);
            }
        }
    }
}
