/*
 * Fou's Archive - A Minecraft Mod
 * Copyright (c) 2016 Foudroyant Factotum
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.foudroyantfactotum.mod.fousarchive.utility.midi;

import com.foudroyantfactotum.mod.fousarchive.TheMod;
import com.foudroyantfactotum.mod.fousarchive.midi.JsonMidiDetails;
import com.foudroyantfactotum.mod.fousarchive.midi.MidiDetails;
import com.foudroyantfactotum.mod.fousarchive.utility.FousArchiveException;
import com.foudroyantfactotum.mod.fousarchive.utility.log.Logger;
import com.foudroyantfactotum.mod.fousarchive.utility.log.UserLogger;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

//used in the sorting of midi files that are packaged with the mod (pre compilation)
public class FileSupporter
{
    public static final Gson JSON = new GsonBuilder()
            .registerTypeAdapter(JsonMidiDetails.class, JsonMidiDetails.Json.JSD)
            .create();

    private static final String midaddr = "src/main/resources/assets/" + TheMod.MOD_ID;
    private static final String output = midaddr + "/midi/";
    private static final String source = "midi/";

    private static final int noOfWorkers = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService pool = Executors.newFixedThreadPool(noOfWorkers);

    private static final BlockingQueue<File> files = new LinkedBlockingQueue<>();
    private static final ConcurrentMap<ResourceLocation, ImmutableMap<String, String>> processedMidiFiles = new ConcurrentHashMap<>();
    private static final File TERMINATOR = new File("");

    private static int fileCount = 0;

    public static void main(String[] args) throws InterruptedException, IOException
    {
        for (int i = 0; i < noOfWorkers; ++i)
            pool.submit(new ConMidiDetailsPuller());

        final File sourceDir = new File(source);
        final File outputDir = new File(output);

        Logger.info(UserLogger.GENERAL, "source directory: " + sourceDir.getAbsolutePath());
        Logger.info(UserLogger.GENERAL, "output directory: " + outputDir.getAbsolutePath());
        Logger.info(UserLogger.GENERAL, "processing midi files using " + noOfWorkers + " cores");

        FileUtils.deleteDirectory(outputDir);
        FileUtils.touch(new File(outputDir + "/master.json.gz"));

        for (File sfile : sourceDir.listFiles())
        {
            recFile(sfile, files);
        }

        for (int i = 0; i < noOfWorkers; ++i)
            files.put(TERMINATOR);

        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);//just get all the work done first.

        try (final OutputStream fstream = new FileOutputStream(outputDir + "/master.json.gz"))
        {
            try (final GZIPOutputStream gzstream = new GZIPOutputStream(fstream))
            {
                final OutputStreamWriter osw = new OutputStreamWriter(gzstream);

                osw.write(JSON.toJson(processedMidiFiles));
                osw.flush();
            }
        } catch (IOException e)
        {
            Logger.info(UserLogger.GENERAL, e.toString());
        }

        Logger.info(UserLogger.GENERAL, "Processed " + processedMidiFiles.size() + " midi files out of " + fileCount + " files. " + (fileCount - processedMidiFiles.size()) + " removed");
    }

    private static ImmutableMap<String, String> midiDetailsToMap(File f)
    {
        try (final InputStream stream = new FileInputStream(f))
        {
            final MidiDetails md = MidiDetails.getMidiDetails(stream);

            if (md != MidiDetails.NO_DETAILS)
            {
                //todo check songs for non pc work
                if (md.getTitle().toLowerCase().contains("nigger"))
                    throw new FousArchiveException("Contains non pc word : " + f.getPath());

                final Integer dateCopyright = cleanCopyrightDate(md.getRollCopyright());
                final Integer dateComposition = cleanCopyrightDate(md.getCompositionDate());

                if (dateCopyright != null && dateCopyright > 1923)
                {
                    throw new FousArchiveException("Inside Copyright date : *" + md.getRollCopyright() + "* : " + md.getCompositionDate() + " : " + f.getPath());
                } else if (dateCopyright == null && dateComposition != null && dateComposition > 1923)
                {
                    throw new FousArchiveException("Inside Copyright date : " + md.getRollCopyright() + " : *" + md.getCompositionDate() + "* : " + f.getPath());
                } else if (dateCopyright == null && dateComposition == null)
                {
                    throw new FousArchiveException("Missing valid copyright date : " + md.getRollCopyright() + " : " + md.getCompositionDate() + " : " + f.getPath());
                }

                return md.toMap();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private static Integer cleanCopyrightDate(String md)
    {
        //dates might be in the form...
        //1918, Jos. W. Stern & Co.
        //1921

        //last case is no date and therefore ignore
        //-
        //J. Albert & Son
        //Chappell & Co. (Royalty Stamp)
        //?
        Matcher regex = Pattern.compile("^\\d{4}").matcher(md);

        if (regex.find())
        {
            final String date = regex.group();
            try { return Integer.valueOf(date);} catch (NumberFormatException e) { }
        }

        return null;
    }

    private static class ConMidiDetailsPuller implements Runnable
    {
        @Override
        public void run()
        {
            File f = null;
            try
            {
                f = files.take();
            } catch (InterruptedException e) { }

            while (f != TERMINATOR)
            {

                ImmutableMap<String, String> details = null;
                try
                {
                    details = midiDetailsToMap(f);

                    if (details != null)
                    {
                        processedMidiFiles.put(new ResourceLocation(TheMod.MOD_ID, f.getPath()), details);

                        try
                        {
                            FileUtils.copyFile(f, new File(midaddr + '/' + f.getPath()));
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    } else
                    {
                        Logger.info(UserLogger.GENERAL, f + " Details Invalid");
                    }
                } catch (FousArchiveException e)
                {
                    Logger.info(UserLogger.GENERAL, "" + e);
                }
                try
                {
                    f = files.take();
                } catch (InterruptedException e)
                { }
            }
        }
    }

    private static void recFile(File file, Queue<File> list)
    {
        if (file.isDirectory())
        {
            final File[] fileList = file.listFiles();

            if (fileList != null)
            {
                for (final File sfile : fileList)
                {
                    recFile(sfile, list);
                }
            }
        } else
        {
            if (file.getName().toLowerCase().endsWith(".mid"))
            {
                list.add(file);
                ++fileCount;
            }
        }
    }
}
