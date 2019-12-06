import model.*;

import java.util.ArrayList;
import java.util.List;

public class MyStrategy {
    public static final int MIN_ENEMY_HEALTH_FOR_SHOOTING_ROCKET = 80;
    public static final int MIN_ENEMY_HEALTH_FOR_CHANGE_ROCKET = 20;
    private boolean iSearchRocket = false;
    private double EPS = 1;

    private static double distanceSqr(Vec2Double a, Vec2Double b) {
        return (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }

    private static boolean isStupidShot(Unit unit, Unit nearestEnemy, Game game) {
        final Tile[][] lvlTiles = game.getLevel().getTiles();
        Boolean x = shootIfOneLevel(unit, nearestEnemy, lvlTiles);
        if (x != null) return x;

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

    private static Boolean shootIfOneLevel(Unit unit, Unit nearestEnemy, Tile[][] lvlTiles) {
        if (Math.abs(unit.getPosition().getY() - nearestEnemy.getPosition().getY()) < 2) // если игроки примерно на одной выcоте
        {
            int i = 0;
            if (nearestEnemy.getPosition().getX() < unit.getPosition().getX()) {//если враг слева (трейсинг влево)
                while (true) {
                    if (unit.getPosition().getX() - i > 0 && unit.getPosition().getX() - i < lvlTiles[0].length){ //проверка, что мы не вышли за границы ибо
                        // хз в чём была проблема
                        if(lvlTiles[(int)unit.getPosition().getX() - i][(int)unit.getPosition().getY()] != Tile.EMPTY){
                            break;
                        }
                        if(lvlTiles[(int)unit.getPosition().getX() - i][(int)unit.getPosition().getY()] ==
                                lvlTiles[(int)nearestEnemy.getPosition().getX()][(int)unit.getPosition().getY()]){
                            return false;
                        }
                        i++;
                    }
                    else break;
                }
            } else if (nearestEnemy.getPosition().getX() > unit.getPosition().getX()){ //если враг справа (трейсинг вправо))
                while (true) {
                    if (unit.getPosition().getX() + i > 0 && unit.getPosition().getX() + i < lvlTiles[0].length){ //проверка, что мы не вышли за границы ибо
                        // хз в чём была проблема
                        if(lvlTiles[(int)unit.getPosition().getX() + i][(int)unit.getPosition().getY()] != Tile.EMPTY){
                            break;
                        }
                        if(lvlTiles[(int)unit.getPosition().getX() + i][(int)unit.getPosition().getY()] ==
                                lvlTiles[(int)nearestEnemy.getPosition().getX()][(int)unit.getPosition().getY()]){
                            return false;
                        }
                        i++;
                    }
                    else break;
                }
            }
        }
        return null;
    }


    /**
     * @param unit - наш персонаж
     * @param game - контекст игры
     * @return - объект храняющий действие игрока
     */
    public UnitAction getAction(Unit unit, Game game, Debug debug) {


        Unit nearestEnemy = null;
        for (Unit other : game.getUnits()) {
            if (other.getPlayerId() != unit.getPlayerId()) {
                if (nearestEnemy == null || distanceSqr(unit.getPosition(),
                        other.getPosition()) < distanceSqr(unit.getPosition(), nearestEnemy.getPosition())) {
                    nearestEnemy = other;
//                    System.out.println("nearestEnemy = other");
                }
            }
        }
        LootBox nearestWeapon = null;
        for (LootBox lootBox : game.getLootBoxes()) {
            if (lootBox.getItem() instanceof Item.Weapon) { // если лутбокс это оружие, то
                if (nearestWeapon == null || distanceSqr(unit.getPosition(),
                        lootBox.getPosition()) < distanceSqr(unit.getPosition(), nearestWeapon.getPosition())) {
                    nearestWeapon = lootBox;
                }
            }
        }

        Vec2Double targetPos = unit.getPosition(); // использовать только для aim-а
        if (unit.getWeapon() == null && nearestWeapon != null) {
            targetPos = nearestWeapon.getPosition(); // если нет оружия и вооружения, пиздуй к ним
        } else if (nearestEnemy != null) {
            targetPos = nearestEnemy.getPosition(); // если есть оружие, то пиздуй к врагу
        }
        targetPos = goToRocketIfCan(unit, game, targetPos);
        //спасайся


        debug.draw(new CustomData.Log("Target pos: " + targetPos));
        Vec2Float debugUnitPoint = new Vec2Float((float) unit.getPosition().getX(), (float) unit.getPosition().getY());

        UnitAction action = new UnitAction();



        double signum = Math.signum(targetPos.getX() - unit.getPosition().getX());
        action.setVelocity(signum * game.getProperties().getUnitMaxHorizontalSpeed());
        //дистанцируйся от врага
        backFromEnemy(nearestEnemy, unit, action, game.getProperties().getUnitMaxHorizontalSpeed());



        boolean jump = getJump(unit, game, nearestEnemy, targetPos);
        action.setJump(jump); // патч на прыжки
        jumpDownIfNeedIt(unit, game, targetPos, action);


        Vec2Double aim = new Vec2Double(0, 0);
        if (nearestEnemy != null) {
            aim = new Vec2Double(nearestEnemy.getPosition().getX() - 0.5 - unit.getPosition().getX(),
                    nearestEnemy.getPosition().getY() - unit.getPosition().getY());
            Vec2Float debugAimPoint = new Vec2Float((float) aim.getX(), (float) aim.getY());
            debug.draw(new CustomData.Line(debugUnitPoint, debugAimPoint, 0.2f, new ColorFloat(0, 100, 0, 100)));
        }
        //aimPatch(unit, nearestEnemy, aim, game); // конфликтует с проверкой тупости выстрела
        action.setAim(aim);


        if (isStupidShot(unit, nearestEnemy, game)) {
            action.setShoot(false);
        } else {
            action.setShoot(true);
        }

        weaponSwapping(unit, nearestEnemy, nearestWeapon, targetPos, action);

        //спасайся если хилый
        savePlayer(unit, game, action, nearestEnemy);
        action.setPlantMine(false);

        Vec2Float debugTargetPoint = new Vec2Float((float) targetPos.getX(), (float) targetPos.getY());
        debug.draw(new CustomData.Line(debugUnitPoint, debugTargetPoint, 0.2f, new ColorFloat(100, 0, 0, 100)));

        return action;
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
        if (distanceSqr(unit.getPosition(), nearestEnemy.getPosition()) <= 5) {
            jump = true;
        }
        return jump;
    }

    private void aimPatch(Unit unit, Unit nearestEnemy, Vec2Double aim, Game game) {
        if (nearestEnemy.isOnGround()) {
            aim.setY(nearestEnemy.getPosition().getY() - 1 - unit.getPosition().getY());
        }
    }


    private void weaponSwapping(Unit unit, Unit nearestEnemy, LootBox nearestWeapon, Vec2Double targetPos, UnitAction action) {
        if (unit.getWeapon() != null && unit.getWeapon().getTyp() == WeaponType.PISTOL) {
            action.setSwapWeapon(true);
        } else if (unit.getWeapon() != null && nearestWeapon.getPosition() != null && nearestEnemy.getHealth() >= MIN_ENEMY_HEALTH_FOR_SHOOTING_ROCKET &&
                unit.getWeapon().getTyp() == WeaponType.ASSAULT_RIFLE && iSearchRocket && (distanceSqr(unit.getPosition(), targetPos)) <= EPS) {
            action.setSwapWeapon(true);
        } else if (unit.getWeapon() != null && nearestEnemy.getHealth() <= MIN_ENEMY_HEALTH_FOR_CHANGE_ROCKET && unit.getWeapon().getTyp() == WeaponType.ROCKET_LAUNCHER) {
            if (nearestWeapon.getItem() instanceof Item.Weapon && ((Item.Weapon) nearestWeapon.getItem()).getWeaponType() == WeaponType.ASSAULT_RIFLE)
                action.setSwapWeapon(true);
        } else {
            action.setSwapWeapon(false);
        }
    }

    private void jumpDownIfNeedIt(Unit unit, Game game, Vec2Double targetPos, UnitAction action) {
        if (game.getLevel().getTiles()[(int) unit.getPosition().getX()][(int) unit.getPosition().getY() - 1] == Tile.PLATFORM &&
                targetPos.getY() < unit.getPosition().getY() && Math.abs((int) targetPos.getX() - (int) unit.getPosition().getX()) < 3) {
            action.setJumpDown(true);
        } else {
            action.setJumpDown(false);
        }
    }

    private void backFromEnemy(Unit nearestEnemy, Unit unit, UnitAction action, double speed) {

        Vec2Double enemyPosition = nearestEnemy.getPosition();
        Vec2Double unitPosition = unit.getPosition();

        if (distanceSqr(unitPosition, enemyPosition) <= 30 && nearestEnemy.getWeapon() != null) {
            if (enemyPosition.getX() > unitPosition.getX()) {// враг правее
                action.setVelocity(-1 * speed);
            } else if (enemyPosition.getX() < unitPosition.getX()) {//враг левее
                action.setVelocity(speed);
            }
        }
    }

    private void savePlayer(Unit unit, Game game, UnitAction action, Unit nearestEnemy) {
        double length = 100000;
        Vec2Double nearestHealPos = new Vec2Double(0,0);

        if (nearestEnemy != null && (nearestEnemy.getHealth() - unit.getHealth()) >= 30 || unit.getHealth() <= 40) {
            for (LootBox lootBox : game.getLootBoxes()) {
                if (lootBox.getItem() instanceof Item.HealthPack) { // если лутбокс это аптечка
                    final double distance = distanceSqr(lootBox.getPosition(), unit.getPosition());
                    if(length > distance){ // если у неё
                        length = distance;
                        nearestHealPos = lootBox.getPosition();
                    } // после этого имеем позицию ближайшей аптечки, и теперь её обрабатываем
                }
            }
            if(nearestHealPos.getX() < unit.getPosition().getX()){
                action.setVelocity(-1*game.getProperties().getUnitMaxHorizontalSpeed());
            }else {
                action.setVelocity(game.getProperties().getUnitMaxHorizontalSpeed());
            }
            if(nearestHealPos.getY()>unit.getPosition().getY()){
                action.setJump(true);
                action.setJumpDown(false);
            }else {
                action.setJump(false);
                action.setJumpDown(true);
            }
        }
    }

    private Vec2Double goToRocketIfCan(Unit unit, Game game, Vec2Double targetPos) {
        if (unit.getWeapon() != null && (unit.getWeapon().getTyp() == WeaponType.ASSAULT_RIFLE || unit.getWeapon().getTyp() == WeaponType.PISTOL) &&
                unit.getHealth() >= 65
        ) {
            iSearchRocket = true;
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