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


    public List<Pair<Graph.Vertex, Double>> getPath(Vec2Double startPosition, Vec2Double finishPosition, Debug debug) {
        //сперва притягиваем позицию к ближайшей вершине в графе
        Graph.Vertex startVer = relaxateFinPos(startPosition,finishPosition);
        debug.draw(new CustomData.Line(new Vec2Float((float) startPosition.getX(), (float) startPosition.getY()), startVer.getPosition(), 0.2f, new ColorFloat(0, 0, 0, 100)));

        Graph.Vertex finVer = relaxateFinPos(finishPosition,finishPosition);
        List<Pair<Graph.Vertex, Double>> path = new ArrayList<>();
        List<Graph.Vertex> nonCheckedVertecies = new ArrayList<>();
        startVer.setParent(null, 0);
        startVer.isVisited = true;
        nonCheckedVertecies.add(startVer);
        for (int i = 0; i < nonCheckedVertecies.size(); i++) {
            Graph.Vertex currentVertex = nonCheckedVertecies.get(i);
            for (Pair<Graph.Vertex, Double> v : currentVertex.getNeighbours()) {
                if (v.getKey().isVisited) {
                    continue; // если бывали в вершине то к следующей
                }
                v.getKey().isVisited = true; //если не бывали ещё в ней, то отмечаем тут, что побывали
                v.getKey().setParent(currentVertex, v.getValue()); // поместили длину ещё до предка
                if (v.getKey() == finVer) { // если нашли финальную вершину, то
                    Graph.Vertex tmpVer = v.getKey();
                    Double lengthToParent = v.getValue();
                    path.add(new Pair<>(tmpVer, lengthToParent));
                    while (tmpVer.getParent().getKey() != null) {
                        path.add(tmpVer.getParent());
                        debug.draw(new CustomData.Line(tmpVer.getPosition(), tmpVer.getParent().getKey().getPosition(), 0.2f, new ColorFloat(75, 75, 75, 100)));
                        tmpVer = tmpVer.getParent().getKey();
                        lengthToParent = tmpVer.getParent().getValue();
                    }
                    break; // путь получен
                }
                nonCheckedVertecies.add(v.getKey()); // обновляем список
            }
        }
        return path;
    }

    public double getPathLength(List<Pair<Graph.Vertex, Double>> path){
        double pathLength = 0;
        for (Pair<Graph.Vertex, Double> p : path) {
            pathLength += p.getValue();
        }
        return pathLength;
    }

    private Graph.Vertex relaxateFinPos(Vec2Double startPosition, Vec2Double finishPosition) {
        Vec2Float floatFinishPosition = new Vec2Float((float) startPosition.getX(), (float) startPosition.getY());
        Graph.Vertex ver = null;
        double distance = 1000;
        for (Graph.Vertex v : graph.vertices) {
            if (distanceSqr(floatFinishPosition, v.position) < distance) {
//            if (Math.abs(floatFinishPosition.getX() + v.getPosition().getX()) < distance) {
                distance = distanceSqr(floatFinishPosition, v.position);
                ver = v;
            }
        }
        return ver;
    }

    public class Graph {
        List<Vertex> vertices = new ArrayList<>();

        // точка нужна, чтобы по ней получить в итоге путь до стартовой точки
        public class Vertex {
            //первый элемент

            private Tile startTile;
            private Vec2Float position;
            private boolean isVisited = false;
            private Pair<Vertex, Double> parent; //ссылка на предыдущую ноду и расстояние до неё
            private ArrayList<Pair<Vertex, Double>> neighbours = new ArrayList<>(); // соседние вершины хранятся в виде пары, конкретной вершины и стоимости туда передвижения

            Vertex(Tile startTile, Vec2Float position) {
                this.startTile = startTile;
                this.position = position;
            }

            public Vec2Float getPosition() {
                return position;
            }

            public void setNeighbour(Vertex neighbour, Double weight) {
                if (neighbour != null)
                    this.neighbours.add(new Pair<>(neighbour, weight));
            }

            public ArrayList<Pair<Vertex, Double>> getNeighbours() {
                return neighbours;
            }

            public void setParent(Vertex parent, double length) {
                this.parent = new Pair<>(parent, length);
            }

            public Pair<Vertex, Double> getParent() {
                return parent;
            }
        }

        /**
         * Создание графа доступных для движений точек, с учётом коллизий от стен
         */
        Graph(Game game, Debug debug) throws IOException {
            double unitJumpLength = game.getProperties().getUnitJumpSpeed() * game.getProperties().getUnitJumpTime(); // длина прыжка
            CreateAllVertex();
            CreateAllEdges(game, unitJumpLength);
            debugDrawing(debug);
        }

        private void debugDrawing(Debug debug) {
            for (Vertex v : vertices) {
                debug.draw(new CustomData.Rect(v.getPosition(), new Vec2Float(0.5f, 0.5f), new ColorFloat(100, 100, 0, 100)));
                for (Pair<Vertex, Double> nv : v.getNeighbours()) {
                    debug.draw(new CustomData.Line(v.position, nv.getKey().position, 0.07f, new ColorFloat(100, 0, 100, 100)));
                }
            }
        }

        private void CreateAllEdges(Game game, double unitJumpLength) {
            double jumpPudLength = game.getProperties().getJumpPadJumpTime() * game.getProperties().getJumpPadJumpSpeed();
            Section section = new Section(game);
            for (int i = 0; i < vertices.size(); i++) {
                Vertex vertex = vertices.get(i);
                for (int j = 0; j < vertices.size(); j++) {
                    Vertex nextVertex = vertices.get(j);
                    if (nextVertex == vertex) {
                        continue; // дабы избежать петель
                    }
                    if (Math.abs(vertex.getPosition().getX() - nextVertex.getPosition().getX()) <= 1 && Math.abs(vertex.getPosition().getY() - nextVertex.getPosition().getY()) <= EPS) {
                        vertex.setNeighbour(nextVertex, 1d);// добавление ближайших горизонтальных соседей
                    }
                    if (Math.abs(vertex.getPosition().getY() - nextVertex.getPosition().getY()) <= 1 && Math.abs(vertex.getPosition().getX() - nextVertex.getPosition().getX()) <= EPS) {
                        vertex.setNeighbour(nextVertex, 1d); // добавление вертикальных ближайших соседей
                    }
                    if (Math.abs(vertex.getPosition().getX() - nextVertex.getPosition().getX()) < 1) {
                        if (!section.checkСollisionBetweenTwoVerticalPositions(vertex.getPosition(), nextVertex.getPosition()).isHaveColllision() &&
                                Math.abs(vertex.getPosition().getY() - nextVertex.getPosition().getY()) <= unitJumpLength) {
                            // если нет коллизий и длина ребра не больше чем прыжок, то добавляем вертикальное ребро
                            vertex.setNeighbour(nextVertex, Math.abs((double) vertex.getPosition().getY() - nextVertex.getPosition().getY()));
                        }
                        if (tiles[(int) vertex.getPosition().getX()][(int) vertex.getPosition().getY()] == Tile.JUMP_PAD &&
                                !section.checkСollisionBetweenTwoVerticalPositions(vertex.getPosition(), nextVertex.getPosition()).isHaveColllision() && // добавление особых рёбер от джампада наверх
                                Math.abs(vertex.getPosition().getY() - nextVertex.getPosition().getY()) <= jumpPudLength) {
                            vertex.setNeighbour(nextVertex, Math.abs((double) vertex.getPosition().getY() - nextVertex.getPosition().getY()));
                            vertex.setNeighbour(vertex, Math.abs((double) nextVertex.getPosition().getY() - vertex.getPosition().getY()));
                        }
                        if (tiles[(int) nextVertex.getPosition().getX()][(int) nextVertex.getPosition().getY()] == Tile.JUMP_PAD &&
                                !section.checkСollisionBetweenTwoVerticalPositions(vertex.getPosition(), nextVertex.getPosition()).isHaveColllision() // добавление особых рёбер до джампада - сверху
                        ) {
                            vertex.setNeighbour(nextVertex, Math.abs((double) vertex.getPosition().getY() - nextVertex.getPosition().getY()));
                            vertex.setNeighbour(nextVertex, Math.abs((double) vertex.getPosition().getY() - nextVertex.getPosition().getY()));
                        }
                    }
                    if (Math.abs((int) vertex.getPosition().getX() - (int) nextVertex.getPosition().getX()) == Math.abs((int) vertex.getPosition().getY() - (int) nextVertex.getPosition().getY()) &&
                            !section.checkСollisionBetweenTwoDiagonalPositions(vertex.getPosition(), nextVertex.getPosition()).isHaveColllision() &&
                            floatDistanceSqr(vertex.getPosition(), nextVertex.getPosition()) <= unitJumpLength * 5.5) {
                        vertex.setNeighbour(nextVertex, floatDistanceSqr(vertex.getPosition(), nextVertex.getPosition()));
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
                    } else if (tiles[i][j] == Tile.EMPTY && tiles[i][j - 1] == Tile.WALL) {                                 // вершина над стенкой
                        vertices.add(new Vertex(tiles[i][j], new Vec2Float(i, j)));
                    }
                }
            }
        }
    }

    private static double floatDistanceSqr(Vec2Float a, Vec2Float b) {
        return (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }
}


