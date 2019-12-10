import model.Game;
import model.Tile;
import model.Vec2Double;
import model.Vec2Float;

public class Section {
    private Tile[][] tiles;
    private double lengthOnOneTick = 1d / 6d;

    public Section(Tile[][] tiles) {
        this.tiles = tiles;
    }

    /**
     * Проверяет коллизии между двумя точками без учёта диаметра пули
     */
    public boolean checkCollision(Vec2Double startPos, Vec2Double endPos) {
        double tangens = (startPos.getY() - endPos.getY()) / (startPos.getX() - endPos.getX());
        boolean haveColllision = false;
        if (startPos == endPos) {
            return haveColllision;
        }
        if (startPos.getX() < endPos.getX()) {
            for (double i = startPos.getX(); i < endPos.getX(); i += lengthOnOneTick) {
                if (tiles[(int) i][(int) (i * tangens)] != Tile.WALL) {
                    haveColllision = true;
                    break;
                }
            }
        } else if (startPos.getX() > endPos.getX()) {
            for (double i = startPos.getX(); i > endPos.getX(); i -= lengthOnOneTick) {
                if (tiles[(int) i][(int) (i * tangens)] == Tile.WALL) {
                    haveColllision = true;
                    break;
                }
            }
        } else {
            int i = (int) startPos.getX();
            if (startPos.getY() > endPos.getY()) {
                for (double j = startPos.getY(); j < endPos.getY(); j += lengthOnOneTick) {
                    if (tiles[i][(int) (j)] == Tile.WALL) {
                        haveColllision = true;
                        break;
                    }
                }
            } else {
                for (double j = startPos.getY(); j > endPos.getY(); j -= lengthOnOneTick) {
                    if (tiles[i][(int) (j)] == Tile.WALL) {
                        haveColllision = true;
                        break;
                    }
                }
            }
        }
        return haveColllision;
    }
}