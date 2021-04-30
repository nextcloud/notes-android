package it.niedermann.owncloud.notes.shared.model;

public interface Item {
    default boolean isSection() {
        return false;
    }
}
