package org.sitmun.backend.schema;

import org.sitmun.backend.schema.model.Schema;
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;
import java.util.logging.Level;

public final class GenerateSchema {

    public static void main(final String[] args) throws Exception {

        // Set log level
        new LoggingConfig(Level.OFF);

        // Create the options
        final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
            .includeSchemas(new RegularExpressionInclusionRule("SITMUNDB.PUBLIC"));
        final LoadOptionsBuilder loadOptionsBuilder = LoadOptionsBuilder.builder()
            .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
        final SchemaCrawlerOptions options =
                SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                        .withLimitOptions(limitOptionsBuilder.toOptions())
                        .withLoadOptions(loadOptionsBuilder.toOptions());

        try (Connection connection = getConnection()) {
            createImage(options, getOutputFile("./src/docs/asciidoc/images/schema.svg"), connection);
            createDocument(options, getOutputFile("./src/docs/asciidoc/schema.adoc"), connection);
        }
    }

    private static void createDocument(SchemaCrawlerOptions options, Path file, Connection connection) throws Exception {
        final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable("template");
        executable.setSchemaCrawlerOptions(options);
        executable.setConnection(connection);
        executable.setOutputOptions(OutputOptionsBuilder.newOutputOptions(TextOutputFormat.text, file));
        final Config additionalConfig = new Config();
        additionalConfig.put("template", "plaintextschema.vm");
        additionalConfig.put("templating-language", "velocity");
        executable.setAdditionalConfiguration(additionalConfig);
        TemplateCommand.additionalContext.put("formatter", new Formatter());

        Table table = new Table("DATABASECHANGELOG", "Some description");
        Schema schema = new Schema();
        schema.setTables(List.of(table));
        TemplateCommand.additionalContext.put("metadata", schema);

        executable.execute();
    }

    private static void createImage(SchemaCrawlerOptions options, Path file, Connection connection) throws Exception {
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

    private static Connection getConnection() {
        final String connectionUrl = "jdbc:h2:file:./build/sitmundb";
        final DatabaseConnectionSource dataSource = new DatabaseConnectionSource(connectionUrl);
        dataSource.setUserCredentials(new SingleUseUserCredentials("sample", ""));
        return dataSource.get();
    }

    private static Path getOutputFile(final String name) {
        return Paths.get(name).toAbsolutePath().normalize();
    }
}