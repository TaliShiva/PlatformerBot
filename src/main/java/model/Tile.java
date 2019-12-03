package model;

public enum Tile {
    EMPTY(0),
    WALL(1),
    PLATFORM(2),
    LADDER(3),
    JUMP_PAD(4);
    public int discriminant;
    Tile(int discriminant) {
      this.discriminant = discriminant;
    }

    public void setMoveAction(MoveAction.state moveAction) {
        Tile.moveAction.setTileState(moveAction);
    }

    public MoveAction getMoveAction() {
        return moveAction;
    }

    private static MoveAction moveAction = new MoveAction();
}
