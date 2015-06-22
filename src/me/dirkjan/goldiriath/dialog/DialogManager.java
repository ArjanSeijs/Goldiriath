package me.dirkjan.goldiriath.dialog;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import me.dirkjan.goldiriath.Goldiriath;
import me.dirkjan.goldiriath.util.Service;
import net.pravian.bukkitlib.config.YamlConfig;
import net.pravian.bukkitlib.util.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public class DialogManager implements Service {

    private final Goldiriath plugin;
    private final Logger logger;
    private final File dialogContainer;
    private final Map<String, NPCDialogHandler> handlers;

    public DialogManager(Goldiriath plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dialogContainer = FileUtils.getPluginFile(plugin, "dialogs");
        this.handlers = new HashMap<>();
    }

    public Goldiriath getPlugin() {
        return plugin;
    }

    @Override
    public void start() {

        // Ensure folder is present
        if (dialogContainer.isFile()) {
            if (!dialogContainer.delete()) {
                logger.severe("Not loading dialogs! Could not delete file: " + dialogContainer.getAbsolutePath());
                return;
            }
        }
        if (!dialogContainer.exists()) {
            dialogContainer.mkdirs();
        }

        // Load handlers
        handlers.clear();
        for (File file : dialogContainer.listFiles(new DialogFileFilter(plugin))) {
            final String id = file.getName().replace("quest_", "").replace(".yml", "").trim().toLowerCase();

            if (id.isEmpty() || handlers.containsKey(id)) {
                logger.warning("Skipping dialog handler file: " + file.getName() + ". Invalid dialog ID!");
                continue;
            }

            final YamlConfig config = new YamlConfig(plugin, file, false);
            config.load();

            final NPCDialogHandler dialog = new NPCDialogHandler(this, id);

            try {
                dialog.loadFrom(config);
            } catch (Exception ex) {
                logger.warning("Skipping dialog handler: " + id + ". Exception loading dialog!");
                logger.severe(ExceptionUtils.getFullStackTrace(ex));
            }

            if (!dialog.isValid()) {
                logger.warning("Skipping dialog handler: " + id + ". Invalid dialog handler! (Are there missing entries?)");
                continue;
            }

            handlers.put(id, dialog);
        }
    }

    @Override
    public void stop() {
        for (NPCDialogHandler handler : handlers.values()) {
            handler.unregister();
        }
    }

    public Map<String, NPCDialogHandler> getHandlers() {
        return Collections.unmodifiableMap(handlers);
    }

}
