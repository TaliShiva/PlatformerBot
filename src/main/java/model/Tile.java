package model;

import java.util.ArrayList;
import java.util.List;
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

    private List<Tile> parents = new ArrayList<>();
    private ArrayList<Tile> neighbours = new ArrayList<>();//значит что из данной вершины ты можешь попасть только в эти

    public void setNeighbour(Tile neighbour) {
        if (neighbour != null)
            this.neighbours.add(neighbour);
    }

    public ArrayList<Tile> getNeighbours() {
        return neighbours;
    }

    public void setParent(Tile parent) {
        parents.add(parent);
    }
}
