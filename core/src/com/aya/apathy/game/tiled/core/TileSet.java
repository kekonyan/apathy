/*
 * Copyright 2004-2010, Thorbjørn Lindeijer <thorbjorn@lindeijer.nl>
 * Copyright 2004-2006, Adam Turk <aturk@biggeruniverse.com>
 *
 * This file is part of libtiled-java.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library;  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aya.apathy.game.tiled.core;

import com.aya.apathy.game.tiled.util.BasicTileCutter;
import com.aya.apathy.game.tiled.util.TileCutter;
import com.badlogic.gdx.math.Rectangle;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * todo: Update documentation
 * <p>
 * TileSet handles operations on tiles as a set, or group. It has several
 * advanced internal functions aimed at reducing unnecessary data replication. A
 * 'tile' is represented internally as two distinct pieces of data. The first
 * and most important is a {@link Tile} object, and these are held in a
 * {@link Vector}.
 * </p>
 * <p/>
 * <p>
 * The other is the tile image.
 * </p>
 */
public class TileSet implements Iterable<Tile> {
    private String base;
    final private Vector<Tile> tiles = new Vector<Tile>();
    private int firstGid;
    private long tilebmpFileLastModified;
    private TileCutter tileCutter;
    private Rectangle tileDimensions;
    private int tileSpacing;
    private int tileMargin;
    private int tilesPerRow;
    private String externalSource;
    private File tilebmpFile;
    private String name;
    private Integer transparentColor;
    private Object tileSetImage;

    /**
     * Default constructor
     */
    public TileSet() {
        tileDimensions = new Rectangle();
    }

    /**
     * Creates a tileset from a tileset image file.
     *
     * @param imgFilename
     * @param cutter
     * @throws IOException
     */
    public void importTileBitmap(String imgFilename, TileCutter cutter)
            throws IOException {
        setTilesetImageFilename(imgFilename);

        Object image = new Object();//BitmapFactory.decodeFile(imgFilename);
        if (image == null) {
            throw new IOException("Failed to load " + tilebmpFile);
        }

        if (transparentColor != null) {
            Logger.getLogger(getClass().getName())
                    .log(Level.WARNING,
                            "libtiled-android does not support transparent color, use alpha channel");
        }

        importTileBitmap(image, cutter);
    }

    /**
     * Creates a tileset from a buffered image. Tiles are cut by the passed
     * cutter.
     *
     * @param tileBitmap the image to be used, must not be null
     * @param cutter     the tile cutter, must not be null
     */
    private void importTileBitmap(Object tileBitmap, TileCutter cutter) {
        assert tileBitmap != null;
        assert cutter != null;

        tileCutter = cutter;
        tileSetImage = tileBitmap;

        cutter.setImage(tileBitmap);

        tileDimensions = new Rectangle(0,0,0,0 );
        if (cutter instanceof BasicTileCutter) {
            BasicTileCutter basicTileCutter = (BasicTileCutter) cutter;
            tileSpacing = basicTileCutter.getTileSpacing();
            tileMargin = basicTileCutter.getTileMargin();
            tilesPerRow = basicTileCutter.getTilesPerRow();
        }

        Object tileImage = new Object();//cutter.getNextTile();
        while (tileImage != null) {
            Tile tile = new Tile();
            tile.setImage(tileImage);
            addNewTile(tile);
            //tileImage = cutter.getNextTile();
        }
    }

    /**
     * Refreshes a tileset from a tileset image file.
     *
     * @throws IOException
     */
    private void refreshImportedTileBitmap() throws IOException {
        String imgFilename = tilebmpFile.getPath();

        Object image = new Object();//BitmapFactory.decodeFile(imgFilename);
        if (image == null) {
            throw new IOException("Failed to load " + tilebmpFile);
        }

        if (transparentColor != null) {
            Logger.getLogger(getClass().getName())
                    .log(Level.WARNING,
                            "libtiled-android does not support transparent color, use alpha channel");
        }

        refreshImportedTileBitmap(image);
    }

    /**
     * Refreshes a tileset from a buffered image. Tiles are cut by the passed
     * cutter.
     *
     * @param tileBitmap the image to be used, must not be null
     */
    private void refreshImportedTileBitmap(Object tileBitmap) {
        assert tileBitmap != null;

        tileCutter.reset();
        tileCutter.setImage(tileBitmap);

        tileSetImage = tileBitmap;
        tileDimensions = new Rectangle(0,0,0,0);

        int id = 0;
        Object tileImage = new Object();//tileCutter.getNextTile();
        while (tileImage != null) {
            Tile tile = getTile(id);
            tile.setImage(tileImage);
            //tileImage = tileCutter.getNextTile();
            id++;
        }
    }

    public void checkUpdate() throws IOException {
        if (tilebmpFile != null
                && tilebmpFile.lastModified() > tilebmpFileLastModified) {
            refreshImportedTileBitmap();
            tilebmpFileLastModified = tilebmpFile.lastModified();
        }
    }

    /**
     * Sets the URI path of the external source of this tile set. By setting
     * this, the set is implied to be external in all other operations.
     *
     * @param source a URI of the tileset image file
     */
    public void setSource(String source) {
        externalSource = source;
    }

    /**
     * Sets the base directory for the tileset
     *
     * @param base a String containing the native format directory
     */
    public void setBaseDir(String base) {
        this.base = base;
    }

    /**
     * Sets the filename of the tileset image. Doesn't change the tileset in any
     * other way.
     *
     * @param name
     */
    public void setTilesetImageFilename(String name) {
        if (name != null) {
            tilebmpFile = new File(name);
            tilebmpFileLastModified = tilebmpFile.lastModified();
        } else {
            tilebmpFile = null;
        }
    }

    /**
     * Sets the first global id used by this tileset.
     *
     * @param firstGid first global id
     */
    public void setFirstGid(int firstGid) {
        this.firstGid = firstGid;
    }

    /**
     * Sets the name of this tileset.
     *
     * @param name the new name for this tileset
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the transparent color in the tileset image.
     *
     * @param color
     */
    public void setTransparentColor(Integer color) {
        transparentColor = color;
    }

    /**
     * Adds the tile to the set, setting the id of the tile only if the current
     * value of id is -1.
     *
     * @param t the tile to add
     * @return int The <b>local</b> id of the tile
     */
    public int addTile(Tile t) {
        if (t.getId() < 0)
            t.setId(tiles.size());

        if (tileDimensions.width < t.getWidth())
            tileDimensions.width = t.getWidth();

        if (tileDimensions.height < t.getHeight())
            tileDimensions.height = t.getHeight();

        tiles.add(t);
        t.setTileSet(this);

        return t.getId();
    }

    /**
     * This method takes a new Tile object as argument, and in addition to the
     * functionality of <code>addTile()</code>, sets the id of the tile to -1.
     *
     * @param t the new tile to add.
     * @see TileSet#addTile(Tile)
     */
    public void addNewTile(Tile t) {
        t.setId(-1);
        addTile(t);
    }

    /**
     * Removes a tile from this tileset. Does not invalidate other tile indices.
     * Removal is simply setting the reference at the specified index to
     * <b>null</b>.
     *
     * @param i the index to remove
     */
    public void removeTile(int i) {
        tiles.set(i, null);
    }

    /**
     * Returns the amount of tiles in this tileset.
     *
     * @return the amount of tiles in this tileset
     */
    public int size() {
        return tiles.size();
    }

    /**
     * Returns the maximum tile id.
     *
     * @return the maximum tile id, or -1 when there are no tiles
     */
    public int getMaxTileId() {
        return tiles.size() - 1;
    }

    /**
     * Returns an iterator over the tiles in this tileset.
     *
     * @return an iterator over the tiles in this tileset.
     */
    public Iterator<Tile> iterator() {
        return tiles.iterator();
    }

    /**
     * Returns the width of tiles in this tileset. All tiles in a tileset should
     * be the same width, and the same as the tile width of the map the tileset
     * is used with.
     *
     * @return int - The maximum tile width
     */
    public float getTileWidth() {
        return tileDimensions.width;
    }

    /**
     * Returns the tile height of tiles in this tileset. Not all tiles in a
     * tileset are required to have the same height, but the height should be at
     * least the tile height of the map the tileset is used with.
     * <p/>
     * If there are tiles with varying heights in this tileset, the returned
     * height will be the maximum.
     *
     * @return the max height of the tiles in the set
     */
    public float getTileHeight() {
        return tileDimensions.height;
    }

    /**
     * Returns the spacing between the tiles on the tileset image.
     *
     * @return the spacing in pixels between the tiles on the tileset image
     */
    public int getTileSpacing() {
        return tileSpacing;
    }

    /**
     * Returns the margin around the tiles on the tileset image.
     *
     * @return the margin in pixels around the tiles on the tileset image
     */
    public int getTileMargin() {
        return tileMargin;
    }

    /**
     * Returns the number of tiles per row in the original tileset image.
     *
     * @return the number of tiles per row in the original tileset image.
     */
    public int getTilesPerRow() {
        return tilesPerRow;
    }

    /**
     * Gets the tile with <b>local</b> id <code>i</code>.
     *
     * @param i local id of tile
     * @return A tile with local id <code>i</code> or <code>null</code> if no
     * tile exists with that id
     */
    public Tile getTile(int i) {
        try {
            return tiles.get(i);
        } catch (ArrayIndexOutOfBoundsException a) {
        }
        return null;
    }

    /**
     * Returns the first non-null tile in the set.
     *
     * @return The first tile in this tileset, or <code>null</code> if none
     * exists.
     */
    public Tile getFirstTile() {
        Tile ret = null;
        int i = 0;
        while (ret == null && i <= getMaxTileId()) {
            ret = getTile(i);
            i++;
        }
        return ret;
    }

    /**
     * Returns the source of this tileset.
     *
     * @return a filename if tileset is external or <code>null</code> if tileset
     * is internal.
     */
    public String getSource() {
        return externalSource;
    }

    /**
     * Returns the base directory for the tileset
     *
     * @return a directory in native format as given in the tileset file or tag
     */
    public String getBaseDir() {
        return base;
    }

    /**
     * Returns the filename of the tileset image.
     *
     * @return the filename of the tileset image, or <code>null</code> if this
     * tileset doesn't reference a tileset image
     */
    public String getTilebmpFile() {
        if (tilebmpFile != null) {
            try {
                return tilebmpFile.getCanonicalPath();
            } catch (IOException e) {
            }
        }

        return null;
    }

    /**
     * Returns the first global id connected to this tileset.
     *
     * @return first global id
     */
    public int getFirstGid() {
        return firstGid;
    }

    /**
     * @return the name of this tileset.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the transparent color of the tileset image, or <code>null</code>
     * if none is set.
     *
     * @return Colour - The transparent color of the set
     */
    public Integer getTransparentColor() {
        return transparentColor;
    }

    /**
     * @return the name of the tileset, and the total tiles
     */
    public String toString() {
        return getName() + " [" + size() + "]";
    }

    // TILE IMAGE CODE

    /**
     * Returns whether the tileset is derived from a tileset image.
     *
     * @return tileSetImage != null
     */
    public boolean isSetFromImage() {
        return tileSetImage != null;
    }
}
