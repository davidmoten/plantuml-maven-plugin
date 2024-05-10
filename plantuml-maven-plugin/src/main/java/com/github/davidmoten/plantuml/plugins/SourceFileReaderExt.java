package com.github.davidmoten.plantuml.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceFileReader;
import net.sourceforge.plantuml.file.SuggestedFile;
import net.sourceforge.plantuml.preproc.Defines;

public class SourceFileReaderExt extends SourceFileReader {

    public SourceFileReaderExt(Defines defines, File file, File outputDirectory, List<String> config, String charset,
            FileFormatOption fileFormatOption) throws IOException {
        super(defines, file, outputDirectory, config, charset, fileFormatOption);
    }

    public SuggestedFile extractSuggestedFile(BlockUml blockUml) throws FileNotFoundException {
        return getSuggestedFile(blockUml);
    }

}
