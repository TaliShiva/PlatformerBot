import model.Level;
import model.Tile;
import model.Vec2Double;

import java.util.ArrayList;
import java.util.List;

class PathFinder {
    private List<Point> visitedPoints = new ArrayList<>();
    private List<Point> noVisitedPoints = new ArrayList<>();
    Tile[][] tiles;
    Grid grid;
    //конструктор в котором создаётся один раз сетка точек для поиска пути
    PathFinder(Level lvl) {
        tiles = lvl.getTiles();
        grid = new Grid();
    }


    public Tile[] getPath(Vec2Double startPosition, Vec2Double finishPosition) {
        final Tile startTile = tiles[(int) startPosition.getX()][(int) startPosition.getY()];
        Point startPoint = new Point(startTile);

        visitedPoints.add(startPoint); // поместили начальный тайл
        while (!noVisitedPoints.isEmpty()) {
            for (Tile neighbour : startTile.getNeighbours()) {

            }
        }
    }

    // точка нужна, чтобы по ней получить в итоге путь до стартовой точки
    class Point {
        //первый элемент
        private List<Tile> parents = new ArrayList<>();
        private Tile tile;

        Point(Tile startTile) {
            tile = startTile;
        }

        public void setParent(Tile parent) {
            parents.add(parent);
        }
    }
    class Grid{
    List<List<Point>> moveGraph = new ArrayList<>();

    Grid(){
        for (int i = 1; i < tiles.length - 1; i++) {
            for (int j = 1; j < tiles[i].length - 1; j++) {

            }
        }
    }
    }

}
// сперва ни одной ноды не посещали

