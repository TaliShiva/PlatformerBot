import model.*;

public class Section {
    private static Tile[][] lvlTiles; // Один раз на жизнь объекта храним все тайлы
    private int emptyTilesQuantity;
    private boolean haveColllision;

    public Section(Game game) {
        lvlTiles = game.getLevel().getTiles();
        emptyTilesQuantity = 0;
        haveColllision = false;
    }

    public Section(int emptyTilesQuantity, boolean haveColllision) {
        this.emptyTilesQuantity = emptyTilesQuantity;
        this.haveColllision = haveColllision;
    }

     /**
     * Метод проверяет коллизии между точками если они на одной горизонтали
     * @return возвращает отрезок, говорит есть ли коллизии и сколько от pos1 до pos2 было пустых тайлов
     */
    Section checkCollisionBetweenTwoHorizontalPositions(Vec2Float pos1, Vec2Float pos2) {
        if (Math.abs(pos1.getY() - pos2.getY()) >= 1) {
            throw new IllegalStateException("не могла быть такая хуета");
        }
        int counter = 0;
        if (pos1.getX() < pos2.getX()) {
            for (int i = (int) pos1.getX(); i <= (int) pos2.getX(); i++) {
                if (lvlTiles[(int) pos1.getX() + i][(int) pos1.getY()] == Tile.WALL) { //если точка слева от pos1 (трейсинг влево)
                    return new Section(counter, true);
                }
                counter++;
            }

        } else { // Не может быть ситуации равенства, это бы значило, что игроки друг в друге стоят
            for (int i = (int) pos1.getX(); i >= (int) pos2.getX(); i--) {
                if (lvlTiles[(int) pos1.getX() - i][(int) pos1.getY()] == Tile.WALL) { //если точка справа от pos1 (трейсинг вправо)
                    return new Section(counter, true);
                }
                counter++;
            }
        }
        return new Section((int) pos1.getX() - (int) pos2.getX(), false);
    }

    /**
     * Метод проверяет коллизии между точками если они на одной вертикали
     * @return возвращает отрезок, говорит есть ли коллизии и сколько от pos1 до pos2 было пустых тайлов
     */
    Section checkСollisionBetweenTwoVerticalPositions(Vec2Float pos1, Vec2Float pos2) {
        if (Math.abs(pos1.getX() - pos2.getX()) >= 1) {
            throw new IllegalStateException("не могла быть такая хуета");
        }
        if (pos1.getY() < pos2.getY()) {
            for (int j = (int) pos1.getY(); j <= (int) pos2.getY(); j++) {
                if (lvlTiles[(int) pos1.getX()][(int) pos2.getY() + j] == Tile.WALL) {
                    return new Section(j - 1, true);
                }
            }

        } else {
            for (int j = (int) pos1.getY(); j >= (int) pos1.getY(); j--) {
                if (lvlTiles[(int) pos2.getX()][(int) pos1.getY() + j] == Tile.WALL) {
                    return new Section(j - 1, true);
                }
            }
        }
        return new Section((int) pos1.getY() - (int) pos2.getY(), false);
    }

}