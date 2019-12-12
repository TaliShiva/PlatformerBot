import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static util.Constants.*;

class PathFinder {


    private static double distanceSqr(Vec2Float a, Vec2Float b) {
        return (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }


    private Tile[][] tiles;
    private Graph graph;

    //конструктор в котором создаётся один раз сетка точек для поиска пути
    PathFinder(Game game, Debug debug) {
        tiles = game.getLevel().getTiles();
        graph = new Graph(debug);
    }


    public List<Pair<Graph.Vertex, Double>> getPath(Vec2Double startPosition, Vec2Double finishPosition, Debug debug) {
        //сперва притягиваем позицию к ближайшей вершине в графе
        Graph.Vertex startVer = relaxatePos(startPosition);
        debug.draw(new CustomData.Line(new Vec2Float((float) startPosition.getX(), (float) startPosition.getY()),
                new Vec2Float((float) startVer.getPosition().getX(), (float) startVer.getPosition().getY()),
                0.2f, new ColorFloat(0, 0, 0, 100)));

        Graph.Vertex finVer = relaxatePos(finishPosition);
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
                if (v.getKey().getPosition() == finVer.getPosition()) { // если нашли финальную вершину, то
                    Graph.Vertex tmpVer = v.getKey();
                    Double lengthToParent = v.getValue();
                    path.add(new Pair<>(tmpVer, lengthToParent));
                    while (tmpVer.getParent().getKey() != null) {
                        path.add(tmpVer.getParent());
                        Vec2Float debugV = new Vec2Float((float) tmpVer.getPosition().getX(), (float) tmpVer.getPosition().getY());
                        Vec2Float debugPV = new Vec2Float((float) tmpVer.getParent().getKey().getPosition().getX(), (float) tmpVer.getParent().getKey().getPosition().getY());
                        debug.draw(new CustomData.Line(debugV, debugPV, 0.2f, new ColorFloat(75, 75, 75, 100)));
                        tmpVer = tmpVer.getParent().getKey();
                        lengthToParent = tmpVer.getParent().getValue();
                    }
                    break; // путь получен
                }
                nonCheckedVertecies.add(v.getKey()); // обновляем список
            }
        }

        for (Graph.Vertex v : graph.vertices.values()) {
            v.isVisited = false;
        }
        return path;
    }

    public double getPathLength(List<Pair<Graph.Vertex, Double>> path) {
        double pathLength = 0;
        for (Pair<Graph.Vertex, Double> p : path) {
            pathLength += p.getValue();
        }
        return pathLength;
    }

    private Graph.Vertex relaxatePos(Vec2Double startPosition) {
        Graph.Vertex ver = null;
        double distance = 1000;
        for (Graph.Vertex v : graph.vertices.values()) {
            if (doubleDistance(startPosition, v.getPosition()) < distance) {
                distance = doubleDistance(startPosition, v.position);
                ver = v;
            }
        }
        return ver;
    }

    public class Graph {
        HashMap<Vec2Double, Vertex> vertices = new HashMap<>();

        // точка нужна, чтобы по ней получить в итоге путь до стартовой точки
        public class Vertex {
            //первый элемент

            private Tile startTile;
            private Vec2Double position;
            private boolean isVisited = false;
            private Pair<Vertex, Double> parent; //ссылка на предыдущую ноду и расстояние до неё
            private ArrayList<Pair<Vertex, Double>> neighbours = new ArrayList<>(); // соседние вершины хранятся в виде пары, конкретной вершины и стоимости туда передвижения

            Vertex(Tile startTile, Vec2Double position) {
                this.startTile = startTile;
                this.position = position;
            }

            public Vec2Double getPosition() {
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
        Graph(Debug debug) {
            CreateAllVertex();
            CreateAllEdges();
            debugDrawing(debug);
        }

        private void debugDrawing(Debug debug) {
            for (Vertex v : vertices.values()) {
                Vec2Float debugV = new Vec2Float((float) v.getPosition().getX(), (float) v.getPosition().getY());
                debug.draw(new CustomData.Rect(debugV, new Vec2Float(0.5f, 0.5f), new ColorFloat(100, 100, 0, 100)));
                for (Pair<Vertex, Double> nv : v.getNeighbours()) {
                    Vec2Float debugNV = new Vec2Float((float) nv.getKey().getPosition().getX(), (float) nv.getKey().getPosition().getY());
                    debug.draw(new CustomData.Line(debugV, debugNV, 0.07f, new ColorFloat(100, 0, 100, 100)));
                }
            }
        }

        private void CreateAllEdges() {

            for (Vertex vertex : vertices.values()) {
//                for (int j = 0; j < vertices.values().size(); j++) {
                for (Vertex nextVertex : vertices.values()) {
//                    Vertex nextVertex = vertices.get(j);
                    double length = doubleDistance(vertex.getPosition(), nextVertex.getPosition());
                    if (nextVertex == vertex) {
                        continue; // дабы избежать петель
                    }
                    if (length - Math.sqrt(2) <= EPS) {
                        vertex.setNeighbour(nextVertex, length);
                    }
                }
            }
        }


        private void CreateAllVertex() {
            // если мы будет получать оставшуюся длиину прыжка, то можно сделать так, у вершины есть движение наверх, пока длина больше либо равна 1
            int width = tiles.length;
            for (int i = 1; i < width - 1; i++) {
                int height = tiles[i].length;
                for (int j = 0; j < height - 1; j++) {
                    for (int z = 1; z <= 5.5; z++) {
                        if (j + z < height - 1) {
                            if (tiles[i][j] == Tile.PLATFORM && tiles[i][j + 1] != Tile.WALL) {                          // вершина над платформой
                                Vec2Double position = new Vec2Double(i, j + z);
                                vertices.put(position, new Vertex(tiles[i][j + z], position));
                            } else if (tiles[i][j] == Tile.LADDER) {
                                Vec2Double position = new Vec2Double(i, j);// вершина лестницы
                                vertices.put(position, new Vertex(tiles[i][j], position));
                            } else if (tiles[i][j] == Tile.LADDER && tiles[i][j + z] == Tile.EMPTY) {
                                Vec2Double position = new Vec2Double(i, j + z);// над лестницей пусто, то это тоже вершина
                                vertices.put(position, new Vertex(tiles[i][j + z], position));
                            } else if (tiles[i][j] == Tile.LADDER && z + i < width - 1 && tiles[i + z][j] == Tile.EMPTY) {
                                vertices.put(new Vec2Double(i + z, j), new Vertex(tiles[i][j + 1], new Vec2Double(i + z, j)));
                            } else if (tiles[i][j] == Tile.LADDER && i - z > 0 && tiles[i - z][j] == Tile.EMPTY) {
                                vertices.put(new Vec2Double(i - z, j), new Vertex(tiles[i][j + 1], new Vec2Double(i - z, j)));
                            } else if (tiles[i][j + z] != Tile.WALL && tiles[i][j] == Tile.WALL) {                       // вершина над стенкой
                                vertices.put(new Vec2Double(i, j + z), new Vertex(tiles[i][j + 1], new Vec2Double(i, j + z)));
                            }
                        }

                    }
                    if (tiles[i][j] == Tile.JUMP_PAD) {
                        for (int z = 1; z <= 11; z++) {
                            if (z + j < height - 1 && tiles[i][j] == Tile.JUMP_PAD && tiles[i][j + z] != Tile.WALL) {        // вершина джампада
                                vertices.put(new Vec2Double(i, j + z), new Vertex(tiles[i][j + z], new Vec2Double(i, j + z)));
                            }
                            if (i - z > 0 && tiles[i][j] == Tile.JUMP_PAD && tiles[i - z][j] != Tile.WALL) {
                                vertices.put(new Vec2Double(i - z, j), new Vertex(tiles[i - z][j], new Vec2Double(i - z, j)));
                            }
                            if (i + z < width - 1 && tiles[i][j] == Tile.JUMP_PAD && tiles[i + z][j] != Tile.WALL) {
                                vertices.put(new Vec2Double(i + z, j), new Vertex(tiles[i + z][j], new Vec2Double(i + z, j)));
                            }
                        }
                    }
                }
            }

        }

    }


}
