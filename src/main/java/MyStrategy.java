import model.*;

public class MyStrategy {
    private boolean iSearchRocket = false;
    private double EPS = 1;

    private static double distanceSqr(Vec2Double a, Vec2Double b) {
        return (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }

    private static boolean isStupidShot(Unit unit, Unit nearestEnemy, Game game) {
        if (Math.abs(unit.getPosition().getY() - nearestEnemy.getPosition().getY()) < 3) // если игроки примерно на одной выcоте
        {
            if (unit.getPosition().getX() < nearestEnemy.getPosition().getX()) { // если наш игрок левее и дистанция между нами больше двух тайлов и рядом
                // нет препятствий , то стреляем
                for (int i = (int) unit.getPosition().getX()+1; i < (int) nearestEnemy.getPosition().getX(); i++) {
                    if (game.getLevel().getTiles()[(int) (unit.getPosition().getX() + i)][(int) (unit.getPosition().getY())] == Tile.EMPTY) {
                        return false;
                    }
                }
            } else if (unit.getPosition().getX() > nearestEnemy.getPosition().getX()) {
                for (int i = (int) unit.getPosition().getX()-1; i > (int) nearestEnemy.getPosition().getX(); i--) {
                    if (game.getLevel().getTiles()[(int) (unit.getPosition().getX() - i)][(int) (unit.getPosition().getY())] == Tile.EMPTY) {
                        return false;
                    }
                }
            }
        }

        final double distanceBetweenPlayers = distanceSqr(unit.getPosition(), nearestEnemy.getPosition());
        if (unit.getWeapon() != null && unit.getWeapon().getTyp() == WeaponType.ROCKET_LAUNCHER && distanceBetweenPlayers > 25) {
            return (game.getLevel().getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY())] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY())] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY())] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY() - 1)] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY() - 1)] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY() + 1)] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY() + 1)] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX())][(int) (unit.getPosition().getY() + 1)] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX())][(int) (unit.getPosition().getY() - 1)] != Tile.EMPTY);
        }
        if (unit.getWeapon() != null && unit.getWeapon().getTyp() == WeaponType.ASSAULT_RIFLE && distanceBetweenPlayers > 25) {
            return (game.getLevel().getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY())] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY())] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY())] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY() - 1)] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY() - 1)] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() + 1)][(int) (unit.getPosition().getY() + 1)] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX() - 1)][(int) (unit.getPosition().getY() + 1)] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX())][(int) (unit.getPosition().getY() + 1)] != Tile.EMPTY) ||
                    (game.getLevel().getTiles()[(int) (unit.getPosition().getX())][(int) (unit.getPosition().getY() - 1)] != Tile.EMPTY);
        }


        return false;
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

        Vec2Double targetPos = unit.getPosition();
        if (unit.getWeapon() == null && nearestWeapon != null) {
            targetPos = nearestWeapon.getPosition(); // если нет оружия и вооружения, пиздуй к ним
        } else if (nearestEnemy != null) {
            targetPos = nearestEnemy.getPosition(); // если есть оружие, то пиздуй к врагу
        }


        //беги за базукой если есть возможность
        targetPos = goToRocketIfCan(unit, game, targetPos);
        //дистанцируйся от врага
        targetPos = backFromEnemy(targetPos, nearestEnemy, unit, game);
        //спасайся
        targetPos = savePlayer(unit, game, nearestEnemy, targetPos);

        debug.draw(new CustomData.Log("Target pos: " + targetPos));
        Vec2Float debugUnitPoint = new Vec2Float((float) unit.getPosition().getX(), (float) unit.getPosition().getY());


        Vec2Double aim = new Vec2Double(0, 0);
        if (nearestEnemy != null) {
            aim = new Vec2Double(nearestEnemy.getPosition().getX() - unit.getPosition().getX(),
                    nearestEnemy.getPosition().getY() - unit.getPosition().getY());
            Vec2Float debugAimPoint = new Vec2Float((float) aim.getX(), (float) aim.getY());
            debug.draw(new CustomData.Line(debugUnitPoint, debugAimPoint, 0.2f, new ColorFloat(0, 100, 0, 100)));
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
        if (distanceSqr(unit.getPosition(), nearestEnemy.getPosition()) <= 20) {
            jump = true;
        }


        UnitAction action = new UnitAction();
        double signum = Math.signum(targetPos.getX() - unit.getPosition().getX());
        action.setVelocity(signum * game.getProperties().getUnitMaxHorizontalSpeed());
        action.setJump(jump);

        // патч на прыжки, если цель снизу
        jumpDownIfNeedIt(unit, game, targetPos, action);

        action.setAim(aim);
        action.setShoot(true);

        if (isStupidShot(unit, nearestEnemy, game)) {
            action.setShoot(false);
        }

        weaponSwapping(unit, nearestWeapon, targetPos, action);

        action.setPlantMine(false);

        Vec2Float debugTargetPoint = new Vec2Float((float) targetPos.getX(), (float) targetPos.getY());
        debug.draw(new CustomData.Line(debugUnitPoint, debugTargetPoint, 0.2f, new ColorFloat(100, 0, 0, 100)));

        return action;
    }

    private void weaponSwapping(Unit unit, LootBox nearestWeapon, Vec2Double targetPos, UnitAction action) {
        if (unit.getWeapon() != null && unit.getWeapon().getTyp() == WeaponType.PISTOL) {
            action.setSwapWeapon(true);
        } else if (unit.getWeapon() != null && nearestWeapon.getPosition() != null &&
                unit.getWeapon().getTyp() == WeaponType.ASSAULT_RIFLE && iSearchRocket && (distanceSqr(unit.getPosition(), targetPos)) <= EPS) {
            action.setSwapWeapon(true);
        } else {
            action.setSwapWeapon(false);
        }
    }

    private void jumpDownIfNeedIt(Unit unit, Game game, Vec2Double targetPos, UnitAction action) {
        if (game.getLevel().getTiles()[(int) unit.getPosition().getX()][(int) unit.getPosition().getY() - 1] == Tile.PLATFORM &&
                targetPos.getY() < unit.getPosition().getY()) {
            action.setJumpDown(true);
        } else {
            action.setJumpDown(false);
        }
    }

    private Vec2Double backFromEnemy(Vec2Double targetPos, Unit nearestEnemy, Unit unit, Game game) {
        Vec2Double enemyPosition = nearestEnemy.getPosition();
        Vec2Double unitPosition = unit.getPosition();
        if (distanceSqr(unitPosition, enemyPosition) <= 19) {
            if (enemyPosition.getX() > unitPosition.getX()) {// враг правее
                targetPos.setX(enemyPosition.getX() - 39);
            } else if (enemyPosition.getX() < unitPosition.getX()) {//враг левее
                targetPos.setX(enemyPosition.getX() + 39);
            }
        }
        return targetPos;
    }

    private Vec2Double savePlayer(Unit unit, Game game, Unit nearestEnemy, Vec2Double targetPos) {
        if (nearestEnemy != null && (nearestEnemy.getHealth() - unit.getHealth()) >= 23 || unit.getHealth() <= 30) {
            for (LootBox lootBox : game.getLootBoxes()) {
                if ((lootBox.getItem() instanceof Item.HealthPack) && distanceSqr(unit.getPosition(), lootBox.getPosition()) <= 55) { // если лутбокс это аптечка и она
                    // относительно недалеко , то бежим к ней
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