package parser;

import java.util.*;

public class States {
    private Set<State> stateSet;
    private List<State> states;
    private int id = 0;

    public States() {
        this.stateSet = new HashSet<>();
        this.states = new ArrayList<>();
    }

    public State getState(int name) {
        if (name >= states.size()) {
            return null;
        }
        return states.get(name);
    }

    public int size() {
       return states.size();
    }

    public boolean addState(State state) {
        if (states.size() == 0) {
            state.setName(states.size());
            states.add(state);
            return true;
        }
//        for (State stateList: states) {
//            if (!stateList.equals(state)) {
//                state.setName(states.size());
//                states.add(state);
//                return true;
//            }
//        }
//        return false;
        boolean exists = false;
        for (int i = 0; i < states.size(); i++) {
            if (state.equals(states.get(i))) {
                exists = true;
            }
        }
        if (!exists) {
            state.setName(states.size());
            states.add(state);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return states.toString();
    }

}
