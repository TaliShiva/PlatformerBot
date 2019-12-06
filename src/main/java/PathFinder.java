import model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PathFinder {
    private List<Vertex> visitedVertices = new ArrayList<>();
    private List<Vertex> noVisitedVertices = new ArrayList<>();
    Tile[][] tiles;
    Graph graph;

    //конструктор в котором создаётся один раз сетка точек для поиска пути
    PathFinder(Level lvl) throws IOException {
        tiles = lvl.getTiles();
//        graph = new Graph();
    }


//    public Tile[] getPath(Vec2Double startPosition, Vec2Double finishPosition) {
//        final Tile startTile = tiles[(int) startPosition.getX()][(int) startPosition.getY()];
//        Point startPoint = new Point(startTile);
//
//        visitedPoints.add(startPoint); // поместили начальный тайл
////        while (!noVisitedPoints.isEmpty()) {
////            for (Tile neighbour : startTile.getNeighbours()) {
////
////            }
////        }
//        return tiles;
//    }

    // точка нужна, чтобы по ней получить в итоге путь до стартовой точки
    class Vertex {
        //первый элемент

        private Tile tile;

        Vertex(Tile startTile) {
            tile = startTile;
        }


    }


    class Graph {
        List<List<Vertex>> moveGraph = new ArrayList<>();


        /**
         * Создание графа доступных для движений точек, с учётом коллизий от стен
         */
        Graph(Game game, Unit unit) throws IOException {
            double unitJumpLength = game.getProperties().getUnitJumpSpeed() * game.getProperties().getUnitJumpTime();
            // если мы будет получать оставшуюся длиину прыжка, то можно сделать так, у вершины есть движение наверх, пока длина больше либо равна 1
            for (int i = 1; i < tiles.length - 1; i++) {
                for (int j = 1; j < tiles[i].length - 1; j++) {
                    switch (tiles[i][j]) {
                        case EMPTY:
                            if (tiles[i][j - 1] != Tile.WALL) {
                                tiles[i][j].setNeighbour(tiles[i][j - 1]);
                            }
                            if (tiles[i + 1][j - 1] != Tile.WALL) {
                                tiles[i][j].setNeighbour(tiles[i + 1][j - 1]);
                            }
                            if (tiles[i - 1][j - 1] != Tile.WALL) {
                                tiles[i][j].setNeighbour(tiles[i - 1][j - 1]);
                            }
                            if (unitJumpLength >= 1) {
                                if (tiles[i][j + 1] != Tile.WALL) {
                                    tiles[i][j].setNeighbour(tiles[i][j + 1]);
                                }
                                if (tiles[i + 1][j + 1] != Tile.WALL) {
                                    tiles[i][j].setNeighbour(tiles[i + 1][j + 1]);
                                }
                                if (tiles[i - 1][j + 1] != Tile.WALL) {
                                    tiles[i][j].setNeighbour(tiles[i - 1][j + 1]);
                                }
                            }
                            break;
                        case WALL:
                            break;
                        case LADDER:
                            if (tiles[i][j + 1] != Tile.WALL) {
                                tiles[i][j].setNeighbour(tiles[i][j + 1]);
                            }
                            if (tiles[i + 1][j + 1] != Tile.WALL) {
                                tiles[i][j].setNeighbour(tiles[i + 1][j + 1]);
                            }
                            if (tiles[i + 1][j] != Tile.WALL) {
                                tiles[i][j].setNeighbour(tiles[i + 1][j]);
                            }
                            if (tiles[i + 1][j - 1] != Tile.WALL) {
                                tiles[i][j].setNeighbour(tiles[i + 1][j - 1]);
                            }
                            if (tiles[i][j - 1] != Tile.WALL) {
                                tiles[i][j].setNeighbour(tiles[i][j - 1]);
                            }
                            if (tiles[i - 1][j - 1] != Tile.WALL) {
                                tiles[i][j].setNeighbour(tiles[i - 1][j - 1]);
                            }
                            if (tiles[i - 1][j] != Tile.WALL) {
                                tiles[i][j].setNeighbour(tiles[i - 1][j]);
                            }
                            if (tiles[i - 1][j + 1] != Tile.WALL) {
                                tiles[i][j].setNeighbour(tiles[i - 1][j + 1]);
                            }
                            break;
                        case PLATFORM:
                            tiles[i][j + 1].setNeighbour(tiles[i + 1][j + 1]);
                            tiles[i][j + 1].setNeighbour(tiles[i - 1][j + 1]);
                            tiles[i][j + 1].setNeighbour(tiles[i][j + 2]);
                            break;

                        case JUMP_PAD:
                            break;
                        default:
                            throw new java.io.IOException("Unexpected discriminant value");
                    }
                }
            }
        }
    }

}


