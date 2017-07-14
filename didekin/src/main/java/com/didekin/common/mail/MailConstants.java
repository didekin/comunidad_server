package com.didekin.common.mail;

/**
 * User: pedro@didekin
 * Date: 05/05/16
 * Time: 10:42
 */
public final class MailConstants {

    private MailConstants()
    {
    }

    public static final String aws_cred_username = System.getenv("aws_cred_username");
    public static final String aws_cred_password = System.getenv("aws_cred_password");
    public static final String aws_smtp_host = "email-smtp.eu-west-1.amazonaws.com";
    public static final int aws_smtp_port = Integer.parseInt(System.getenv("aws_smtp_port"));
    public static final String mail_transport_protocol = "smtp";
    public static final String mail_smtp_auth_prop = "mail.smtp.auth";
    public static final String mail_smtp_starttls_enable_prop = "mail.smtp.starttls.enable";
    public static final String mail_smtp_starttls_required = "mail.smtp.starttls.required";
    public static final String mail_from = System.getenv("mail_from");
    static final String TEXT_PLAIN_UTF_8 = "text/plain; charset=UTF-8";
    public static final String default_encoding = "UTF-8";
}
