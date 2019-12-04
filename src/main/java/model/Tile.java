package model;

import java.util.Set;
import java.util.TreeSet;

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

    public void setNeighbours(Set<Tile> neighbours) {
        this.neighbours = neighbours;
    }

    public void setNeighbour(Tile neighbour) {
        if (neighbour != null)
            this.neighbours.add(neighbour);
    }

    public Set<Tile> getNeighbours() {
        return neighbours;
    }

    private Set<Tile> neighbours = new TreeSet<>();
}
