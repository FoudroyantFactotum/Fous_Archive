/*
 * Copyright (c) 2016 Foudroyant Factotum
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.foudroyantfactotum.mod.fousarchive.blocks.Structure.PlayerPiano;

import com.foudroyantfactotum.mod.fousarchive.blocks.Structure.TEStructureTemplate;
import com.foudroyantfactotum.mod.fousarchive.items.ItemPianoRoll;
import com.foudroyantfactotum.mod.fousarchive.midi.midiPlayer.MidiPianoPlayer;
import com.foudroyantfactotum.mod.fousarchive.structure.registry.StructureDefinition;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TEPlayerPiano extends TEStructureTemplate implements ITickable
{
    public static final ExecutorService midiService = Executors.newCachedThreadPool();
    public static final String ITEM_LOADED_SONG = "itemPianoRoll";
    public static final String SONG_POSITION = "songPos";
    public static final String IS_SONG_PLAYING = "isSongPlaying";
    public static final String IS_SONG_RUNNING = "isSongRunning";
    public static final String HAS_SONG_TERMINATED = "hasSongTerminated";

    @SideOnly(Side.CLIENT)
    public volatile float[] keyOffset = new float[88];
    @SideOnly(Side.CLIENT)
    public volatile boolean[] keyIsDown = new boolean[88];

    public volatile double songPos;
    public volatile boolean isSongPlaying = false;
    public volatile boolean isSongRunning = false;
    public volatile boolean hasSongTerminated = true;

    public ItemPianoRoll loadedSong;

    public TEPlayerPiano()
    {
        //noop
    }

    public TEPlayerPiano(StructureDefinition sd, EnumFacing orientation, boolean mirror)
    {
        super(sd, orientation, mirror);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setString(ITEM_LOADED_SONG, loadedSong == null? "" : loadedSong.getUnlocalizedName());
        nbt.setDouble(SONG_POSITION, songPos);
        nbt.setBoolean(IS_SONG_PLAYING, isSongPlaying);
        nbt.setBoolean(IS_SONG_RUNNING, isSongRunning);
        nbt.setBoolean(HAS_SONG_TERMINATED, hasSongTerminated);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        loadedSong = (ItemPianoRoll) Item.getByNameOrId(nbt.getString(ITEM_LOADED_SONG));
        songPos = nbt.getDouble(SONG_POSITION);
        isSongPlaying = nbt.getBoolean(IS_SONG_PLAYING);
        isSongRunning = nbt.getBoolean(IS_SONG_RUNNING);
        hasSongTerminated = nbt.getBoolean(HAS_SONG_TERMINATED);
    }

    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        if (!hasSongTerminated)
            midiService.execute(new MidiPianoPlayer(this, songPos));
    }

    @Override
    public void update()
    {
        //Logger.info(this.toString());
    }

    @Override
    public String toString()
    {
        return "te.state: " + isSongPlaying + " : " + isSongRunning + " : " + hasSongTerminated + " : " + songPos + " : " + (loadedSong == null? " " : loadedSong.getUnlocalizedName());
    }
}