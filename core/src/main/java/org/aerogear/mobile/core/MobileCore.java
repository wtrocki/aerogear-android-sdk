package org.aerogear.mobile.core;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import org.aerogear.android.core.BuildConfig;
import org.aerogear.mobile.core.configuration.MobileCoreJsonParser;
import org.aerogear.mobile.core.configuration.ServiceConfiguration;
import org.aerogear.mobile.core.exception.ConfigurationNotFoundException;
import org.aerogear.mobile.core.exception.InitializationException;
import org.aerogear.mobile.core.http.HttpServiceModule;
import org.aerogear.mobile.core.http.OkHttpServiceModule;
import org.aerogear.mobile.core.logging.Logger;
import org.aerogear.mobile.core.logging.LoggerAdapter;
import org.aerogear.mobile.core.metrics.MetricsService;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.aerogear.mobile.core.utils.SanityCheck.nonNull;

/**
 * MobileCore is the entry point into AeroGear mobile services
 */
public final class MobileCore {

    private static final String TAG = "AEROGEAR/CORE";
    private static Logger logger = new LoggerAdapter();
    private static String appVersion;

    private final Context context;
    private final String configFileName;
    private final HttpServiceModule httpLayer;
    private final Map<String, ServiceConfiguration> servicesConfig;
    private final Map<Class<? extends ServiceModule>, ServiceModule> services = new HashMap<>();

    /**
     * Creates a MobileCore instance
     *
     * @param context Application context
     */
    private MobileCore(final Context context, final Options options) throws InitializationException, IllegalStateException {
        this.context = nonNull(context, "context").getApplicationContext();
        this.configFileName = nonNull(options, "options").configFileName;

        // -- Allow to override the default logger
        if (options.logger != null) {
            logger = options.logger;
        }

        // -- Parse JSON config file
        try (final InputStream configStream = context.getAssets().open(configFileName)) {
            this.servicesConfig = MobileCoreJsonParser.parse(configStream);
        } catch (JSONException | IOException exception) {
            String message = String.format("%s could not be loaded", configFileName);
            throw new InitializationException(message, exception);
        }

        // -- Set the app version variable
        appVersion = getAppVersion(context);

        // -- Setting default http layer
        if (options.httpServiceModule == null) {
            final OkHttpServiceModule httpServiceModule = new OkHttpServiceModule();

            ServiceConfiguration configuration = this.servicesConfig.get(httpServiceModule.type());
            if (configuration == null) {
                configuration = new ServiceConfiguration.Builder().build();
            }

            httpServiceModule.configure(this, configuration);

            this.httpLayer = httpServiceModule;
        } else {
            this.httpLayer = options.httpServiceModule;
        }
    }

    /**
     * Initialize the AeroGear system
     *
     * @param context Application context
     * @return MobileCore instance
     */
    public static MobileCore init(final Context context) throws InitializationException {
        return init(context, new Options());
    }

    /**
     * Initialize the AeroGear system
     *
     * @param context Application context
     * @param options AeroGear initialization options
     * @return MobileCore instance
     */
    public static MobileCore init(final Context context, final Options options) throws InitializationException {
        return new MobileCore(context, options);
    }

    /**
     * Called when mobile core instance needs to be destroyed
     */
    public void destroy() {
        for (Class<? extends ServiceModule> serviceKey : services.keySet()) {
            ServiceModule serviceModule = services.get(serviceKey);
            serviceModule.destroy();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ServiceModule> T getInstance(final Class<T> serviceClass) {
        return (T) getInstance(serviceClass, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends ServiceModule> T getInstance(final Class<T> serviceClass,
                                                   final ServiceConfiguration serviceConfiguration)
        throws InitializationException {
        nonNull(serviceClass, "serviceClass");

        if (services.containsKey(serviceClass)) {
            return (T) services.get(serviceClass);
        }

        try {
            final ServiceModule serviceModule = serviceClass.newInstance();

            ServiceConfiguration serviceCfg = serviceConfiguration;

            if (serviceCfg == null) {
                serviceCfg = getServiceConfiguration(serviceModule.type());
            }

            if (serviceCfg == null && serviceModule.requiresConfiguration()) {
                throw new ConfigurationNotFoundException(serviceModule.type() + " not found on " + this.configFileName);
            }

            serviceModule.configure(this, serviceCfg);

            services.put(serviceClass, serviceModule);

            return (T) serviceModule;

        } catch (IllegalAccessException | InstantiationException e) {
            throw new InitializationException(e.getMessage(), e);
        }
    }

    /**
     * Get application context
     *
     * @return Application context
     */

    public Context getContext() {
        return context;
    }

    /**
     * Returns the configuration for this singleThreadService from the JSON config file
     *
     * @param type Service type/name
     * @return the configuration for this singleThreadService from the JSON config file
     */
    public ServiceConfiguration getServiceConfiguration(final String type) {
        return this.servicesConfig.get(type);
    }

    /**
     * Get the user app version from the package manager
     *
     * @param context Android application context
     * @return String app version name
     */
    private String getAppVersion(final Context context) throws InitializationException {
        nonNull(context, "context");
        try {
            return context
                .getPackageManager()
                .getPackageInfo(context.getPackageName(), 0)
                .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // Wrap in Initialization exception
            throw new InitializationException("Failed to read app version", e);
        }
    }

    public HttpServiceModule getHttpLayer() {
        return this.httpLayer;
    }

    public static Logger getLogger() {
        return logger;
    }

    /**
     * Get the version name of the SDK itself
     *
     * @return String SDK version
     */
    public static String getSdkVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * Get the version of the user app
     *
     * @return String App version name
     */
    public static String getAppVersion() {
        return appVersion;
    }

    public static final class Options {

        private String configFileName = "mobile-services.json";
        // Don't have a default implementation because it should use configuration
        private HttpServiceModule httpServiceModule;
        private Logger logger = new LoggerAdapter();

        public Options() {
        }

        public Options(@NonNull final String configFileName, @NonNull final HttpServiceModule httpServiceModule) {
            this.configFileName = nonNull(configFileName, "configFileName");
            this.httpServiceModule = nonNull(httpServiceModule, "httpServiceModule");
        }

        public Options setConfigFileName(@NonNull final String configFileName) {
            this.configFileName = nonNull(configFileName, "configFileName");
            return this;
        }

        public Options setHttpServiceModule(@NonNull final HttpServiceModule httpServiceModule) {
            this.httpServiceModule = nonNull(httpServiceModule, "httpServiceModule");
            return this;
        }

        public Options setLogger(final Logger logger) {
            this.logger = logger;
            return this;
        }
    }
}
