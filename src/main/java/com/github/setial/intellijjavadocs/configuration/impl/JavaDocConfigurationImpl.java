package com.github.setial.intellijjavadocs.configuration.impl;

import com.github.setial.intellijjavadocs.configuration.JavaDocConfiguration;
import com.github.setial.intellijjavadocs.exception.SetupTemplateException;
import com.github.setial.intellijjavadocs.model.settings.JavaDocSettings;
import com.github.setial.intellijjavadocs.model.settings.Level;
import com.github.setial.intellijjavadocs.model.settings.Mode;
import com.github.setial.intellijjavadocs.model.settings.Visibility;
import com.github.setial.intellijjavadocs.template.DocTemplateManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import java.util.HashSet;
import java.util.Set;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The type Java doc configuration impl.
 *
 * @author Sergey Timofiychuk
 */
@State(
    name = JavaDocConfiguration.COMPONENT_NAME,
    storages = @Storage("intellij-javadocs2.xml")
)
public class JavaDocConfigurationImpl implements JavaDocConfiguration,
    PersistentStateComponent<Element> {

    private static final Logger LOGGER = Logger.getInstance(JavaDocConfigurationImpl.class);

    private JavaDocSettings settings;
    private DocTemplateManager templateManager;
    private boolean loadedStoredConfig = false;

    /**
     * Instantiates a new Java doc configuration object.
     */
    public JavaDocConfigurationImpl() {
        templateManager = ApplicationManager.getApplication().getComponent(DocTemplateManager.class);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public JavaDocSettings getConfiguration() {
        JavaDocSettings result;
        try {
            result = (JavaDocSettings)getSettings().clone();
        } catch (Exception e) {
            // return null if cannot clone object
            result = null;
        }
        return result;
    }

    @Nullable
    @Override
    public Element getState() {
        Element root = new Element("JAVA_DOC_SETTINGS_PLUGIN");
        if (settings != null) {
            settings.addToDom(root);
            loadedStoredConfig = true;
        }
        return root;
    }

    @Override
    public void loadState(@NotNull Element javaDocSettings) {
        settings = new JavaDocSettings(javaDocSettings);
        setupTemplates();
        loadedStoredConfig = true;
    }

    @Override
    public JavaDocSettings getSettings() {
        if (!loadedStoredConfig) {
            // setup default values
            settings = new JavaDocSettings();
            Set<Level> levels = new HashSet<>();
            levels.add(Level.TYPE);
            levels.add(Level.METHOD);
            levels.add(Level.FIELD);

            Set<Visibility> visibilities = new HashSet<>();
            visibilities.add(Visibility.PUBLIC);
            visibilities.add(Visibility.PROTECTED);
            visibilities.add(Visibility.DEFAULT);

            settings.getGeneralSettings().setOverriddenMethods(false);
            settings.getGeneralSettings().setSplittedClassName(true);
            settings.getGeneralSettings().setMode(Mode.UPDATE);
            settings.getGeneralSettings().setLevels(levels);
            settings.getGeneralSettings().setVisibilities(visibilities);

            settings.getTemplateSettings().setClassTemplates(templateManager.getClassTemplates());
            settings.getTemplateSettings().setConstructorTemplates(templateManager.getConstructorTemplates());
            settings.getTemplateSettings().setMethodTemplates(templateManager.getMethodTemplates());
            settings.getTemplateSettings().setFieldTemplates(templateManager.getFieldTemplates());
        }
        return settings;
    }

    @Override
    public void setupTemplates() {
        try {
            templateManager.setClassTemplates(settings.getTemplateSettings().getClassTemplates());
            templateManager.setConstructorTemplates(settings.getTemplateSettings().getConstructorTemplates());
            templateManager.setMethodTemplates(settings.getTemplateSettings().getMethodTemplates());
            templateManager.setFieldTemplates(settings.getTemplateSettings().getFieldTemplates());
        } catch (SetupTemplateException e) {
            LOGGER.error(e);
            Messages.showErrorDialog("Javadocs plugin is not available, cause: " + e.getMessage(), "Javadocs plugin");
        }
    }

}
