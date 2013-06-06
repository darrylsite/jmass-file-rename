package com.darrylsite.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author Darryl Kpizingui
 */
public class Processor
{

    private static final String SCRIPT_ENGINE = "JavaScript";
    private static final String FILENAME_PARAM = "fileName";
    private static final String IGNORE_PARAM = "ignore";
    private static final String FILE_PARAM = "file";
    ILogger logger;

    public void processFolder(String script, File folder, boolean doRename, boolean recursive)
    {
        File[] files = folder.listFiles();
        
        for (File file : files)
        {
            try
            {
                if (recursive && file.isDirectory())
                {
                    this.processFolder(script, file, doRename, recursive);
                } else
                {
                    String outputFileName = this.process(script, file.getName(), file, doRename);

                    String renameTxt = "Renaming {0} to {1}";
                    renameTxt = renameTxt.replace("{0}", file.getName()).replace("{1}", outputFileName);
                    logger.logInfo(renameTxt);
                }
            } catch (ScriptException | IOException ex)
            {
                logger.logInfo(ex.getMessage());
            }
        }
    }

    public String process(String script, String fileName, File file, boolean doRename) throws ScriptException, IOException
    {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName(SCRIPT_ENGINE);

        engine.put(FILENAME_PARAM, fileName);
        engine.put(IGNORE_PARAM, false);
        engine.put(FILE_PARAM, file);

        engine.eval(script);

        boolean ignore = (boolean) engine.get(IGNORE_PARAM);
        if (ignore)
        {
            return fileName;
        }

        String newFileName = (String) engine.get(FILENAME_PARAM);

        if (doRename)
        {
            Path pathFile = Paths.get(file.getAbsolutePath());
            File newFile = pathFile.resolveSibling(newFileName).toFile();
            file.renameTo(newFile);
        }

        return newFileName;
    }

    public ILogger getLogger()
    {
        return logger;
    }

    public void setLogger(ILogger logger)
    {
        this.logger = logger;
    }
}