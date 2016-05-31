package com.steveperkins.mediagallery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Gallery {

    private final List<GalleryItem> items;
    private int cursor;

    public Gallery(final GalleryItem item) {
        this.items = new ArrayList<>();
        this.items.add(item);
        this.cursor = 0;
    }

    public Gallery(final Collection<GalleryItem> items) {
        this.items = new ArrayList<>();
        this.items.addAll(items);
        this.cursor = 0;
    }

    public void add(final GalleryItem item) {
        if (!items.contains(item)) {
            items.add(item);
            if (items.size() == 1) {
                cursor = 0;
            }
        }
    }

    public void addAll(final Collection<GalleryItem> items) {
        items.stream().forEach(item -> add(item));
    }

    public GalleryItem next() {
        if (items.isEmpty()) return null;
        final GalleryItem returnValue = items.get(cursor);
        cursor = cursor + 1 < items.size() ? cursor + 1 : 0;
        return returnValue;
    }

    public GalleryItem previous() {
        if (items.isEmpty()) return null;
        final GalleryItem returnValue = items.get(cursor);
        cursor = cursor > 0 ? cursor - 1 : items.size() - 1;
        return returnValue;
    }

    public int getCursor() {
        return cursor;
    }

    public int size() {
        return items.size();
    }

}
