import model.Game;
import model.Tile;
import model.Vec2Double;
import model.Vec2Float;

public class Section {
    private Tile[][] tiles;
    private double lengthOnOneTick = 1d / 6d;
    private double height;
    private double width;

    public Section(Tile[][] tiles) {
        this.tiles = tiles;
        height = tiles.length;
        width = tiles[0].length;
    }

    /**
     * Проверяет коллизии между двумя точками без учёта диаметра пули
     */
    public boolean checkCollision(Vec2Double startPos, Vec2Double endPos) {
        if (Math.abs(endPos.getX() - startPos.getX()) < 1e-6) { // случай когда тангенс улетает в бесконечность
            int i = (int) startPos.getX();
            if (startPos.getY() > endPos.getY()) {
                for (double j = startPos.getY(); j < endPos.getY(); j -= lengthOnOneTick) {
                    if (tiles[i][(int) (j)] == Tile.WALL) {
                        return true;
                    }
                }
            } else {
                for (double j = startPos.getY(); j > endPos.getY(); j += lengthOnOneTick) {
                    if (tiles[i][(int) (j)] == Tile.WALL) {
                        return true;
                    }
                }
            }
        }
        double tangens = (endPos.getY() - startPos.getY()) / (endPos.getX() - startPos.getX());

        if (startPos == endPos) {
            return true;
        }
        if (startPos.getX() < endPos.getX() && startPos.getY() <= endPos.getY()) { // Первый квадрант, тангенс положительный, при наращивании Y координаты, она складывается
            for (double i = startPos.getX(); i < endPos.getX(); i += lengthOnOneTick) {
                double Y = startPos.getY() + Math.abs(startPos.getX() - i) * tangens;
                if (startPos.getY() + lengthOnOneTick * tangens < height && tiles[(int) i][(int) Y] == Tile.WALL) {
                    return true;
                }
            }
        } else if (startPos.getX() > endPos.getX() && startPos.getY() <= endPos.getY()) {
            for (double i = startPos.getX(); i > endPos.getX(); i -= lengthOnOneTick) { //второй квадрант, тангенс отрицательный,но наращивать Y надо вверх, значит отнимаем его (складываем, так как он отрицательный)
                double Y = startPos.getY() - Math.abs(startPos.getX() - i) * tangens;
                if (startPos.getY() + lengthOnOneTick * tangens < height && tiles[(int) i][(int) Y] == Tile.WALL) {
                    return true;
                }
            }
        } else if (startPos.getX() > endPos.getX() && startPos.getY() >= endPos.getY()) {
            for (double i = startPos.getX(); i > endPos.getX(); i -= lengthOnOneTick) { //третий квадрант, тангенс положительный,но наращивать Y надо вниз, значит отнимаем его
                double Y = startPos.getY() - Math.abs(startPos.getX() - i) * tangens;
                if (startPos.getY() + lengthOnOneTick * tangens < height && tiles[(int) i][(int) Y] == Tile.WALL) {
                    return true;
                }
            }
        } else if (startPos.getX() < endPos.getX() && startPos.getY() >= endPos.getY()) {
            for (double i = startPos.getX(); i > endPos.getX(); i -= lengthOnOneTick) { //четвертый квадрант, тангенс отрицательнфй, наращивать Y надо вниз, значит складываем его
                double Y = startPos.getY() + Math.abs(startPos.getX() - i) * tangens;
                if (startPos.getY() + lengthOnOneTick * tangens < height && tiles[(int) i][(int) Y] == Tile.WALL) {
                    return true;
                }
            }
        }


        return false;
    }
}