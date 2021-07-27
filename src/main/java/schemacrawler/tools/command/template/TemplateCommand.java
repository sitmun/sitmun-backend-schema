/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2021, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/

package schemacrawler.tools.command.template;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

import org.sitmun.backend.schema.Formatter;
import schemacrawler.tools.command.template.options.TemplateLanguageType;
import schemacrawler.tools.executable.BaseSchemaCrawlerCommand;
import schemacrawler.tools.options.LanguageOptions;

public final class TemplateCommand extends BaseSchemaCrawlerCommand<LanguageOptions> {

    static final String COMMAND = "template";

    public TemplateCommand() {
        super(COMMAND);
    }

    @Override
    public void checkAvailability() throws Exception {
        // Nothing to check at this point. The Command should be available
        // after the class is loaded, and imports are resolved.
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws Exception {
        requireNonNull(commandOptions, "No template language provided");
        checkCatalog();

        // Find if the language type is valid, or throw an exception
        final TemplateLanguageType languageType =
                TemplateLanguageType.valueOf(commandOptions.getLanguage());

        final String templateRendererClassName = languageType.getTemplateRendererClassName();
        final Class<TemplateRenderer> templateRendererClass =
                (Class<TemplateRenderer>) Class.forName(templateRendererClassName);
        final TemplateRenderer templateRenderer = templateRendererClass.newInstance();

        final Map<String, Object> context = new HashMap<>();
        context.put("catalog", catalog);
        context.put("identifiers", identifiers);
        context.put("formatter", new Formatter());

        templateRenderer.setResourceFilename(commandOptions.getScript());
        templateRenderer.setContext(context);
        templateRenderer.setOutputOptions(outputOptions);

        templateRenderer.execute();
    }

    @Override
    public boolean usesConnection() {
        return true;
    }
}
