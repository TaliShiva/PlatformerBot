import javafx.util.Pair;
import model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PathFinder {
    private static double distanceSqr(Vec2Float a, Vec2Float b) {
        return (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }

    private static final float EPS = 1e-6f;
    //private List<Vertex> visitedVertices = new ArrayList<>();
    //private List<Vertex> noVisitedVertices = new ArrayList<>();
    private Tile[][] tiles;
    private Graph graph;

    //конструктор в котором создаётся один раз сетка точек для поиска пути
    PathFinder(Game game, Debug debug) throws IOException {
        tiles = game.getLevel().getTiles();
        graph = new Graph(game, debug);
    }


    public void getPath(Vec2Double startPosition, Vec2Double finishPosition, Debug debug) {
        //сперва притягиваем позицию к ближайшей вершине в графе
        Graph.Vertex startVer = relaxateFinPos(startPosition);
        debug.draw(new CustomData.Line(new Vec2Float((float) startPosition.getX(), (float) startPosition.getY()), startVer.getPosition(), 0.2f, new ColorFloat(0, 0, 0, 100)));

        Graph.Vertex finVer = relaxateFinPos(finishPosition);
        List<Graph.Vertex> path = new ArrayList<>();
        List<Graph.Vertex> nonCheckedVertecies = new ArrayList<>();
        startVer.setParent(null);
        startVer.isVisited = true;
        nonCheckedVertecies.add(startVer);
        for (int i = 0; i < nonCheckedVertecies.size(); i++) {
            Graph.Vertex currentVertex = nonCheckedVertecies.get(i);
            for (Pair<Graph.Vertex, Float> v : currentVertex.getNeighbours()) {
                if (v.getKey().isVisited) {
                    continue; // если бывали в вершине то к следующей
                }
                v.getKey().isVisited = true; //если не бывали ещё в ней, то отмечаем тут, что побывали
                v.getKey().setParent(currentVertex);
                if (v.getKey() == finVer) { // если нашли финальную вершину, то
                    Graph.Vertex tmpVer = v.getKey();
                    path.add(tmpVer);
                    while (tmpVer.getParent() != null) {
                        path.add(tmpVer.getParent());
                        debug.draw(new CustomData.Line(tmpVer.position, tmpVer.getParent().position, 0.2f, new ColorFloat(75, 75, 75, 100)));
                        tmpVer = tmpVer.getParent();
                    }
                    break; // путь получен
                }
                nonCheckedVertecies.add(v.getKey()); // обновляем список
            }
        }
    }

    private Graph.Vertex relaxateFinPos(Vec2Double finishPosition) {
        Vec2Float floatFinishPosition = new Vec2Float((float) finishPosition.getX(), (float) finishPosition.getY());
        Graph.Vertex ver = null;
        double distance = 1000;
        for (Graph.Vertex v : graph.vertices) {
            if (distanceSqr(floatFinishPosition, v.position) < distance) {
                distance = distanceSqr(floatFinishPosition, v.position);
                ver = v;
            }
        }
        return ver;
    }

    class Graph {
        List<Vertex> vertices = new ArrayList<>();

        // точка нужна, чтобы по ней получить в итоге путь до стартовой точки
        public class Vertex {
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

            private Vertex parent;

            private ArrayList<Pair<Vertex, Float>> neighbours = new ArrayList<>(); // соседние вершины хранятся в виде пары, конкретной вершины и стоимости туда передвижения

            public void setNeighbour(Vertex neighbour, Float weight) {
                if (neighbour != null)
                    this.neighbours.add(new Pair<>(neighbour, weight));
            }

            public ArrayList<Pair<Vertex, Float>> getNeighbours() {
                return neighbours;
            }

            public void setParent(Vertex parent) {
                this.parent = parent;
            }

            public Vertex getParent() {
                return parent;
            }

            public boolean isVisited = false;
        }

        /**
         * Создание графа доступных для движений точек, с учётом коллизий от стен
         */
        Graph(Game game, Debug debug) throws IOException {
            //double unitJumpLength = game.getProperties().getUnitJumpSpeed() * game.getProperties().getUnitJumpTime();
            CreateAllVertex();
            CreateAllEdges(game);
            debugDrawing(debug);
        }

        private void debugDrawing(Debug debug) {
            for (Vertex v : vertices) {
                debug.draw(new CustomData.Rect(v.getPosition(), new Vec2Float(0.5f, 0.5f), new ColorFloat(100, 100, 0, 100)));
                for (Pair<Vertex, Float> nv : v.getNeighbours()) {
                    debug.draw(new CustomData.Line(v.position, nv.getKey().position, 0.07f, new ColorFloat(100, 0, 100, 100)));
                }
            }
        }

        private void CreateAllEdges(Game game) {
            Section section = new Section(game);
            for (int i = 0; i < vertices.size(); i++) {
                Vertex vertex = vertices.get(i);
                for (int j = 0; j < vertices.size(); j++) {
                    Vertex nextVertex = vertices.get(j);
                    if (nextVertex == vertex) {
                        continue; // дабы избежать петель
                    }
                    if (Math.abs(vertex.getPosition().getX() - nextVertex.getPosition().getX()) <= 1 && Math.abs(vertex.getPosition().getY() - nextVertex.getPosition().getY()) <= EPS) {
                        vertex.setNeighbour(nextVertex, 1f);// добавление ближайших горизонтальных соседей
                    }
                    if (Math.abs(vertex.getPosition().getY() - nextVertex.getPosition().getY()) <= 1 && Math.abs(vertex.getPosition().getX() - nextVertex.getPosition().getX()) <= EPS) {
                        vertex.setNeighbour(nextVertex, 1f); // добавление вертикальных ближайших соседей
                    }
                    if (Math.abs(vertex.getPosition().getX() - nextVertex.getPosition().getX()) < 1) {
                        if (!section.checkСollisionBetweenTwoVerticalPositions(vertex.getPosition(), nextVertex.getPosition()).isHaveColllision()) { // если нет коллизий
                            vertex.setNeighbour(nextVertex, Math.abs(vertex.getPosition().getY() - nextVertex.getPosition().getY()));
                        }
                    }
                }
            }
        }


        private void CreateAllVertex() {
            // если мы будет получать оставшуюся длиину прыжка, то можно сделать так, у вершины есть движение наверх, пока длина больше либо равна 1
            for (int i = 1; i < tiles.length - 1; i++) {
                for (int j = 1; j < tiles[i].length - 1; j++) {
                    if (tiles[i][j] == Tile.WALL && tiles[i - 1][j] == Tile.WALL && tiles[i][j - 1] == Tile.WALL) { // правый верхний угол
                        vertices.add(new Vertex(tiles[i + 1][j + 1], new Vec2Float(i + 1, j + 1)));
                        vertices.add(new Vertex(tiles[i][j + 1], new Vec2Float(i, j + 1)));
                    } else if (tiles[i][j] == Tile.WALL && tiles[i + 1][j] == Tile.WALL && tiles[i][j - 1] == Tile.WALL) { // левый верхний угол
                        vertices.add(new Vertex(tiles[i - 1][j + 1], new Vec2Float(i - 1, j + 1)));
                        vertices.add(new Vertex(tiles[i][j + 1], new Vec2Float(i, j + 1)));
                    } else if (tiles[i][j] == Tile.WALL && tiles[i][j + 1] == Tile.WALL && tiles[i - 1][j] == Tile.WALL) { // внутренний правый угол
                        vertices.add(new Vertex(tiles[i - 1][j + 1], new Vec2Float(i - 1, j + 1)));
                    } else if (tiles[i][j] == Tile.WALL && tiles[i][j + 1] == Tile.WALL && tiles[i + 1][j] == Tile.WALL) { // внутренний левый угол
                        vertices.add(new Vertex(tiles[i + 1][j + 1], new Vec2Float(i + 1, j + 1)));
                    } else if (tiles[i][j] == Tile.PLATFORM) {                                                              // вершина над платформой
                        vertices.add(new Vertex(tiles[i][j + 1], new Vec2Float(i, j + 1)));
                    } else if (tiles[i][j] == Tile.LADDER) {                                                                // вершина лестницы
                        vertices.add(new Vertex(tiles[i][j], new Vec2Float(i, j)));
                        if (tiles[i][j + 1] == Tile.EMPTY) {                                                                // над лестницей пусто, то это тоже вершина
                            vertices.add(new Vertex(tiles[i][j + 1], new Vec2Float(i, j + 1)));
                        }
                    } else if (tiles[i][j] == Tile.JUMP_PAD) {                                                              // вершина джампада
                        vertices.add(new Vertex(tiles[i][j], new Vec2Float(i, j)));
                    } else if (tiles[i][j] == Tile.WALL && tiles[i][j + 1] == Tile.EMPTY) {                                 // вершина над стенкой
                        vertices.add(new Vertex(tiles[i][j + 1], new Vec2Float(i, j + 1)));
                    }
                }
            }
        }
    }

}


