package com.steveperkins.mediagallery;

import java.io.File;
import java.util.Arrays;

public class GalleryItem {

    public enum Type {
        IMAGE, VIDEO
    }

    private final File item;
    private final Type type;

    public GalleryItem(final File file, final Type type) {
        this.item = file;
        this.type = type;
    }

    public static GalleryItem create(final File file) {
        if (file == null) return null;
        if (file.getName().lastIndexOf('.') == -1 || file.getName().endsWith(".")) return null;
        final String ext = file.getName().substring(file.getName().lastIndexOf('.') + 1).toLowerCase();
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
