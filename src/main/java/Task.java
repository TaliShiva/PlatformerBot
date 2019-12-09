import model.Game;
import model.Unit;
import model.Vec2Double;

/**
 * Классу Task передаются либо пара вершин,
 * либо две позиции и он возвращает количество тиков,
 * необходимое для того, чтобы совершать именно это движение,
 * и пока оно не завершится карта не будет пересчитана
 */
public class Task {
    Task(Game game, Unit unitPos, Vec2Double endPos) {
        double speedOfOneMove = 1d / 60d;
        double lengthOnOneTick = speedOfOneMove * game.getProperties().getUnitMaxHorizontalSpeed();
        int predictTimeToCongratulateTask = (int)(((unitPos.getPosition().getX() - endPos.getX()) / lengthOnOneTick) + 1);
    }
}
