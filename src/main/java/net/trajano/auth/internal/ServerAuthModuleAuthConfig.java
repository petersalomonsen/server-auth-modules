package net.trajano.auth.internal;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;

import net.trajano.auth.AuthModuleConfigProvider;

/**
 * Provides initialized server modules/contexts.
 */
public class ServerAuthModuleAuthConfig extends AbstractAuthModuleAuthConfig implements ServerAuthConfig {
    /**
     * Context class.
     */
    private final Class<? extends ServerAuthContext> contextClass;

    /**
     * @param options
     *            options to pass into the module for initialization
     * @param layer
     *            layer
     * @param appContext
     *            application context
     * @param handler
     *            callback handlers
     * @throws AuthException
     */
    @SuppressWarnings("unchecked")
    public ServerAuthModuleAuthConfig(final Map<String, String> options, final String layer, final String appContext, final CallbackHandler handler) throws AuthException {
        super(options, layer, appContext, handler);
        try {
            contextClass = (Class<? extends ServerAuthContext>) Class.forName(options.get(AuthModuleConfigProvider.SERVER_AUTH_MODULE_CLASS));
        } catch (final ClassNotFoundException e) {
            throw new AuthException(e.getMessage());
        }
    }

    @Override
    public ServerAuthContext getAuthContext(final String authContextID, final Subject serviceSubject, @SuppressWarnings("rawtypes") final Map properties) throws AuthException {
        final Map<?, ?> augmentedOptions = augmentProperties(properties);
        try {
            final ServerAuthContext context = contextClass.newInstance();
            if (context instanceof ServerAuthModule) {

                final ServerAuthModule module = (ServerAuthModule) context;
                if (authContextID == null) {
                    module.initialize(NON_MANDATORY, NON_MANDATORY, getHandler(), augmentedOptions);
                } else {
                    module.initialize(MANDATORY, MANDATORY, getHandler(), augmentedOptions);
                }
            }
            return context;
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new AuthException(e.getMessage());
        }
    }
}
