package com.steveperkins.mediagallery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Gallery {

    private final List<GalleryItem> items;
    private int cursor;

    public Gallery(final GalleryItem item) {
        this.items = new ArrayList<>();
        if (item != null) this.items.add(item);
        this.cursor = 0;
    }

    public Gallery(final Collection<GalleryItem> items) {
        this.items = new ArrayList<>();
        for (final GalleryItem item : items) {
            if (item != null) this.items.add(item);
        }
        this.cursor = 0;
    }

    public void add(final GalleryItem item) {
        if (item != null && !items.contains(item)) {
            items.add(item);
            if (items.size() == 1) {
                cursor = 0;
            }
        }
    }

    public void addAll(final Collection<GalleryItem> items) {
        items.stream().forEach(this::add);
    }

    public GalleryItem next() {
        if (items.isEmpty()) return null;
        cursor = cursor + 1 < items.size() ? cursor + 1 : 0;
        return items.get(cursor);
    }

    public GalleryItem previous() {
        if (items.isEmpty()) return null;
        cursor = cursor > 0 ? cursor - 1 : items.size() - 1;
        return items.get(cursor);
    }

    public int getCursor() {
        return cursor;
    }

    public int size() {
        return items.size();
    }

}
