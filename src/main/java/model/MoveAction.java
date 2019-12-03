package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoveAction {
    private List<state> tileStates = new ArrayList<>();

    public MoveAction(){
    }

    public MoveAction(state[] tileStates) {
        this.tileStates = Arrays.asList(tileStates);
    }

    public List getTileStates() {
        return tileStates;
    }

    public void setTileState(state tileStates) {
        this.tileStates.add(tileStates);
    }

    enum state {
        NONE(0),
        JUMP(1),
        JUMPDOWN(2),
        LEFTMOVE(3),
        RIGHTMOVE(4);
        public int discriminant;
        state(int discriminant) {
            this.discriminant = discriminant;
        }
    }
}
