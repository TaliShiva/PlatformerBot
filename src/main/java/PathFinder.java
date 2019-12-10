import model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PathFinder {


    private static double distanceSqr(Vec2Double a, Vec2Double b) {
        return Math.sqrt(a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }

    private static final float EPS = 1e-6f;
    private static Tile[][] tiles;
    private static Graph graph;
    static double lengthOnOneTick = 1d / 6d;
    double jumpLength = 5.5;
    static int EXPERT_MUL = 3; //реальный граф карты очень дорого создавать

    //конструктор в котором создаётся один раз сетка точек для поиска пути
    PathFinder(Game game, Debug debug) throws IOException {
        tiles = game.getLevel().getTiles();
        graph = new Graph(debug);
    }

/*
    public List<Pair<Graph.Vertex, Double>> getPath(Vec2Double startPosition, Vec2Double finishPosition, Debug debug) {
        //сперва притягиваем позицию к ближайшей вершине в графе
        Graph.Vertex startVer = relaxateFinPos(startPosition, finishPosition);
        debug.draw(new CustomData.Line(new Vec2Float((float) startPosition.getX(), (float) startPosition.getY()), startVer.getPosition(), 0.2f, new ColorFloat(0, 0, 0, 100)));

        Graph.Vertex finVer = relaxateFinPos(finishPosition, finishPosition);
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

    public double getPathLength(List<Pair<Graph.Vertex, Double>> path) {
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

    */

    public static class Graph {
        List<Vertex> vertices = new ArrayList<>();

        // точка нужна, чтобы по ней получить в итоге путь до стартовой точки
        public class Vertex {
            private Vec2Double position;
            private boolean isVisited = false;
            private Pair<Vertex, Double> parent; //ссылка на предыдущую ноду и расстояние до неё
            private ArrayList<Pair<Vertex, Double>> neighbours = new ArrayList<>(); // соседние вершины хранятся в виде пары, конкретной вершины и стоимости туда передвижения

            Vertex(Vec2Double position) {
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
         * Создание графа доступных для движений точек путём наложения сетки
         * @param debug
         */
        Graph(Debug debug){
            CreateAllVertex();
            CreateAllEdges();
            debugDrawing(debug);
        }

        private void debugDrawing(Debug debug) {
            for (Vertex v : vertices) {

                debug.draw(new CustomData.Rect(new Vec2Float((float) v.getPosition().getX(), (float) v.getPosition().getY()),
                        new Vec2Float(0.05f, 0.05f),
                        new ColorFloat(100, 100, 0, 100)));
                for (Pair<Vertex, Double> nv : v.getNeighbours()) {
                    debug.draw(new CustomData.Line(
                            new Vec2Float((float) v.position.getX(), (float) v.position.getY()),
                            new Vec2Float((float) nv.getKey().getPosition().getX(), (float) nv.getKey().getPosition().getY()),
                            0.05f,
                            new ColorFloat(100, 0, 100, 100)));
                }
            }
        }

        private void CreateAllEdges() {
            for (int i = 0; i < vertices.size(); i++) {
                Vertex vertex = vertices.get(i);
                for (int j = 0; j < vertices.size(); j++) {
                    Vertex nextVertex = vertices.get(j);
                    double lengthBetweenVertex = distanceSqr(vertex.getPosition(), nextVertex.position);
                    if (nextVertex == vertex) {
                        continue; // дабы избежать петель
                    }
                    if (lengthBetweenVertex - lengthOnOneTick * EXPERT_MUL <= EPS)
                        vertex.setNeighbour(nextVertex, lengthBetweenVertex);
                }
            }
        }


        private void CreateAllVertex() {
            // если мы будет получать оставшуюся длиину прыжка, то можно сделать так, у вершины есть движение наверх, пока длина больше либо равна 1
            for (double i = 1; i < tiles.length - 1; i += lengthOnOneTick * EXPERT_MUL) {
                for (double j = 1; j < tiles[(int) i].length - 1; j += lengthOnOneTick * EXPERT_MUL) {
                    if (tiles[(int) i][(int) j] != Tile.WALL) {
                        vertices.add(new Vertex(new Vec2Double(i, j)));
                    }
                }
            }
        }
    }

    private static double floatDistanceSqr(Vec2Float a, Vec2Float b) {
        return (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }
}


