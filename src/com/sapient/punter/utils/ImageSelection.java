package com.sapient.punter.utils;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class ImageSelection implements Transferable {
    private Image image;

    public static void copyImageToClipboard(Image image) {
        ImageSelection imageSelection = new ImageSelection(image);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.getSystemClipboard().setContents(imageSelection, null);
    }

    public ImageSelection(Image image) {
        this.image = image;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(DataFlavor.imageFlavor) == false) {
            throw new UnsupportedFlavorException(flavor);
        }
        return image;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DataFlavor.imageFlavor);
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{
                DataFlavor.imageFlavor
        };
    }
}
