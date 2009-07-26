/*
 * Crossfire Java Editor.
 * Copyright (C) 2000  Michael Toennies
 * Copyright (C) 2001  Andreas Vogl
 *
 * (code based on: Gridder. 2D grid based level editor. (C) 2000  Pasi Keranen)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 */

package cfeditor;

import java.io.*;

/**
 * MapArchObject contains the specific data about a map that is stored
 * in the map-arch, at the very beginning of the map file. (width,
 * height, difficulty level... etc) In former days, this used to be
 * put into an ordinary ArchObject, but that's just no longer
 * appropriate.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class MapArchObject {
    // tags which appear in the map header
    private static final String TAG_START_TEXT = "msg";
    private static final String TAG_END_TEXT   = "endmsg";
    private static final String TAG_START_LORE = "maplore";
    private static final String TAG_END_LORE   = "endmaplore";

    // map attributes:
    private StringBuffer msgText = new StringBuffer(""); // map-msg text buffer
    private StringBuffer loreText = new StringBuffer(""); // lore text buffer
    private int width, height;       // map size
    private int enter_x, enter_y;    // default enter coordinates (usage not recommended)
    private int reset_timeout;       // number of seconds that need to elapse
                                     // before this map will be reset.
    private int swap_time;           // number of ticks that must elapse after the
                                     // map has not been used before it gets swapped out.
    private int difficulty;          // map difficulty. If null, server calculates something
    private boolean fixed_resettime; // If nonzero, the map reset time will not
                                     // be updated when someone enters/exits the map.
    private int darkness;            // light/darnkess of map (overall). Zero means fully bright
    private boolean unique;          // if set, this entire map is unique.
    private boolean outdoor;         // if set, this is an outdoor map.

    // Weather variables:
    private int temp;                // temperature
    private int pressure;            // pressure
    private int humid;               // humidity (water in the air)
    private int windspeed;           // wind speed
    private int winddir;             // wind direction
    private int sky;                 // sky settings

    // only for iso:
    private boolean no_save;        // no save map
    private boolean no_magic;        // no magic spells
    private boolean no_priest;       // no prayers
    private boolean no_harm;         // no harmful spells allowed
    private boolean no_summon;       // no summoning
    private boolean fixed_login;     // check map reset status after re-login
    private boolean perm_death;      // permanent death with revivable corpses
    private boolean ultra_death;     // permanent death with corpses temporarily available
    private boolean ultimate_death;  // permanent death with instant character deletion
    private boolean pvp;             // PVP combat allowed
	private boolean plugins;		 // Global map plugins are allowed

    private String name;             // map name (this is the name that appears in the game)
	private String background_music; // Music of the map
    private String filename;         // name of the map file
    private String[] tile_path = new String[8];  // Used with map tiling.  0=north, 1=east, 2=south, 3=west

    /**
     * Constructor, set default values
     */
    MapArchObject () {
        width=0; height=0; enter_x=0; enter_y=0;
        reset_timeout=0; swap_time=0; difficulty=0; darkness=-1;
        for (int i=0; i<8; i++) tile_path[i]="";
        name = "<untitled>"; // map creation enforces setting a real map name
		background_music = "";
        filename = IGUIConstants.DEF_MAPFNAME;  // default map file name

        fixed_resettime = false;
        unique = false;
        outdoor = false;

        // iso flags
        no_save = false;
        no_magic = false; no_priest = false;
        no_summon = false; fixed_login = false;
        perm_death = false; ultra_death = false;
        ultimate_death = false; pvp = false;
		plugins = false;
    }

    // get/set attributes
    public int getWidth() {return width;}
    public void setWidth(int x) {width=x;}
    public int getHeight() {return height;}
    public void setHeight(int y) {height=y;}
    public String getMapName() {return name;}
	public String getBackgroundMusic() {return background_music;}
    public void setMapName(String new_name) {name=new_name;}
	public void setBackgroundMusic(String new_background_music) {background_music = new_background_music;}
    public String getFileName() {return filename;}
    public void setFileName(String new_name) {filename=new_name;}
    public int getEnterX() {return enter_x;}
    public void setEnterX(int x) {enter_x=x;}
    public int getEnterY() {return enter_y;}
    public void setEnterY(int y) {enter_y=y;}
    public int getResetTimeout() {return reset_timeout;}
    public void setResetTimeout(int n) {reset_timeout=n;}
    public int getSwapTime() {return swap_time;}
    public void setSwapTime(int n) {swap_time=n;}
    public int getDifficulty() {return difficulty;}
    public void setDifficulty(int n) {difficulty=n;}
    public boolean isFixedReset() {return fixed_resettime;}
    public void setFixedReset(boolean b) {fixed_resettime=b;}
    public int getDarkness() {return darkness;}
    public void setDarkness(int n) {darkness=n;}
    public boolean isUnique() {return unique;}
    public void setUnique(boolean b) {unique=b;}
    public boolean isOutdoor() {return outdoor;}
    public void setOutdoor(boolean b) {outdoor=b;}
    public String getTilePath(int n) {return tile_path[n];}
    public void setTilePath(int n, String s) {tile_path[n]=s;}

    public int getTemp() {return temp;}
    public void setTemp(int t) {temp=t;}
    public int getPressure() {return pressure;}
    public void setPressure(int t) {pressure=t;}
    public int getHumid() {return humid;}
    public void setHumid(int t) {humid=t;}
    public int getWindspeed() {return windspeed;}
    public void setWindspeed(int t) {windspeed=t;}
    public int getWinddir() {return winddir;}
    public void setWinddir(int t) {winddir=t;}
    public int getSky() {return sky;}
    public void setSky(int t) {sky=t;}

    public boolean isNoSave() {return no_save;}
    public void setNoSave(boolean b) {no_save = b;}
    public boolean isNoMagic() {return no_magic;}
    public void setNoMagic(boolean b) {no_magic = b;}
    public boolean isNoPriest() {return no_priest;}
    public void setNoPriest(boolean b) {no_priest = b;}
    public boolean isNoSummon() {return no_summon;}
    public void setNoSummon(boolean b) {no_summon = b;}
    public boolean isNoHarm() {return no_harm;}
    public void setNoHarm(boolean b) {no_harm = b;}
    public boolean isFixedLogin() {return fixed_login;}
    public void setFixedLogin(boolean b) {fixed_login = b;}
    public boolean isPermDeath() {return perm_death;}
    public void setPermDeath(boolean b) {perm_death = b;}
    public boolean isUltraDeath() {return ultra_death;}
    public void setUltraDeath(boolean b) {ultra_death = b;}
    public boolean isUltimateDeath() {return ultimate_death;}
    public void setUltimateDeath(boolean b) {ultimate_death = b;}
    public boolean isPvp() {return pvp;}
    public void setPvp(boolean b) {pvp = b;}
	public boolean isPlugins() {return plugins;}
    public void setPlugins(boolean b) {plugins = b;}

    /**
     * append 'text' to the map text
     * @param text    string to add
     */
    public void addText(String text) {
        msgText.append(text);
    }

    /**
     * set 'text' = maptext
     * @param text    string to set
     */
    public void setText(String text) {
        msgText.delete(0, msgText.length());
        msgText.append(text);
    }

    /**
     * delete maptext
     */
    public void resetText() {
        msgText.delete(0, msgText.length());
    }

    /**
     * @return the maptext
     */
    public String getText() {
        return(msgText.toString());
    }

    /**
     * set 'text' = lore
     * @param text    string to set
     */
    public void setLore(String text) {
        loreText.delete(0, loreText.length());
        loreText.append(text);
    }

    /**
     * @return the map lore text
     */
    public String getLore() {
        return(loreText.toString());
    }

    /**
     * @return true if the mapsize is zero
     */
    public boolean size_null() {
        if (width<=0 && height<=0)
            return true;
        return false;
    }

    /**
     * Parsing the MapArchObject. This special object has it's own parser
     * now because it must be easily expandable for possible features. Who knows
     * how much information this Object might contain in future?
     *
     * @param reader     <code>BufferedReader</code> to the mapfile
     * @param fname      file name of the mapfile (relative name, no path)
     * @return true if reading the MapArchObject succeeded with sane
     *         results, otherwise false
     */
    public boolean parseMapArch (BufferedReader reader, String fname) {
        String line;       // input line
        int i;
        boolean msgflag = false;     // flag for map-message
        boolean loreflag = false;    // flag for lore-text
        boolean archflag = false;    // flag for arch<->end
        boolean end_reached = false; // true when end of maparch is reached

        filename = fname;  // store file name

        try {
            // read lines
            while((!end_reached && (line = reader.readLine()) != null)) {
                line = line.trim();

                if (archflag) {
                    // we are inside the map arch
                    if (msgflag) {
                        // reading the map message:
                        if (line.equalsIgnoreCase(TAG_END_TEXT)) {
                            msgflag = false;
                        }
                        else {
                            if (msgText.length() > 0)
                                msgText.append("\n");
                            msgText.append(line);
                        }
                    }
                    else if (loreflag) {
                        // reading lore text:
                        if (line.equalsIgnoreCase(TAG_END_LORE)) {
                            loreflag = false;
                        }
                        else {
                            if (loreText.length() > 0)
                                loreText.append("\n");
                            loreText.append(line);
                        }
                    }
                    else {
                        // inside map arch, outside message
                        if (line.equalsIgnoreCase(TAG_START_TEXT)) {
                            msgflag = true;
                        }
                        if (line.equalsIgnoreCase(TAG_START_LORE)) {
                            loreflag = true;
                        }
                        else if (line.equalsIgnoreCase("end")) {
                            end_reached = true;
                        }
                        else if (line.startsWith("name"))
                            name = line.substring(line.indexOf(" ")+1).trim();
						else if (line.startsWith("bg_music"))
                            background_music = line.substring(line.indexOf(" ")+1).trim();
                        else if (line.startsWith("width") || line.startsWith("x "))
                            width = getLineValue(line);
                        else if (line.startsWith("height") || line.startsWith("y "))
                            height = getLineValue(line);
                        else if (line.startsWith("enter_x") || line.startsWith("hp"))
                            enter_x = getLineValue(line);
                        else if (line.startsWith("enter_y") || line.startsWith("sp"))
                            enter_y = getLineValue(line);
                        else if (line.startsWith("reset_timeout") || line.startsWith("weight"))
                            reset_timeout = getLineValue(line);
                        else if (line.startsWith("swap_time") || line.startsWith("value"))
                            swap_time = getLineValue(line);
                        else if (line.startsWith("difficulty") || line.startsWith("level"))
                            difficulty = getLineValue(line);
                        else if (line.startsWith("darkness") || line.startsWith("invisible"))
                            darkness = getLineValue(line);
                        else if (line.startsWith("fixed_resettime") || line.startsWith("stand_still")) {
                            if (getLineValue(line) != 0)
                                fixed_resettime = true;
                        }
                        else if (line.startsWith("unique")) {
                            if (getLineValue(line) != 0)
                                unique = true;
                        }
                        else if (line.startsWith("outdoor")) {
                            if (getLineValue(line) != 0)
                                outdoor = true;
                        }
                        else if (line.startsWith("tile_path_")) {
                            // get tile path
                            try {
                                i = Integer.valueOf(line.substring(10, 11)).intValue(); // direction
                                if (i > 0 && i < 9 && line.lastIndexOf(" ") > 0)
                                    tile_path[i-1] = line.substring(line.lastIndexOf(" ")+1);
                            }
                            catch (NumberFormatException e) {}
                        }
                        else if (line.startsWith("temp"))
                            temp = getLineValue(line);
                        else if (line.startsWith("pressure"))
                            pressure = getLineValue(line);
                        else if (line.startsWith("humid"))
                            humid = getLineValue(line);
                        else if (line.startsWith("windspeed"))
                            windspeed = getLineValue(line);
                        else if (line.startsWith("winddir"))
                            winddir = getLineValue(line);
                        else if (line.startsWith("sky"))
                            sky = getLineValue(line);
                          else if (IGUIConstants.isoView && line.startsWith("no_save")) {
                              if (getLineValue(line) != 0) no_save = true;
                          }
                          else if (IGUIConstants.isoView && line.startsWith("no_magic")) {
                              if (getLineValue(line) != 0) no_magic = true;
                          }
                        else if (IGUIConstants.isoView && line.startsWith("no_priest")) {
                            if (getLineValue(line) != 0) no_priest = true;
                        }
                        else if (IGUIConstants.isoView && line.startsWith("no_summon")) {
                            if (getLineValue(line) != 0) no_summon = true;
                        }
                        else if (IGUIConstants.isoView && line.startsWith("no_harm")) {
                            if (getLineValue(line) != 0) no_harm = true;
                        }
                        else if (IGUIConstants.isoView && line.startsWith("fixed_login")) {
                            if (getLineValue(line) != 0) fixed_login = true;
                        }
                        else if (IGUIConstants.isoView && line.startsWith("perm_death")) {
                            if (getLineValue(line) != 0) perm_death = true;
                        }
                        else if (IGUIConstants.isoView && line.startsWith("ultra_death")) {
                            if (getLineValue(line) != 0) ultra_death = true;
                        }
                        else if (IGUIConstants.isoView && line.startsWith("ultimate_death")) {
                            if (getLineValue(line) != 0) ultimate_death = true;
                        }
                        else if (IGUIConstants.isoView && line.startsWith("pvp")) {
                            if (getLineValue(line) != 0) pvp = true;
                        }
						else if (IGUIConstants.isoView && line.startsWith("plugins")) {
                            if (getLineValue(line) != 0) plugins = true;
                        }
                    }
                }
                else {
                    // looking for the map arch
                    if (line.toLowerCase().regionMatches(0,"arch ", 0, 5) &&
                        line.toLowerCase().endsWith(" map"))
                        archflag = true;
                }

            }
        }
        catch (IOException e) {
            // if we reach eof here, the mapfile is corrupt
            return false;
        }

        if (archflag)
            return true;   // everything okay
        else
            return false;  // no map arch found
    }

    /**
     * Get the value from an arch entry (A line of the format
     * "&lt;attribute_name&gt; &lt;value&gt;").
     *
     * @param s      the attribute line to be parsed
     * @return attribute value, zero if value not readable
     */
    private int getLineValue(String s) {
        String tmp;

        try {
            if (s.lastIndexOf(" ") > 0) {
                return Integer.valueOf(s.substring(s.lastIndexOf(" ")+1)).intValue();
            }
        }
        catch (NumberFormatException e) {}

        return 0;
    }

    /**
     * Writing the MapArchObject into the mapfile
     *
     * @param stream                    <code>BufferedWriter</code> to the mapfile
     * @throws FileNotFoundException    the mapfile could not be found
     * @throws IOException              an I/O error occured during writing to the file
     */
    public void writeMapArch (BufferedWriter stream) throws FileNotFoundException,
                                                            IOException {
        int i;

        stream.write("arch map\n");
        if (name.length() > 0)
            stream.write("name "+name+"\n");

		if (background_music.length() > 0)
            stream.write("bg_music "+background_music+"\n");

        // maptext
        stream.write(TAG_START_TEXT+"\n");
        stream.write(msgText.toString().trim()+"\n");
        stream.write(TAG_END_TEXT+"\n");

        // lore
        if (loreText.length() > 0 && loreText.toString().trim().length() > 0 && !IGUIConstants.isoView) {
            stream.write(TAG_START_LORE+"\n");
            stream.write(loreText.toString().trim()+"\n");
            stream.write(TAG_END_LORE+"\n");
        }

        if (width > 0)
            stream.write("width "+width+"\n");
        if (height > 0)
            stream.write("height "+height+"\n");

        if (enter_x > 0)
            stream.write("enter_x "+enter_x+"\n");
        if (enter_y > 0)
            stream.write("enter_y "+enter_y+"\n");

        if (reset_timeout > 0)
            stream.write("reset_timeout "+reset_timeout+"\n");
        if (swap_time > 0)
            stream.write("swap_time "+swap_time+"\n");
        if (difficulty > 0)
            stream.write("difficulty "+difficulty+"\n");
        if (darkness >= 0)
            stream.write("darkness "+darkness+"\n");
        if (fixed_resettime)
            stream.write("fixed_resettime 1\n");
        if (unique)
            stream.write("unique 1\n");
        if (outdoor)
            stream.write("outdoor 1\n");
        if (temp != 0)
            stream.write("temp "+temp+"\n");
        if (pressure != 0)
            stream.write("pressure "+pressure+"\n");
        if (humid != 0)
            stream.write("humid "+humid+"\n");
        if (windspeed != 0)
            stream.write("windspeed "+windspeed+"\n");
        if (winddir != 0)
            stream.write("winddir "+winddir+"\n");
        if (sky != 0)
            stream.write("sky "+sky+"\n");

          if (no_save)
              stream.write("no_save 1\n");
            if (no_magic)
                stream.write("no_magic 1\n");
        if (no_priest)
            stream.write("no_priest 1\n");
        if (no_summon)
            stream.write("no_summon 1\n");
        if (no_harm)
            stream.write("no_harm 1\n");
        if (fixed_login)
            stream.write("fixed_login 1\n");
        if (perm_death)
            stream.write("perm_death 1\n");
        if (ultra_death)
            stream.write("ultra_death 1\n");
        if (ultimate_death)
            stream.write("ultimate_death 1\n");
        if (pvp)
            stream.write("pvp 1\n");
		if (plugins)
			stream.write("plugins 1\n");

        // tile_path
        for (i=0; i<8; i++) {
            if (tile_path[i] != null && tile_path[i].length() > 0)
                stream.write("tile_path_"+(i+1)+" "+tile_path[i]+"\n");
        }

        // end
        stream.write("end\n");
    }
}
