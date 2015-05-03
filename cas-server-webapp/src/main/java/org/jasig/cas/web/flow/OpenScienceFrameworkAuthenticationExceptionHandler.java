//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.jasig.cas.web.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;

public class OpenScienceFrameworkAuthenticationExceptionHandler {
    private static final String UNKNOWN = "UNKNOWN";
    private static final String DEFAULT_MESSAGE_BUNDLE_PREFIX = "authenticationFailure.";
    private static final List<Class<? extends Exception>> DEFAULT_ERROR_LIST = new ArrayList();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @NotNull
    private List<Class<? extends Exception>> errors;
    private String messageBundlePrefix;

    public OpenScienceFrameworkAuthenticationExceptionHandler() {
        this.errors = DEFAULT_ERROR_LIST;
        this.messageBundlePrefix = "authenticationFailure.";
    }

    public void setErrors(List<Class<? extends Exception>> errors) {
        this.errors = errors;
    }

    public final List<Class<? extends Exception>> getErrors() {
        return Collections.unmodifiableList(this.errors);
    }

    public void setMessageBundlePrefix(String prefix) {
        this.messageBundlePrefix = prefix;
    }

    public String handle(AuthenticationException e, MessageContext messageContext) {
        if(e != null) {
            MessageBuilder messageCode = new MessageBuilder();
            Iterator i$ = this.errors.iterator();

            while(i$.hasNext()) {
                Class kind = (Class)i$.next();
                Iterator i$1 = e.getHandlerErrors().values().iterator();

                if (kind == OneTimePasswordRequiredException.class) {
                    return OneTimePasswordRequiredException.class.getSimpleName();
                }

                while(i$1.hasNext()) {
                    Class handlerError = (Class)i$1.next();
                    if(handlerError != null && handlerError.equals(kind)) {
                        String handlerErrorName = handlerError.getSimpleName();
                        String messageCode1 = this.messageBundlePrefix + handlerErrorName;
                        messageContext.addMessage(messageCode.error().code(messageCode1).build());
                        return handlerErrorName;
                    }
                }
            }
        }

        String messageCode2 = this.messageBundlePrefix + "UNKNOWN";
        this.logger.trace("Unable to translate handler errors of the authentication exception {}. Returning {} by default...", e, messageCode2);
        messageContext.addMessage((new MessageBuilder()).error().code(messageCode2).build());
        return "UNKNOWN";
    }

    static {
        DEFAULT_ERROR_LIST.add(AccountLockedException.class);
        DEFAULT_ERROR_LIST.add(FailedLoginException.class);
        DEFAULT_ERROR_LIST.add(CredentialExpiredException.class);
        DEFAULT_ERROR_LIST.add(AccountNotFoundException.class);
        DEFAULT_ERROR_LIST.add(AccountDisabledException.class);
        DEFAULT_ERROR_LIST.add(InvalidLoginLocationException.class);
        DEFAULT_ERROR_LIST.add(AccountPasswordMustChangeException.class);
        DEFAULT_ERROR_LIST.add(InvalidLoginTimeException.class);
        // Open Science Framework Exceptions
        DEFAULT_ERROR_LIST.add(LoginNotAllowedException.class);
        // One Time Password Exceptions
        DEFAULT_ERROR_LIST.add(OneTimePasswordFailedLoginException.class);
        DEFAULT_ERROR_LIST.add(OneTimePasswordRequiredException.class);
    }
}
