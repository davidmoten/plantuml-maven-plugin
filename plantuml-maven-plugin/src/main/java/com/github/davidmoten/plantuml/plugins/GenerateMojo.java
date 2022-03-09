package com.github.davidmoten.plantuml.plugins;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;
import net.sourceforge.plantuml.preproc.Defines;

@Mojo(name = "generate")
public final class GenerateMojo extends AbstractMojo {

    @Parameter(name = "sources")
    private FileSet sources;

    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/generated-diagrams/")
    private File outputDirectory;

    @Parameter(name = "charset", defaultValue = "UTF-8")
    private String charset;

    @Parameter(name = "configs")
    private List<String> configs;

    @Parameter(name = "formats")
    private List<String> formats;
    
    @Parameter(name = "metadata", defaultValue="true")
    private boolean metadata;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        if (formats == null) {
            formats = Collections.singletonList("PNG");
        }
        if (configs == null) {
            configs = Collections.emptyList();
        }
        if (sources == null) {
            sources = new FileSet();
            sources.setDirectory(new File(//
                    project.getBasedir(), //
                    "src" + File.separator //
                            + "main" + File.separator //
                            + "plantuml").getAbsolutePath());
        }
        getLog().info("sources=" + sources);
        getLog().info("configs=" + configs);
        try {
            if (sources.getIncludes().isEmpty()) {
                sources.addInclude("**/*.puml");
                sources.addInclude("**/*.plantuml");
                sources.addInclude("**/*.txt");
            }
            List<File> files = FileUtils.getFiles(new File(sources.getDirectory()),
                    commaSeparate(sources.getIncludes()), commaSeparate(sources.getExcludes()));
            for (File file : files) {
                for (String format : formats) {
                    FileFormat fileFormat = FileFormat.valueOf(format.toUpperCase());
                    getLog().info("generating image from " + file);
                    FileFormatOption option = new FileFormatOption(fileFormat, metadata);
                    final SourceFileReader reader = new SourceFileReader( //
                            Defines.createEmpty(), //
                            file, //
                            outputDirectory, //
                            configs, //
                            charset, //
                            option);
                    for (final GeneratedImage image : reader.getGeneratedImages()) {
                        getLog().info("image " + image + " written to " + image.getPngFile());
                    }
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private static String commaSeparate(List<String> list) {
        return list.stream().collect(Collectors.joining(","));
    }

}
