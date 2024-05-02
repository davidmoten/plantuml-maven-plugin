package com.github.davidmoten.plantuml.plugins;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.io.FileUtils;

import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.file.SuggestedFile;
import net.sourceforge.plantuml.preproc.Defines;
import net.sourceforge.plantuml.security.SFile;

@Mojo(name = "generate", threadSafe = true)
public final class GenerateMojo extends AbstractMojo {

    @Parameter(name = "sources")
    private FileSet sources;

    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/generated-diagrams/")
    private File outputDirectory;

    @Parameter(name = "charset", defaultValue = "UTF-8")
    private String charset;

    @Parameter(name = "configs")
    private List<String> configs;

    @Parameter(name = "configFiles")
    private List<File> configFiles;

    @Parameter(name = "formats")
    private List<String> formats;

    @Parameter(name = "metadata", defaultValue = "true")
    private boolean metadata;

    @Parameter(name = "storePreproc", defaultValue = "false")
    private boolean storePreproc;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    
    @Parameter(defaultValue = "false")
    private boolean preserveDirectoryStructure;

    @Override
    public void execute() throws MojoExecutionException {
        if (formats == null) {
            formats = Collections.singletonList("PNG");
        }
        if (configs == null) {
            configs = new ArrayList<>();
        }
        if (sources == null) {
            sources = new FileSet();
            sources.setDirectory(new File(//
                    project.getBasedir(), //
                    "src" + File.separator //
                            + "main" + File.separator //
                            + "plantuml").getAbsolutePath());
        }
        if (configFiles == null) {
            configFiles = Collections.emptyList();
        }
        getLog().info("sources=" + sources);
        try {
            addConfigFiles(configs, configFiles);
            getLog().info("configs=" + configs);
            if (sources.getIncludes().isEmpty()) {
                sources.addInclude("**/*.puml");
                sources.addInclude("**/*.plantuml");
                sources.addInclude("**/*.txt");
            }
            File sourcesDirectory = new File(sources.getDirectory());
            if (!sourcesDirectory.exists()) {
                getLog().info("sources directory does not exist");
                return;
            }
            List<File> files = FileUtils.getFiles(sourcesDirectory,
                    commaSeparate(sources.getIncludes()), commaSeparate(sources.getExcludes()));
            ExecutorService executor = Executors.newWorkStealingPool();
            List<Path> relativePaths = new ArrayList<>();
            String firstFormat = formats.isEmpty() ? null : formats.get(0);
            for (File file : files) {
                for (String format : formats) {
                    executor.submit(() -> {
                        FileFormat fileFormat = FileFormat.valueOf(format.toUpperCase());
                        getLog().info("generating image from " + file);
                        FileFormatOption option = new FileFormatOption(fileFormat, metadata);
                        final File outDir;
                        if (preserveDirectoryStructure) {
                            Path rel = Paths.get(sources.getDirectory()).relativize(file.getParentFile().toPath());
                            getLog().info("relative output path=" + rel);
                            outDir = Paths.get(outputDirectory.getAbsolutePath(), rel.toString()).toFile();
                        } else {
                            outDir = outputDirectory;
                        }
                        final SourceFileReaderExt reader = new SourceFileReaderExt( //
                                Defines.createEmpty(), //
                                file, //
                                outDir, //
                                configs, //
                                charset, //
                                option);
                        for (final GeneratedImage image : reader.getGeneratedImages()) {
                            getLog().info("image " + image + " written to " + image.getPngFile());
                            if (firstFormat.equals(format)) {
                                relativePaths.add(outputDirectory.toPath().relativize(image.getPngFile().toPath()));
                                if (storePreproc) {
                                	extractPreproc(reader);
                                }
                            }
                        }
                        return null;
                    });
                }
            }
            executor.shutdown();
            executor.awaitTermination(10L, TimeUnit.MINUTES);
            File index = new File(outputDirectory, "index.md");
            StringBuilder b = new StringBuilder();
            relativePaths.sort((x, y) -> x.toString().compareTo(y.toString()));
            relativePaths.forEach(path -> {
                String name = removeExtension(path.toString());
                b.append("### " + name + "\n");
                b.append("![" + name + "](" + path + ")\n\n");
            });
            Files.write(index.toPath(), b.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private static String removeExtension(String s) {
        int i = s.lastIndexOf(".");
        if (i == -1) {
            return s;
        } else {
            return s.substring(0, i);
        }
    }

    private static void addConfigFiles(List<String> configs, List<File> configFiles) throws IOException {
        for (File f : configFiles) {
            Files.readAllLines(f.toPath()) //
                    .stream() //
                    .map(x -> x.trim()) //
                    .filter(x -> !x.isEmpty()) //
                    .peek(x -> configs.add(x)) //
                    .count();
        }
    }

    private static String commaSeparate(List<String> list) {
        return list.stream().collect(Collectors.joining(","));
    }

	private void extractPreproc(SourceFileReaderExt sourceFileReader) throws IOException {
		for (BlockUml blockUml : sourceFileReader.getBlocks()) {
			final SuggestedFile suggested = sourceFileReader.extractSuggestedFile(blockUml)
					.withPreprocFormat();
			final SFile file = suggested.getFile(0);
			getLog().info("Export preprocessing source to " + file.getPrintablePath());
			try (final PrintWriter pw = file.createPrintWriter(charset)) {
				pw.println("'DO NOT EDIT THIS FILE, PREPROCESSED OUTPUT");
				for (CharSequence cs : blockUml.getDefinition(true)) {
					String s = cs.toString();
					if (s.trim().length() > 0) {
						pw.println(s);
					}
				}
			}
		}
	}

}
