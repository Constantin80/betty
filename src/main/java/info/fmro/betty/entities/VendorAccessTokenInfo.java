package info.fmro.betty.entities;

import info.fmro.betty.enums.TokenType;

@SuppressWarnings("unused")
class VendorAccessTokenInfo {
    private String access_token; // Session token used by web vendors
    private TokenType token_type; // Type of the token
    private Long expires_in; // How long until the token expires
    private String refresh_token; // Token used to refresh the session token in future
    private ApplicationSubscription application_subscription; // Object containing the vendor client id and optionally some subscription information
}
