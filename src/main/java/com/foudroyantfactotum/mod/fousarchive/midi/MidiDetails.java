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
package com.foudroyantfactotum.mod.fousarchive.midi;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.sound.midi.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class MidiDetails
{
    /*
    /music_category:       Piano_Roll
    /title:                Stumbling
    /subtitle:             Fox Trot
    /roll_manufacturer:    Broadway
    /roll_brand:           Broadway Word Roll
    /roll_number:          1080
    /roll_class:           88
    /performer:            Vanderhaak, William
    /composer:             Confrey
    /composition_date:     1922
    /roll_tempo:           80
    /song_type:            Foxtrot, Novelty Ragtime
    /roll_condition:       Excellent
    /roll_authenticity:    Original
    /roll_copyright:       Allans (9d Royalty Stamp)
    */

    public static final MidiDetails NO_DETAILS = new MidiDetails();

    private final String[] elem;

    private final String title;
    private final String subtitle;
    private final String rollManufacturer;
    private final String rollBrand;
    private final String rollNumber;
    private final String rollClass;
    private final String performer;
    private final String composer;
    private final String compositionDate;
    private final String rollTempo;
    private final String songType;
    private final String rollCondition;
    private final String rollAuthenticity;
    private final String rollCopyright;

    private static final String[] tags = {
            "/title:",
            "/subtitle:",
            "/roll_manufacturer:",
            "/roll_brand:",
            "/roll_number:",
            "/roll_class:",
            "/performer:",
            "/composer:",
            "/composition_date:",
            "/roll_tempo:",
            "/song_type:",
            "/roll_condition:",
            "/roll_authenticity:",
            "/roll_copyright:"
    };

    private MidiDetails()
    {
        this(new String[14]);
    }

    private MidiDetails(String[] result)
    {
        this.title              = result[0];
        this.subtitle           = result[1];
        this.rollManufacturer   = result[2];
        this.rollBrand          = result[3];
        this.rollNumber         = result[4];
        this.rollClass          = result[5];
        this.performer          = result[6];
        this.composer           = result[7];
        this.compositionDate    = result[8];
        this.rollTempo          = result[9];
        this.songType           = result[10];
        this.rollCondition      = result[11];
        this.rollAuthenticity   = result[12];
        this.rollCopyright      = result[13];

        this.elem = result;
    }

    @Nonnull
    public static MidiDetails getMidiDetails(InputStream stream) throws IOException
    {
        final String[] result = new String[tags.length];

        final Sequence sequence;

        try
        {
            sequence = MidiSystem.getSequence(stream);
        } catch (InvalidMidiDataException e)
        {
            return NO_DETAILS;
        }

        for (Track track : sequence.getTracks())
        {
            for (int i = 0; i < track.size(); i++)
            {
                final MidiEvent event = track.get(i);
                final MidiMessage midimsg = event.getMessage();

                if (midimsg instanceof MetaMessage)
                {
                    final MetaMessage msg = (MetaMessage) midimsg;
                    final String meta = new String(msg.getData());

                    for (int tgi = 0; tgi < tags.length; ++tgi)
                    {
                        final String tag = tags[tgi];

                        if (meta.startsWith(tag))
                        {
                            result[tgi] = meta.substring(tag.length()).trim();
                            break;
                        }
                    }
                }
            }
        }

        if (result[0] == null || result[7] == null)
            return NO_DETAILS;

        return new MidiDetails(result);
    }

    public String getSimpleDetails()
    {
        return String.format("\"%s\" by %s", title, composer);
    }

    public String getLongDetails()
    {
        return String.format(
                "%s #%s class %s\n" +
                        "Manufacturer: %s\n" +
                        "Performer: %s\n" +
                        "Composer: %s (%s)\n" +
                        "(c) %s"
                , rollBrand, rollNumber, rollClass
                , rollManufacturer
                , performer
                , composer, compositionDate
                , rollCopyright
        );
    }

    public ImmutableMap<String, String> toMap()
    {
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        for (int i=0; i < tags.length; ++i)
        {
            if (elem[i] != null)
                builder.put(tags[i], elem[i]);
        }

        return builder.build();
    }

    public static MidiDetails fromMap(ImmutableMap<String, String> map)
    {
        final String[] elem = new String[tags.length];

        for (Map.Entry<String, String> entry : map.entrySet())
        {
            for (int i=0; i < tags.length; ++i)
            {
                if (entry.getKey().equals(tags[i]))
                {
                    elem[i] = entry.getValue();
                    break;
                }
            }
        }

        if (elem[0] == null || elem[7] == null)
            return null;

        return new MidiDetails(elem);
    }

    public String getTitle()
    {
        return title;
    }

    public String getSubtitle()
    {
        return subtitle;
    }

    public String getRollManufacturer()
    {
        return rollManufacturer;
    }

    public String getRollBrand()
    {
        return rollBrand;
    }

    public String getRollNumber()
    {
        return rollNumber;
    }

    public String getRollClass()
    {
        return rollClass;
    }

    public String getPerformer()
    {
        return performer;
    }

    public String getComposer()
    {
        return composer;
    }

    public String getCompositionDate()
    {
        return compositionDate;
    }

    public String getRollTempo()
    {
        return rollTempo;
    }

    public String getSongType()
    {
        return songType;
    }

    public String getRollCondition()
    {
        return rollCondition;
    }

    public String getRollAuthenticity()
    {
        return rollAuthenticity;
    }

    public String getRollCopyright()
    {
        return rollCopyright;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("title", title)
                .add("subtitle", subtitle)
                .add("rollManufacturer", rollManufacturer)
                .add("rollBrand", rollBrand)
                .add("rollNumber", rollNumber)
                .add("rollClass", rollClass)
                .add("performer", performer)
                .add("composer", composer)
                .add("compositionDate", compositionDate)
                .add("rollTempo", rollTempo)
                .add("songType", songType)
                .add("rollCondition", rollCondition)
                .add("rollAuthenticity", rollAuthenticity)
                .add("rollCopyright", rollCopyright)
                .toString();
    }
}
