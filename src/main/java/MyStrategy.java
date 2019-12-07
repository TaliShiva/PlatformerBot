import model.*;

import java.io.IOException;

public class MyStrategy {
    public static final int MIN_ENEMY_HEALTH_FOR_SHOOTING_ROCKET = 80;
    public static final int MIN_ENEMY_HEALTH_FOR_CHANGE_ROCKET = 20;


    private static double distanceSqr(Vec2Double a, Vec2Double b) {
        return (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }

    private static boolean isStupidShot(Unit unit, Unit nearestEnemy, Game game, Debug debug) throws IOException {
        final Tile[][] lvlTiles = game.getLevel().getTiles();

        //TODO: исправить выстреливание всё таки
//        if (unit.getPosition().getY() - nearestEnemy.getPosition().getY() <= 1) {
//            if (shootHorPatch(unit, nearestEnemy, game)) {
//                return false;
//            }
//        }
//
//        if (unit.getPosition().getX() - nearestEnemy.getPosition().getX() <= 1) {
//            if (shootVerPatch(unit, nearestEnemy, game)) {
//                return false;
//            }
//        }


        final double distanceBetweenPlayers = distanceSqr(unit.getPosition(), nearestEnemy.getPosition());
        if (unit.getWeapon() != null && unit.getWeapon().getTyp() == WeaponType.ROCKET_LAUNCHER && distanceBetweenPlayers > 25) {
            return (lvlTiles[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY())] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY())] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY())] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY() - 1)] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY() - 1)] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY() + 1)] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY() + 1)] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX())][(int) (unit.getPosition().getY() + 1)] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX())][(int) (unit.getPosition().getY() - 1)] != Tile.EMPTY);
        }
        if (unit.getWeapon() != null && unit.getWeapon().getTyp() == WeaponType.ASSAULT_RIFLE && distanceBetweenPlayers > 25) {
            return (lvlTiles[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY())] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY())] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY())] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY() - 1)] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY() - 1)] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY() + 1)] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY() + 1)] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX())][(int) (unit.getPosition().getY() + 1)] != Tile.EMPTY) ||
                    (lvlTiles[(int) (unit.getPosition().getX())][(int) (unit.getPosition().getY() - 1)] != Tile.EMPTY);
        }


        return false;
    }

    private static boolean shootHorPatch(Unit unit, Unit nearestEnemy, Game game) {
        Section sec = new Section(game);
        Vec2Float floatUnitPos = new Vec2Float((float) unit.getPosition().getX(), (float) unit.getPosition().getY());
        Vec2Float floatEnemyPos = new Vec2Float((float) nearestEnemy.getPosition().getX(), (float) nearestEnemy.getPosition().getY());
        return sec.checkCollisionBetweenTwoHorizontalPositions(floatUnitPos, floatEnemyPos).isHaveColllision();
    }

    private static boolean shootVerPatch(Unit unit, Unit nearestEnemy, Game game) {
        Section sec = new Section(game);
        Vec2Float floatUnitPos = new Vec2Float((float) unit.getPosition().getX(), (float) unit.getPosition().getY());
        Vec2Float floatEnemyPos = new Vec2Float((float) nearestEnemy.getPosition().getX(), (float) nearestEnemy.getPosition().getY());
        return sec.checkСollisionBetweenTwoVerticalPositions(floatUnitPos, floatEnemyPos).isHaveColllision();
    }

    /**
     * @param unit - наш персонаж
     * @param game - контекст игры
     * @return - объект храняющий действие игрока
     */
    public UnitAction getAction(Unit unit, Game game, Debug debug) throws IOException {

        PathFinder pf = new PathFinder(game, debug);

        UnitAction action = new UnitAction();
        Unit nearestEnemy = getNearestEnemy(unit, game);
        LootBox nearestWeapon = getNearestWeapon(unit, game);
        Vec2Double nearestHealPos = getNearestHealPos(unit, game);

        //Подготовительная фаза - её смысл в том, чтобы подготовить юнита к бою, как понять что подготовительная фаза закончилась? (может быть когда хп игроков != 100, или когда они приблизились)
        Vec2Double targetPos = unit.getPosition(); // использовать только для aim-а
        if (unit.getWeapon() == null && nearestWeapon != null) {
            targetPos = nearestWeapon.getPosition(); // если нет оружия и вооружения, пиздуй к ним
        } else if (nearestEnemy != null) {
            targetPos = nearestEnemy.getPosition(); // если есть оружие, то пиздуй к врагу
        }
        targetPos = goToRocketIfNeed(unit, game, targetPos);
        drawLineToTarget(unit, debug, targetPos);

        //Блок ответственный за прыжки
        boolean jump = getJump(unit, game, nearestEnemy, targetPos);
        jump = jumpDownIfNeedIt(unit, game, targetPos, action);
        action.setJump(jump);
        action.setJumpDown(!jump);

        if (targetPos.getX() < unit.getPosition().getX()) {
            action.setVelocity(-1 * game.getProperties().getUnitMaxHorizontalSpeed());
        }
        if (targetPos.getX() > unit.getPosition().getX()) {
            action.setVelocity(game.getProperties().getUnitMaxHorizontalSpeed());
        }
        backFromEnemy(nearestEnemy, unit, action, game.getProperties().getUnitMaxHorizontalSpeed()); //просто меняет полярность движения если близко к сопернику
        savePlayer(unit, game, action, nearestEnemy, nearestHealPos); // спасаться приоритетней, по этому идёт позже, причём может сделать патч и на прыжок

        Vec2Double aim = new Vec2Double(nearestEnemy.getPosition().getX() - unit.getPosition().getX(),
                nearestEnemy.getPosition().getY() - unit.getPosition().getY());

        action.setAim(aim);

        if (isStupidShot(unit, nearestEnemy, game, debug)) {
            action.setShoot(false);
        } else {
            action.setShoot(true);
        }

        //блок смены оружия, самый понятный патч, здесь мы можем описать все ситуации в которых имеется бот
        weaponSwapping(unit, nearestEnemy, nearestWeapon, targetPos, action);

        //блок ответственный за минирование
        action.setPlantMine(false);

        return action;
    }

    private void drawLineToTarget(Unit unit, Debug debug, Vec2Double targetPos) {
        Vec2Float debugUnitPoint = new Vec2Float((float) unit.getPosition().getX(), (float) unit.getPosition().getY());
        Vec2Float debugTargetPoint = new Vec2Float((float) targetPos.getX(), (float) targetPos.getY());
        debug.draw(new CustomData.Line(debugUnitPoint, debugTargetPoint, 0.2f, new ColorFloat(100, 0, 0, 100)));
    }

    private Vec2Double getNearestHealPos(Unit unit, Game game) {
        Vec2Double nearestHealPos = new Vec2Double(0, 0);
        double length = 1000;
        for (LootBox lootBox : game.getLootBoxes()) {
            if (lootBox.getItem() instanceof Item.HealthPack) { // если лутбокс это аптечка
                final double distance = distanceSqr(lootBox.getPosition(), unit.getPosition());
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
                if (nearestWeapon == null || distanceSqr(unit.getPosition(),
                        lootBox.getPosition()) < distanceSqr(unit.getPosition(), nearestWeapon.getPosition())) {
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
                if (nearestEnemy == null || distanceSqr(unit.getPosition(),
                        other.getPosition()) < distanceSqr(unit.getPosition(), nearestEnemy.getPosition())) {
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

    private void weaponSwapping(Unit unit, Unit nearestEnemy, LootBox nearestWeapon, Vec2Double targetPos, UnitAction action) {
        if (unit.getWeapon() != null && unit.getWeapon().getTyp() == WeaponType.PISTOL) {
            action.setSwapWeapon(true);
        } else if (unit.getWeapon() != null && nearestWeapon.getPosition() != null && nearestEnemy.getHealth() >= MIN_ENEMY_HEALTH_FOR_SHOOTING_ROCKET &&
                unit.getWeapon().getTyp() == WeaponType.ASSAULT_RIFLE && (distanceSqr(unit.getPosition(), targetPos)) <= 1) {
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

    private void backFromEnemy(Unit nearestEnemy, Unit unit, UnitAction action, double speed) {

        Vec2Double enemyPosition = nearestEnemy.getPosition();
        Vec2Double unitPosition = unit.getPosition();

        if (distanceSqr(unitPosition, enemyPosition) <= 30) {
            if (enemyPosition.getX() > unitPosition.getX()) {// враг правее
                action.setVelocity(-1 * speed);
            } else if (enemyPosition.getX() < unitPosition.getX()) {//враг левее
                action.setVelocity(speed);
            }
        }
    }

    private void savePlayer(Unit unit, Game game, UnitAction action, Unit nearestEnemy, Vec2Double nearestHealPos) {

        if (nearestEnemy != null && (unit.getHealth() <= 50 && nearestEnemy.getHealth() >= 30)) {
            if (nearestHealPos.getX() < unit.getPosition().getX()) {
                action.setVelocity(-1 * game.getProperties().getUnitMaxHorizontalSpeed());
            } else {
                action.setVelocity(game.getProperties().getUnitMaxHorizontalSpeed());
            }
            if (nearestHealPos.getY() > unit.getPosition().getY()) {
                action.setJump(true);
                action.setJumpDown(false);
            } else {
                action.setJump(false);
                action.setJumpDown(true);
            }
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