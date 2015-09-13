package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;
import java.net.URI;

/**
 * A {@link Command} for authenticating the user details saved in {@link TmcSettings}.
 */
public class VerifyCredentials extends Command<Boolean> {

    private static final String TMC_SERVER_ROUTE = "/user";
    private static final int HTTP_SUCCESS_RANGE_MIN = 200;
    private static final int HTTP_SUCCESS_RANGE_MAX = 299;

    private UrlCommunicator communicator;

    /**
     * Constructs a new verify credentials command that authenticates user credentials specified
     * in {@code settings} using {@code communicator}.
     */
    public VerifyCredentials(TmcSettings settings, UrlCommunicator communicator) {
        super(settings);
        this.communicator = communicator;
    }

    private void assertHasRequiredData() throws TmcCoreException {
        String username = settings.getUsername();
        if (username == null || username.isEmpty()) {
            throw new TmcCoreException("Cannot verify credentials when no username is set.");
        }

        String password = settings.getPassword();
        if (password == null || password.isEmpty()) {
            throw new TmcCoreException("Cannot verify credentials when no password is set.");
        }
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public Boolean call() throws TmcCoreException, IOException {
        assertHasRequiredData();

        String auth = settings.getUsername() + ":" + settings.getPassword();

        int response = communicator
                .makeGetRequest(URI.create(settings.getServerAddress() + TMC_SERVER_ROUTE), auth)
                .getStatusCode();

        return (response >= HTTP_SUCCESS_RANGE_MIN && response <= HTTP_SUCCESS_RANGE_MAX);
    }
}
