package parser;

import java.util.*;

public class State implements Comparable<State> {
    private Set<Item> itemSet;
    private List<Item> items;
    private int name;

    public State(int name) {
        this.itemSet = new HashSet<>();
        this.items = new ArrayList<>();
        this.name = name;
    }

    public State() {
        this(0);
    }

    public void setName(int name) {
        this.name = name;
    }

    public int size() {
        return items.size();
    }

    public boolean addItem(Item item) {
        if (!items.contains(item)) {
            items.add(item);
            itemSet.add(item);
            return true;
        }
        return false;
    }

    public Item getItem(int pos) {
        if (pos < items.size()) {
            return items.get(pos);
        }
        return null;
    }

    public boolean addState(State other) {
        boolean added = false;
        for (int i = 0; i < other.size(); i++) {
            Item currentItem = other.getItem(i);
            added = added || this.addItem(currentItem);
        }
        return added;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        ArrayList<Item> sortedList = new ArrayList<>(items);
        sortedList.sort(Comparator.comparingInt(Item::hashCode));
        for (Item item : sortedList) {
            hash = 37 * hash + Objects.hashCode(item);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final State other = (State) obj;
        if (items.size() != other.items.size()) {
            return false;
        }
        for (Item item : items) {
            if (!other.itemSet.contains(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return this.name + ": " + items.toString();
    }

    @Override
    public int compareTo(State o) {
        return new Integer(this.name).compareTo(new Integer(o.name));
    }

}
