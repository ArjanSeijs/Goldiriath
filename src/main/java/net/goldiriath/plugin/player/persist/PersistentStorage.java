package net.goldiriath.plugin.player.persist;

import com.google.common.base.CaseFormat;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.goldiriath.plugin.Goldiriath;
import net.goldiriath.plugin.player.persist.delegate.ConfigDelegate;
import net.goldiriath.plugin.player.persist.delegate.DefaultConfigDelegate;
import net.goldiriath.plugin.player.persist.delegate.IntConfigDelegate;
import net.goldiriath.plugin.player.persist.delegate.ListConfigDelegate;
import net.goldiriath.plugin.player.persist.delegate.StringConfigDelegate;
import net.goldiriath.plugin.util.ConfigLoadable;
import net.goldiriath.plugin.util.ConfigSavable;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

public class PersistentStorage implements ConfigLoadable, ConfigSavable {

    public static final Class<DefaultConfigDelegate> DEFAULT_DELEGATE_CLASS = DefaultConfigDelegate.class;
    public static final Map<Class<?>, Class<? extends ConfigDelegate<?>>> DELEGATES = new HashMap<>();

    static {
        register(int.class, IntConfigDelegate.class);
        register(Integer.class, IntConfigDelegate.class);
        register(String.class, StringConfigDelegate.class);
        register(List.class, ListConfigDelegate.class);
    }

    public static void register(Class<?> typeClass, Class<? extends ConfigDelegate<?>> delegateClass) {
        DELEGATES.put(typeClass, delegateClass);
    }

    protected boolean init = false;
    protected final List<Persistence<?>> fields = new ArrayList<>();

    public PersistentStorage() {
        for (Field field : getClass().getDeclaredFields()) {
            addField(field);
        }
    }

    @SuppressWarnings("unchecked")
    private void addField(Field field) {
        field.setAccessible(true);

        final Persist persistAnn = field.getAnnotation(Persist.class);
        if (persistAnn == null) {
            return;
        }

        // Get key
        String key = persistAnn.value().isEmpty() ? field.getName() : persistAnn.value();
        key = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, key);

        // Find delegate class
        final DelegatePersistence delegateAnn = field.getAnnotation(DelegatePersistence.class);
        Class<? extends ConfigDelegate<?>> delegateClass = (delegateAnn == null ? DELEGATES.get(field.getType()) : delegateAnn.value());

        if (delegateClass == null) {
            // Custom delegate, supertype
            final Class<?> fieldClass = field.getType();
            for (Class<?> loopDelegateClass : DELEGATES.keySet()) {
                if (loopDelegateClass.isAssignableFrom(fieldClass)) {
                    delegateClass = DELEGATES.get(loopDelegateClass);
                    break;
                }
            }
        }

        // Fallback to default
        if (delegateClass == null) {
            delegateClass = DEFAULT_DELEGATE_CLASS;
        }

        // Instantiate delegate
        ConfigDelegate<?> inst;
        try {
            inst = delegateClass.getConstructor(String.class).newInstance(key);
        } catch (Exception ex) {
            Bukkit.getLogger().severe("Could not setup persistent storage. Could not instance String-arg delegate constructor!");
            Bukkit.getLogger().severe(ExceptionUtils.getFullStackTrace(ex));
            return;
        }

        Object def;
        try {
            def = field.get(this);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Bukkit.getLogger().severe("Could not setup persistent storage. Could not obtain default");
            Bukkit.getLogger().severe(ExceptionUtils.getFullStackTrace(ex));
            return;
        }

        fields.add(new Persistence(field, inst, def));

        Goldiriath.plugin.logger.info("Added field: " + field.getName());
    }

    private void initFields() {
        if (init) {
            return;
        }

        for (Persistence<?> persist : fields) {
            Object def;
            try {
                def = persist.getField().get(this);
                persist.setDefaultValue(def);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Bukkit.getLogger().severe("Could not setup persistent storage. Could not obtain default");
                Bukkit.getLogger().severe(ExceptionUtils.getFullStackTrace(ex));
            }

            Goldiriath.plugin.logger.info("Init field: " + persist.getField().getName());
        }
        init = true;
    }

    @Override
    public void loadFrom(ConfigurationSection config) {
        initFields();

        for (Persistence<?> persist : fields) {
            try {
                Object value = persist.getDelegate().loadValue(config, persist.getField());

                if (value == null) {
                    Goldiriath.plugin.logger.info("DEBUG: Loading as default!");
                    value = persist.getDefaultValue();
                }

                persist.getField().set(this, value);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Bukkit.getLogger().severe("Could not load persistent storage value: " + persist.getField().getName());
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void saveTo(ConfigurationSection config) {
        initFields();

        for (Persistence<?> persist : fields) {
            try {
                Object value = persist.getField().get(this);
                persist.getDelegate().saveValue(config, value);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Bukkit.getLogger().severe("Could not save persistent storage value: " + persist.getField().getName());
            }
        }
    }

}
