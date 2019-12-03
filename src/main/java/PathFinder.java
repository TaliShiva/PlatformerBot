import jdk.nashorn.internal.runtime.ListAdapter;
import model.Level;
import model.Tile;
import model.Vec2Double;
import java.util.ArrayList;
import java.util.List;

class PathFinder {
    List<Tile> frontier = new ArrayList<>();

    public Tile[] getPath(Vec2Double startPosition, Vec2Double finishPosition, Level lvl) {
        final Tile startTile = lvl.getTiles()[(int) startPosition.getX()][(int) startPosition.getY()];
        frontier.add(); // поместили начальный тайл
        for(Tile tile : )

        for (int i = 0; i < lvl.getTiles().length; i++) {
            for (int j = 0; j < lvl.getTiles()[i].length; j++) {

            }
        }
    }
}