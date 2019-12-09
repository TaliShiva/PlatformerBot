import model.Game;
import model.Tile;
import model.Vec2Float;

public class Section {
    private static Tile[][] lvlTiles; // Один раз на жизнь объекта храним все тайлы
    private int emptyTilesQuantity;

    public int getEmptyTilesQuantity() {
        return emptyTilesQuantity;
    }

    public boolean isHaveColllision() {
        return haveColllision;
    }

    private boolean haveColllision;

    public Section(Game game) {
        lvlTiles = game.getLevel().getTiles();
    }

    public Section(int emptyTilesQuantity, boolean haveColllision) {
        this.emptyTilesQuantity = emptyTilesQuantity;
        this.haveColllision = haveColllision;
    }

    /**
     * Метод проверяет коллизии между точками если они на одной горизонтали
     *
     * @return возвращает отрезок, говорит есть ли коллизии и сколько от pos1 до pos2 было пустых тайлов
     */
    Section checkCollisionBetweenTwoHorizontalPositions(Vec2Float pos1, Vec2Float pos2) {
        int lengthX = (int) Math.abs(pos1.getX() - pos2.getX());
        int lengthY = (int) Math.abs(pos1.getY() - pos2.getY());

        if (lengthY > 1) {
//            throw new IllegalStateException("не могла быть такая хуета");
            return new Section(0, true);
        }
        if (pos1.getX() < pos2.getX()) {
            for (int i = 0; i < lengthX; i++) {
                if (lvlTiles[(int) pos1.getX() + i][(int) pos1.getY()] == Tile.WALL) { //если точка слева от pos1 (трейсинг влево)
                    return new Section(i, true);
                }
            }
        } else { // Не может быть ситуации равенства, это бы значило, что игроки друг в друге стоят
            for (int i = lengthX; i > 0; i--) {
                if (lvlTiles[(int) pos1.getX() - i][(int) pos1.getY()] == Tile.WALL) { //если точка справа от pos1 (трейсинг вправо)
                    return new Section(i, true);
                }
            }
        }
        return new Section((int) pos1.getX() - (int) pos2.getX(), false);
    }

    /**
     * Метод проверяет коллизии между точками если они на одной вертикали
     *
     * @param pos1
     * @param pos2
     * @return возвращает отрезок, говорит есть ли коллизии и сколько от pos1 до pos2 было пустых тайлов
     */
    Section checkСollisionBetweenTwoVerticalPositions(Vec2Float pos1, Vec2Float pos2) {
        float lengthX = Math.abs(pos1.getX() - pos2.getX());
        int lengthY = (int) Math.abs(pos1.getY() - pos2.getY());

        if (lengthX - 1 > 1e-6) {
//            throw new IllegalStateException("не могла быть такая хуета");
            return new Section(0, true);
        }
        if (pos1.equals(pos2)) {
            return new Section(0, false);
        }
        if (pos1.getY() < pos2.getY()) {
            for (int j = 0; j < lengthY; j++) {
                if (lvlTiles[(int) pos1.getX()][(int) pos2.getY() + j] == Tile.WALL) {
                    return new Section(j, true);
                }
            }

        } else {
            for (int j = lengthY; j > 0; j--) {
                if (lvlTiles[(int) pos1.getX()][(int) pos1.getY() - j] == Tile.WALL) {
                    return new Section(j, true);
                }
            }
        }
        return new Section((int) pos1.getY() - (int) pos2.getY(), false);
    }

    /**
     * Проверка диагональных коллизий
     */
    Section checkСollisionBetweenTwoDiagonalPositions(Vec2Float pos1, Vec2Float pos2) {
        int length = Math.abs((int) pos1.getX() - (int) pos2.getX());

        if (pos1.equals(pos2)) {
            return new Section(0, false);
        }
        if (pos1.getY() < pos2.getY()) {
            if (pos1.getX() < pos2.getX()) { //диагональ слева-снизу -> направо наверх
                for (int k = 0; k < length; k++) {
                    if (lvlTiles[(int) pos1.getX() + k][(int) pos1.getY() + k] == Tile.WALL) {
                        return new Section(k, true);
                    }
                }
            } else { // диагональ справа-снизу -> налево наверх
                for (int k = 0; k < length; k++) {
                    if (lvlTiles[(int) pos1.getX() - k][(int) pos1.getY() + k] == Tile.WALL) {
                        return new Section(k, true);
                    }
                }
            }
        } else if (pos1.getY() > pos2.getY()) {
            if (pos1.getX() < pos2.getX()) { // диагональ слева-сверху -> направо-вниз
                for (int k = 0; k < length; k++) {
                    if (lvlTiles[(int) pos1.getX() + k][(int) pos1.getY() - k] == Tile.WALL) {
                        return new Section(k, true);
                    }
                }
            } else { // диагональ справа-сверху -> налево-вниз
                for (int k = 0; k < length; k++) {
                    if (lvlTiles[(int) pos1.getX() - k][(int) pos1.getY() - k] == Tile.WALL) {
                        return new Section(k, true);
                    }
                }
            }
        }
        return new Section(length, false);
    }

}