package com.steveperkins.mediagallery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A wrapper for the media items currently loaded into the gallery, along with a cursor for tracking
 * the currently-rendered item.
 */
public class Gallery {

    private final List<GalleryItem> items;
    private int cursor;

    /**
     * Constructs a <code>Gallery</code> from a single media item.  If the item is <code>null</code>, then
     * the gallery will be empty and will need further populating via {@link Gallery#add(GalleryItem)} or
     * {@link Gallery#addAll(Collection)}.
     *
     * @param item
     */
    public Gallery(final GalleryItem item) {
        this.items = new ArrayList<>();
        if (item != null) this.items.add(item);
        this.cursor = 0;
    }

    /**
     * Constructs a <code>Gallery</code> from multiple media items.  Items that are <code>null</code> will
     * be stripped out.
     *
     * @param items
     */
    public Gallery(final Collection<GalleryItem> items) {
        this.items = new ArrayList<>();
        for (final GalleryItem item : items) {
            if (item != null) this.items.add(item);
        }
        this.cursor = 0;
    }

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
     * Moves the cursor state forward and returns the next item in the gallery (or <code>null</code> if the
     * gallery is empty).  When the cursor reaches the end, it is reset back to the beginning.
     *
     * @return
     */
    public GalleryItem next() {
        if (items.isEmpty()) return null;
        cursor = cursor + 1 < items.size() ? cursor + 1 : 0;
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
        return items.get(cursor);
    }

    /**
     * The current cursor position for the gallery.  Zero-indexed.
     *
     * @return
     */
    public int getCursor() {
        return cursor;
    }

    /**
     * The number of items in the gallery.
     *
     * @return
     */
    public int size() {
        return items.size();
    }

}
