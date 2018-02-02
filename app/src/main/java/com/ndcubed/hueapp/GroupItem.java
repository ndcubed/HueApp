package com.ndcubed.hueapp;

import com.philips.lighting.model.PHGroup;

public class GroupItem {

    private PHGroup group;
    private int[] colors;

    public GroupItem(PHGroup group, int[] color) {
        this.group = group;
        this.colors = color;
    }

    public PHGroup getGroup() {
        return group;
    }

    public void setGroup(PHGroup group) {
        this.group = group;
    }

    public int[] getColors() {
        return colors;
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }
}
