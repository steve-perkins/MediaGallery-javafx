package com.steveperkins.mediagallery;

import java.io.File;
import java.util.Arrays;

/**
 * A wrapper for a supported media file.  Includes a file reference and type identifier (i.e. image or video).
 */
public class GalleryItem {

    public enum Type {
        IMAGE, VIDEO
    }

    private final File item;
    private final Type type;

    /**
     * <p>A private constructor, which assumes that the file is already known to be a supported media type.</p>
     *
     * <p>Use {@link GalleryItem#create(File)}.</p>
     *
     * @param file
     * @param type
     */
    private GalleryItem(final File file, final Type type) {
        this.item = file;
        this.type = type;
    }

    /**
     * Check whether a file is of a supported media type, and returns either a <code>GalleryItem</code> instance
     * or else <code>null</code> if the file is unsupported.
     *
     * @param file
     * @return
     */
    public static GalleryItem create(final File file) {
        if (file == null) return null;
        if (file.getName().lastIndexOf('.') == -1 || file.getName().endsWith(".")) return null;
        final String ext = file.getName().substring(file.getName().lastIndexOf('.') + 1).toLowerCase();
        // TODO: Add support for video files
        return file.isFile() && Arrays.asList("bmp", "gif", "jpg", "png").contains(ext)
                ? new GalleryItem(file, Type.IMAGE)
                : null;
    }

    public File getItem() {
        return this.item;
    }

    public boolean isImage() {
        return Type.IMAGE.equals(type);
    }

    public boolean isVideo() {
        return Type.VIDEO.equals(type);
    }
}
