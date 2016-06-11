package com.steveperkins.mediagallery;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A wrapper for the media items currently loaded into the gallery, along with a cursor for tracking
 * the currently-rendered item.
 */
public class Gallery {

    private final List<GalleryItem> items = new ArrayList<>();
    private int cursor = -1;
    private final ReadOnlyStringWrapper statusProperty = new ReadOnlyStringWrapper("No file selected");

    /**
     * Appends a new media item to the gallery, if the item is non-null and isn't already included.
     *
     * @param item
     */
    public void add(final GalleryItem item) {
        if (item != null && !items.contains(item)) {
            items.add(item);
            if (items.size() == 1) {
                cursor = 0;
            }
            statusProperty.set((cursor + 1) + " of " + items.size());
        }
    }

    /**
     * Appends multiple media items to the gallery, if they are non-null and not already included.
     *
     * @param items
     */
    public void addAll(final Collection<GalleryItem> items) {
        items.stream().forEach(this::add);
    }

    /**
     * Removes all items from the gallery.
     */
    public void clear() {
        items.clear();
        cursor = -1;
        statusProperty.set("No file selected");
    }

    /**
     * Moves the cursor state forward and returns the next item in the gallery (or <code>null</code> if the
     * gallery is empty).  When the cursor reaches the end, it is reset back to the beginning.
     *
     * @return
     */
    public GalleryItem next() {
        if (items.isEmpty()) return null;
        cursor = cursor + 1 < items.size() ? cursor + 1 : 0;
        statusProperty.set((cursor + 1) + " of " + items.size());
        return items.get(cursor);
    }

    /**
     * Moves the cursor state backwards and returns the next item in the gallery (or <code>null</code> if the
     * gallery is empty).  When the cursor reaches the beginning, it is scrolled back around to the end.
     *
     * @return
     */
    public GalleryItem previous() {
        if (items.isEmpty()) return null;
        cursor = cursor > 0 ? cursor - 1 : items.size() - 1;
        statusProperty.set((cursor + 1) + " of " + items.size());
        return items.get(cursor);
    }

    /**
     * Moves the cursor state to the first item in the gallery, and returns that item (or <code>null</code> if
     * the gallery is empty).
     *
     * @return
     */
    public GalleryItem first() {
        if (items.isEmpty()) return null;
        cursor = 0;
        statusProperty.set((cursor + 1) + " of " + items.size());
        return items.get(cursor);
    }

    /**
     * Moves the cursor state to the last item in the gallery, and returns that item (or <code>null</code> if
     * the gallery is empty).
     *
     * @return
     */
    public GalleryItem last() {
        if (items.isEmpty()) return null;
        cursor = items.size() - 1;
        statusProperty.set((cursor + 1) + " of " + items.size());
        return items.get(cursor);
    }

    public ReadOnlyStringProperty statusProperty() {
        return statusProperty.getReadOnlyProperty();
    }

    /**
     * Whether or not the gallery is empty.
     * @return
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public File directory() {
        return items.isEmpty() ? null : items.get(0).getItem().getParentFile();
    }
}
