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



}
