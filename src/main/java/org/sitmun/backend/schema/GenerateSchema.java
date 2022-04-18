package org.sitmun.backend.schema;

import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import org.sitmun.backend.schema.formatter.FormatterEn;
import org.sitmun.backend.schema.formatter.FormatterEs;
import org.sitmun.backend.schema.model.Configuration;
import org.sitmun.backend.schema.model.Table;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schemacrawler.*;
import schemacrawler.tools.command.template.TemplateCommand;
import schemacrawler.tools.command.text.diagram.options.DiagramOutputFormat;
import schemacrawler.tools.command.text.schema.options.SchemaTextOptionsBuilder;
import schemacrawler.tools.command.text.schema.options.TextOutputFormat;
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource;
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.OutputOptionsBuilder;
import us.fatehi.utility.LoggingConfig;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class GenerateSchema {

    public static Map<String, Object> formatters = new HashMap<>();
    public static void main(final String[] args) throws Exception {

        formatters.put("en", new FormatterEn());
        formatters.put("es", new FormatterEs());

        System.setProperty("config.file","application.conf");
        com.typesafe.config.Config config = ConfigFactory.load();

        Configuration conf = ConfigBeanFactory.create(config, Configuration.class);

        for (Table table: conf.getSchema().getTables()) {
            table.setDescription(trim(table.getDescription()));
        }

        // Set log level
        new LoggingConfig(Level.OFF);

        // Create the options
        final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
            .includeSchemas(new RegularExpressionInclusionRule(conf.getSchema().getName()));
        final LoadOptionsBuilder loadOptionsBuilder = LoadOptionsBuilder.builder()
            .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
        final SchemaCrawlerOptions options =
                SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                        .withLimitOptions(limitOptionsBuilder.toOptions())
                        .withLoadOptions(loadOptionsBuilder.toOptions());

        Path outputFolder = Paths.get(conf.getOutput().getFolder());
        Path imageFolder = Paths.get(conf.getOutput().getFolder(), "images");
        Path localeFolder = Paths.get(conf.getOutput().getFolder(), "locale");
        if (Files.exists(outputFolder)) {
            Files.walk(outputFolder)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectory(outputFolder);
        Files.createDirectory(imageFolder);
        Files.createDirectory(localeFolder);
        Path outputImage = Paths.get(conf.getOutput().getFolder(), "images", "schema.svg").toAbsolutePath().normalize();
        Path outputDoc = Paths.get(conf.getOutput().getFolder(), "schema.adoc").toAbsolutePath().normalize();

        for(String fileName : conf.getOutput().getIncludeFiles()) {
            Files.copy(Paths.get(fileName),  Paths.get(conf.getOutput().getFolder(), fileName));
        }

        try (InputStream attributes = GenerateSchema.class.getResourceAsStream("/locale/attributes.adoc")) {
            assert attributes != null;
            Files.copy(attributes, Paths.get(conf.getOutput().getFolder(), "locale", "attributes.adoc"));
        }

        try (InputStream attributes = GenerateSchema.class.getResourceAsStream("/locale/attributes-"+conf.getOutput().getLanguage()+".adoc")) {
            assert attributes != null;
            Files.copy(attributes, Paths.get(conf.getOutput().getFolder(), "locale", "attributes-"+conf.getOutput().getLanguage()+".adoc"));
        }

        try (Connection connection = getConnection(conf)) {
            createImage(options, outputImage, connection);
            createDocument(conf, options, outputDoc, connection);
        }
    }

    private static void createDocument(Configuration conf, SchemaCrawlerOptions options, Path file, Connection connection) {
        final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable("template");
        executable.setSchemaCrawlerOptions(options);
        executable.setConnection(connection);
        executable.setOutputOptions(OutputOptionsBuilder.newOutputOptions(TextOutputFormat.text, file));
        final Config additionalConfig = new Config();
        additionalConfig.put("template", Paths.get(conf.getOutput().getLanguage(),"plaintextschema.vm").toString());
        additionalConfig.put("templating-language", "velocity");
        executable.setAdditionalConfiguration(additionalConfig);

        TemplateCommand.additionalContext.put("formatter", formatters.get(conf.getOutput().getLanguage()));
        TemplateCommand.additionalContext.put("config", conf);

        executable.execute();
    }

    private static void createImage(SchemaCrawlerOptions options, Path file, Connection connection) {
        final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable("schema");
        executable.setSchemaCrawlerOptions(options);
        executable.setConnection(connection);
        final OutputOptions output = OutputOptionsBuilder.newOutputOptions(DiagramOutputFormat.svg, file);
        executable.setOutputOptions(output);
        final Config additionalConfig = new Config();
        additionalConfig.merge(SchemaTextOptionsBuilder.builder().portableNames().toConfig());
        executable.setAdditionalConfiguration(additionalConfig);
        executable.execute();
    }

    private static Connection getConnection(Configuration conf) {
        final String connectionUrl = conf.getDatabase().getUrl();
        final DatabaseConnectionSource dataSource = new DatabaseConnectionSource(connectionUrl);
        dataSource.setUserCredentials(new SingleUseUserCredentials(conf.getDatabase().getUser(), conf.getDatabase().getPassword()));
        return dataSource.get();
    }

    private static String trim(String text) {
        return Pattern.compile("\n").splitAsStream(text)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));
    }
}