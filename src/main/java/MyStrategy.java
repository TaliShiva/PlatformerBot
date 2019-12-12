import model.*;

import java.io.IOException;
import java.util.List;

import static util.Constants.doubleDistance;
import static util.Constants.lengthOnOneTick;

public class MyStrategy {
    public static final int MIN_ENEMY_HEALTH_FOR_SHOOTING_ROCKET = 80;
    public static final int MIN_ENEMY_HEALTH_FOR_CHANGE_ROCKET = 20;
    static int endTaskTick = 0;
    private UnitAction lastUnitAction = new UnitAction();
    private List<Pair<PathFinder.Graph.Vertex, Double>> path;
    PathFinder pf = null;

    private boolean isStupidShot(Unit unit, Unit nearestEnemy, Game game, Debug debug) throws IOException {

        final Tile[][] lvlTiles = game.getLevel().getTiles();
        Section sec = new Section(lvlTiles);
        Vec2Double unitCenter = new Vec2Double(unit.getPosition().getX(), unit.getPosition().getY() + 1);
        Vec2Double enemyCenter = new Vec2Double(nearestEnemy.getPosition().getX(), nearestEnemy.getPosition().getY() + 1);
        drawLineToTarget(unitCenter, enemyCenter, debug, new ColorFloat(0, 50, 50, 75));

        if (sec.checkCollision(unitCenter, enemyCenter)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param unit - наш персонаж
     * @param game - контекст игры
     * @return - объект храняющий действие игрока
     */
    public UnitAction getAction(Unit unit, Game game, Debug debug) throws IOException {
        if (pf == null) {
            pf = new PathFinder(game, debug);
        }
        UnitAction action = new UnitAction();
        Unit nearestEnemy = getNearestEnemy(unit, game);
        LootBox nearestWeapon = getNearestWeapon(unit, game);
        Vec2Double nearestHealPos = getNearestHealPos(unit, game, pf, debug);

        //Подготовительная фаза - её смысл в том, чтобы подготовить юнита к бою, как понять что подготовительная фаза закончилась? (может быть когда хп игроков != 100, или когда они приблизились)
        Vec2Double targetPos = unit.getPosition(); // использовать только для aim-а
        if (unit.getWeapon() == null && nearestWeapon != null) {
            targetPos = nearestWeapon.getPosition(); // если нет оружия и вооружения, пиздуй к ним
        } else if (nearestEnemy != null) {
            targetPos = nearestEnemy.getPosition(); // если есть оружие, то пиздуй к врагу
        }

        targetPos = goToRocketIfNeed(unit, game, targetPos);
        if (nearestEnemy != null && (unit.getHealth() <= 65 && nearestEnemy.getHealth() >= 30)) {
            targetPos = nearestHealPos;
        }

        drawLineToTarget(unit.getPosition(), targetPos, debug, new ColorFloat(75, 75, 75, 75));

        action = movingModule(unit, game, debug, pf, action, nearestEnemy, targetPos);
        backFromEnemy(game, nearestEnemy, unit, action, game.getProperties().getUnitMaxHorizontalSpeed()); //просто меняет полярность движения если близко к сопернику

        //savePlayer(unit, game, action, nearestEnemy, nearestHealPos); // спасаться приоритетней, по этому идёт позже, причём может сделать патч и на прыжок

        /** НЕ ОТНОСИТСЯ К ДВИЖЕНИю*/
        Vec2Double aim = new Vec2Double(nearestEnemy.getPosition().getX() - unit.getPosition().getX(),
                nearestEnemy.getPosition().getY() - unit.getPosition().getY());
        action.setAim(aim);

        if (isStupidShot(unit, nearestEnemy, game, debug)) {
            action.setShoot(false);
        } else {
            action.setShoot(true);
        }

        //блок смены оружия, самый понятный патч, здесь мы можем описать все ситуации в которых имеется бот
        weaponSwapping(unit, nearestEnemy, nearestWeapon, action);
        //блок ответственный за минирование
        action.setPlantMine(false);
        return action;
    }

    private UnitAction movingModule(Unit unit, Game game, Debug debug, PathFinder pf, UnitAction action, Unit nearestEnemy, Vec2Double targetPos) {
        path = pf.getPath(unit.getPosition(), targetPos, debug);
        if (path.size() != 0) { // если существует путь какой-то, потому что если path - пустой, то его тупо нет
            PathFinder.Graph.Vertex vertex = path.get(path.size() - 1).getKey(); // сейчас путь в обратном порядке записан,
            PathFinder.Graph.Vertex nextVertex = path.get(path.size() - 2).getKey(); // сейчас путь в обратном порядке записан,
            final int tickCountToCongratulateMove = getTickCountToCongratulateTask(game, vertex.getPosition(), new Vec2Double(nextVertex.getPosition().getX(), nextVertex.getPosition().getY())); // считаем количество тиков до конца таска
            endTaskTick = game.getCurrentTick() + tickCountToCongratulateMove;
            targetPos = new Vec2Double(nextVertex.getPosition().getX(), nextVertex.getPosition().getY()); //задаём цель движения и по паттернам будем придумывать движения
            // движение влево
            if (targetPos.getX() < unit.getPosition().getX()) {
                action.setVelocity(-1 * game.getProperties().getUnitMaxHorizontalSpeed());
            }
            //движение вправо
            if (targetPos.getX() > unit.getPosition().getX()) {
                action.setVelocity(game.getProperties().getUnitMaxHorizontalSpeed());
            }
            //если цель выше, то прыгай
            if (targetPos.getY() > unit.getPosition().getY() && unit.getJumpState().isCanJump()) {
                action.setJump(true);
            }
            //если цель ниже, то прыгай вниз
            if (targetPos.getY() < unit.getPosition().getY() && unit.getJumpState().isCanJump()) {
                action.setJumpDown(true);
            }
            lastUnitAction = action;// сохраняем ссылку на последний экшен

        } else {
            System.out.printf("поиск пути отказал %d\n", game.getCurrentTick());
            pf = new PathFinder(game, debug);
            // подстраховка на случай отказывания основного алгоритма
            if (targetPos.getX() < unit.getPosition().getX()) {
                action.setVelocity(-1 * game.getProperties().getUnitMaxHorizontalSpeed());
            }
            if (targetPos.getX() > unit.getPosition().getX()) {
                action.setVelocity(game.getProperties().getUnitMaxHorizontalSpeed());
            }

        }

        drawLineToTarget(unit.getPosition(), targetPos, debug, new

                ColorFloat(100, 0, 100, 100));
        return action;
    }


    private void drawLineToTarget(Vec2Double startPos, Vec2Double targetPos, Debug debug, ColorFloat color) {
        Vec2Float debugUnitPoint = new Vec2Float((float) startPos.getX(), (float) startPos.getY());
        Vec2Float debugTargetPoint = new Vec2Float((float) targetPos.getX(), (float) targetPos.getY());
        debug.draw(new CustomData.Line(debugUnitPoint, debugTargetPoint, 0.2f, color));
    }

    private int getTickCountToCongratulateTask(Game game, Vec2Double startPos, Vec2Double endPos) {
        return (int) ((doubleDistance(startPos, endPos) / lengthOnOneTick));
    }


    private Vec2Double getNearestHealPos(Unit unit, Game game, PathFinder pf, Debug debug) {
        Vec2Double nearestHealPos = new Vec2Double(0, 0);
        double length = 1000;
        for (LootBox lootBox : game.getLootBoxes()) {
            if (lootBox.getItem() instanceof Item.HealthPack) { // если лутбокс это аптечка
                //double distance = pf.getPathLength(pf.getPath(unit.getPosition(), lootBox.getPosition(), debug));
                final double distance = doubleDistance(lootBox.getPosition(), unit.getPosition());
                if (length > distance) { // если у неё
                    length = distance;
                    nearestHealPos = lootBox.getPosition();
                }
            }
        }
        return nearestHealPos;
    }

    private LootBox getNearestWeapon(Unit unit, Game game) {
        LootBox nearestWeapon = null;
        for (LootBox lootBox : game.getLootBoxes()) {
            if (lootBox.getItem() instanceof Item.Weapon) { // если лутбокс это оружие, то
                if (nearestWeapon == null || doubleDistance(unit.getPosition(),
                        lootBox.getPosition()) < doubleDistance(unit.getPosition(), nearestWeapon.getPosition())) {
                    nearestWeapon = lootBox;
                }
            }
        }
        return nearestWeapon;
    }


    private Unit getNearestEnemy(Unit unit, Game game) {
        Unit nearestEnemy = null;
        for (Unit other : game.getUnits()) {
            if (other.getPlayerId() != unit.getPlayerId()) {
                if (nearestEnemy == null || doubleDistance(unit.getPosition(),
                        other.getPosition()) < doubleDistance(unit.getPosition(), nearestEnemy.getPosition())) {
                    nearestEnemy = other;
                }
            }
        }
        return nearestEnemy;
    }

    private boolean getJump(Unit unit, Game game, Unit nearestEnemy, Vec2Double targetPos) {
        boolean jump = targetPos.getY() > unit.getPosition().getY();
        if (targetPos.getX() > unit.getPosition().getX() && game.getLevel()
                .getTiles()[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY())] == Tile.WALL) {
            jump = true;
        }
        if (targetPos.getX() < unit.getPosition().getX() && game.getLevel()
                .getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY())] == Tile.WALL) {
            jump = true;
        }
        return jump;
    }

    private void weaponSwapping(Unit unit, Unit nearestEnemy, LootBox nearestWeapon, UnitAction action) {
        if (unit.getWeapon() != null && unit.getWeapon().getTyp() == WeaponType.PISTOL) {
            action.setSwapWeapon(true);
        } else if (unit.getWeapon() != null && nearestWeapon.getPosition() != null && nearestEnemy.getHealth() >= MIN_ENEMY_HEALTH_FOR_SHOOTING_ROCKET &&
                unit.getWeapon().getTyp() == WeaponType.ASSAULT_RIFLE && (doubleDistance(unit.getPosition(), nearestWeapon.getPosition())) <= 1) {
            action.setSwapWeapon(true);
        } else if (unit.getWeapon() != null && nearestEnemy.getHealth() <= MIN_ENEMY_HEALTH_FOR_CHANGE_ROCKET && unit.getWeapon().getTyp() == WeaponType.ROCKET_LAUNCHER) {
            if (nearestWeapon.getItem() instanceof Item.Weapon && ((Item.Weapon) nearestWeapon.getItem()).getWeaponType() == WeaponType.ASSAULT_RIFLE)
                action.setSwapWeapon(true);
        } else {
            action.setSwapWeapon(false);
        }
    }

    private boolean jumpDownIfNeedIt(Unit unit, Game game, Vec2Double targetPos, UnitAction action) {
        if ((game.getLevel().getTiles()[(int) unit.getPosition().getX()][(int) unit.getPosition().getY() - 1] == Tile.PLATFORM ||
                game.getLevel().getTiles()[(int) unit.getPosition().getX()][(int) unit.getPosition().getY() - 1] == Tile.LADDER) &&
                targetPos.getY() < unit.getPosition().getY()) {
            return false;
        } else {
            return true;
        }
    }

    private void backFromEnemy(Game game, Unit nearestEnemy, Unit unit, UnitAction action, double speed) {

        Vec2Double enemyPosition = nearestEnemy.getPosition();
        Vec2Double unitPosition = unit.getPosition();

        if (doubleDistance(unitPosition, enemyPosition) <= 15) {
            endTaskTick = game.getCurrentTick();
            if (enemyPosition.getX() > unitPosition.getX()) {// враг правее
                action.setVelocity(-1 * speed);
            } else if (enemyPosition.getX() < unitPosition.getX()) {//враг левее
                action.setVelocity(speed);
            }
        }
    }

    private void savePlayer(Unit unit, Game game, UnitAction action, Unit nearestEnemy, Vec2Double nearestHealPos) {

        if (nearestEnemy != null && (unit.getHealth() <= 65 && nearestEnemy.getHealth() >= 30)) {

//            if (nearestHealPos.getX() < unit.getPosition().getX()) {
//                action.setVelocity(-1 * game.getProperties().getUnitMaxHorizontalSpeed());
//            } else {
//                action.setVelocity(game.getProperties().getUnitMaxHorizontalSpeed());
//            }
//            if (nearestHealPos.getY() > unit.getPosition().getY()) {
//                action.setJump(true);
//                action.setJumpDown(false);
//            } else {
//                action.setJump(false);
//                action.setJumpDown(true);
//            }
        }
    }

    /**
     * Сделать целевой позицией лутбокс с ракетницей, если в руках оружие (пистолет или автомат)
     */
    private Vec2Double goToRocketIfNeed(Unit unit, Game game, Vec2Double targetPos) {
        if (unit.getWeapon() != null && (unit.getWeapon().getTyp() == WeaponType.ASSAULT_RIFLE || unit.getWeapon().getTyp() == WeaponType.PISTOL)) {
            for (LootBox lootBox : game.getLootBoxes()) {
                Item item = lootBox.getItem();
                if (item instanceof Item.Weapon) { // если лутбокс это оружие, то
                    if (((Item.Weapon) item).getWeaponType() == WeaponType.ROCKET_LAUNCHER)
                        targetPos = lootBox.getPosition();
                }
            }
        }
        return targetPos;
    }
}