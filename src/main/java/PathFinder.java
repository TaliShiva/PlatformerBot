import javafx.util.Pair;
import model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PathFinder {
    private static final float EPS = 1e-6f;
    //private List<Vertex> visitedVertices = new ArrayList<>();
    //private List<Vertex> noVisitedVertices = new ArrayList<>();
    Tile[][] tiles;
    Graph graph;

    //конструктор в котором создаётся один раз сетка точек для поиска пути
    PathFinder(Game game, Debug debug) throws IOException {
        tiles = game.getLevel().getTiles();
        graph = new Graph(game, debug);
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


    class Graph {
        List<Vertex> moveGraph = new ArrayList<>();

        // точка нужна, чтобы по ней получить в итоге путь до стартовой точки
        private class Vertex {
            //первый элемент

            private Tile startTile;
            private Vec2Float position;

            Vertex(Tile startTile, Vec2Float position) {
                this.startTile = startTile;
                this.position = position;
            }

            public Vec2Float getPosition() {
                return position;
            }

            //private List<Vertex> parents = new ArrayList<>();

            private ArrayList<Pair<Vertex, Float>> neighbours = new ArrayList<>(); // соседние вершины хранятся в виде пары, конкретной вершины и стоимости туда передвижения

            public void setNeighbour(Vertex neighbour, Float weight) {
                if (neighbour != null)
                    this.neighbours.add(new Pair<>(neighbour, weight));
            }

            public ArrayList<Pair<Vertex, Float>> getNeighbours() {
                return neighbours;
            }

//            public void setParent(Vertex parent) {
//                parents.add(parent);
//            }
        }

        /**
         * Создание графа доступных для движений точек, с учётом коллизий от стен
         */
        Graph(Game game, Debug debug) throws IOException {
            //double unitJumpLength = game.getProperties().getUnitJumpSpeed() * game.getProperties().getUnitJumpTime();
            CreateAllVertex();
            for (int i = 0; i < moveGraph.size(); i++) {
                Vertex vertex = moveGraph.get(i);
                for (int j = 0; j < moveGraph.size(); j++) {
                    Vertex nextVertex = moveGraph.get(j);
                    if (Math.abs(vertex.getPosition().getX() - nextVertex.getPosition().getX()) <= 1 && Math.abs(vertex.getPosition().getY() - nextVertex.getPosition().getY()) <= EPS) {
                        vertex.setNeighbour(nextVertex, 1f);// добавление горизонтальных соседей
                    }
                    if (Math.abs(vertex.getPosition().getY() - nextVertex.getPosition().getY()) <= 1 && Math.abs(vertex.getPosition().getX() - nextVertex.getPosition().getX()) <= EPS) {
                        vertex.setNeighbour(nextVertex, 1f); // добавление вертикальных ближайших соседей
                    }
                    if(Math.abs(vertex.getPosition().getX() - nextVertex.getPosition().getX()) <= 1){
                        if(vertex.getPosition().getY() < nextVertex.getPosition().getY()){
                            //TODO: реализовать проверку на пустые тайлы между позициями
                        }
                    }
                }
            }

            for (Vertex v : moveGraph) {
                debug.draw(new CustomData.Rect(v.getPosition(), new Vec2Float(0.5f, 0.5f), new ColorFloat(100, 100, 0, 100)));
                for (Pair<Vertex, Float> nv : v.getNeighbours()) {
                    debug.draw(new CustomData.Line(v.position, nv.getKey().position, 0.2f, new ColorFloat(100, 0, 100, 100)));
                }
            }
        }


        private void CreateAllVertex() {
            // если мы будет получать оставшуюся длиину прыжка, то можно сделать так, у вершины есть движение наверх, пока длина больше либо равна 1
            for (int i = 1; i < tiles.length - 1; i++) {
                for (int j = 1; j < tiles[i].length - 1; j++) {
                    if (tiles[i][j] == Tile.WALL && tiles[i - 1][j] == Tile.WALL && tiles[i][j - 1] == Tile.WALL) { // правый верхний угол
                        moveGraph.add(new Vertex(tiles[i + 1][j + 1], new Vec2Float(i + 1, j + 1)));
                        moveGraph.add(new Vertex(tiles[i][j + 1], new Vec2Float(i, j + 1)));
                    } else if (tiles[i][j] == Tile.WALL && tiles[i + 1][j] == Tile.WALL && tiles[i][j - 1] == Tile.WALL) { // левый верхний угол
                        moveGraph.add(new Vertex(tiles[i - 1][j + 1], new Vec2Float(i - 1, j + 1)));
                        moveGraph.add(new Vertex(tiles[i][j + 1], new Vec2Float(i, j + 1)));
                    } else if (tiles[i][j] == Tile.WALL && tiles[i][j + 1] == Tile.WALL && tiles[i - 1][j] == Tile.WALL) { // внутренний правый угол
                        moveGraph.add(new Vertex(tiles[i - 1][j + 1], new Vec2Float(i - 1, j + 1)));
                    } else if (tiles[i][j] == Tile.WALL && tiles[i][j + 1] == Tile.WALL && tiles[i + 1][j] == Tile.WALL) { // внутренний левый угол
                        moveGraph.add(new Vertex(tiles[i + 1][j + 1], new Vec2Float(i + 1, j + 1)));
                    } else if (tiles[i][j] == Tile.PLATFORM) {                                                              // вершина над платформой
                        moveGraph.add(new Vertex(tiles[i][j + 1], new Vec2Float(i, j + 1)));
                    } else if (tiles[i][j] == Tile.LADDER) {                                                                // вершина лестницы
                        moveGraph.add(new Vertex(tiles[i][j], new Vec2Float(i, j)));
                        if (tiles[i][j + 1] == Tile.EMPTY) {                                                                // над лестницей пусто, то это тоже вершина
                            moveGraph.add(new Vertex(tiles[i][j + 1], new Vec2Float(i, j + 1)));
                        }
                    } else if (tiles[i][j] == Tile.JUMP_PAD) {                                                              // вершина джампада
                        moveGraph.add(new Vertex(tiles[i][j], new Vec2Float(i, j)));
                    } else if (tiles[i][j] == Tile.WALL && tiles[i][j + 1] == Tile.EMPTY) {                                 // вершина над стенкой
                        moveGraph.add(new Vertex(tiles[i][j + 1], new Vec2Float(i, j + 1)));
                    }
                }
            }
        }
    }

}


