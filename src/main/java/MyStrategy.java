import model.*;

public class MyStrategy {
    private boolean iSearchRocket = false;
    private double EPS = 1;

    private static double distanceSqr(Vec2Double a, Vec2Double b) {
        return (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }

    private static boolean isStupidShot(Unit unit, Unit nearestEnemy, Game game) {

        if (unit.getWeapon() != null && distanceSqr(unit.getPosition(), nearestEnemy.getPosition()) > 5 &&
                unit.getWeapon() != null && nearestEnemy.getWeapon() != null) {
            return (game.getLevel().getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY())] == Tile.WALL) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY())] == Tile.WALL) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY())] == Tile.WALL) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY() - 1)] == Tile.WALL) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY() - 1)] == Tile.WALL) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY() + 1)] == Tile.WALL) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY() + 1)] == Tile.WALL) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX())][(int) (unit.getPosition().getY() + 1)] == Tile.WALL) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX())][(int) (unit.getPosition().getY() - 1)] == Tile.WALL);
        }
        return false;
    }

    Vec2Double getMoveToAspirine(){
    return new Vec2Double();
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
                    System.out.println("nearestEnemy = other");
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

        Vec2Double targetPos = unit.getPosition();
        if (unit.getWeapon() == null && nearestWeapon != null) {
            targetPos = nearestWeapon.getPosition(); // если нет оружия и вооружения, пиздуй к ним
        } else if (nearestEnemy != null) {
            targetPos = nearestEnemy.getPosition(); // если есть оружие, то пиздуй к врагу
        }

        getMoveToAspirine();
        targetPos = savePlayer(unit, game, nearestEnemy, targetPos);

        //беги за базукой если есть возможность
        targetPos = goToRocketIfCan(unit, game, targetPos);

        debug.draw(new CustomData.Log("Target pos: " + targetPos));
        Vec2Float debugUnitPoint = new Vec2Float((float) unit.getPosition().getX(), (float) unit.getPosition().getY());
        Vec2Float debugTargetPoint = new Vec2Float((float) targetPos.getX(), (float) targetPos.getY());
        debug.draw(new CustomData.Line(debugUnitPoint, debugTargetPoint, 0.2f, new ColorFloat(100, 0, 0, 100)));


        Vec2Double aim = new Vec2Double(0, 0);
        if (nearestEnemy != null) {
            aim = new Vec2Double(nearestEnemy.getPosition().getX() - unit.getPosition().getX(),
                    nearestEnemy.getPosition().getY() - unit.getPosition().getY());
            System.out.printf("aim x=%f;y=%f", aim.getX(), aim.getY());
        }


        boolean jump = targetPos.getY() > unit.getPosition().getY();
        if (targetPos.getX() > unit.getPosition().getX() && game.getLevel()
                .getTiles()[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY())] == Tile.WALL) {
            jump = true;
        }
        if (targetPos.getX() < unit.getPosition().getX() && game.getLevel()
                .getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY())] == Tile.WALL) {
            jump = true;
        }


        UnitAction action = new UnitAction();
        double signum = Math.signum(targetPos.getX() - unit.getPosition().getX());
        action.setVelocity(signum * game.getProperties().getUnitMaxHorizontalSpeed());
        action.setJump(jump);
        action.setJumpDown(!jump);

        action.setAim(aim);
        action.setShoot(true);

        if (isStupidShot(unit, nearestEnemy, game)) {
            action.setShoot(false);
        }
        if (unit.getWeapon() != null && unit.getWeapon().getTyp() == WeaponType.PISTOL) {
            action.setSwapWeapon(true);
        } else if (unit.getWeapon() != null && nearestWeapon.getPosition() != null &&
                unit.getWeapon().getTyp() == WeaponType.ASSAULT_RIFLE && iSearchRocket && (distanceSqr(unit.getPosition(), targetPos)) <= EPS) {
            action.setSwapWeapon(true);
        } else {
            action.setSwapWeapon(false);
        }

        action.setPlantMine(false);
        return action;
    }

    private Vec2Double savePlayer(Unit unit, Game game, Unit nearestEnemy, Vec2Double targetPos) {
        if (nearestEnemy != null && (nearestEnemy.getHealth() - unit.getHealth()) >= 23 || unit.getHealth() <= 20) {
            for (LootBox lootBox : game.getLootBoxes()) {
                if (lootBox.getItem() instanceof Item.HealthPack && distanceSqr(unit.getPosition(), lootBox.getPosition()) <= 30) { // если лутбокс это аптечка, то
                    targetPos = lootBox.getPosition();
                }
            }
        }
        return targetPos;
    }

    private Vec2Double goToRocketIfCan(Unit unit, Game game, Vec2Double targetPos) {
        if (unit.getWeapon() != null && (unit.getWeapon().getTyp() == WeaponType.ASSAULT_RIFLE || unit.getWeapon().getTyp() == WeaponType.PISTOL) &&
                unit.getHealth() >= 65
        ) {
            iSearchRocket = true;
            for (LootBox lootBox : game.getLootBoxes()) {
                Item item = lootBox.getItem();
                if (item instanceof Item.Weapon) { // если лутбокс это аптечка, то
                    if (((Item.Weapon) item).getWeaponType() == WeaponType.ROCKET_LAUNCHER)
                        targetPos = lootBox.getPosition();
                }
            }
        }
        return targetPos;
    }
}