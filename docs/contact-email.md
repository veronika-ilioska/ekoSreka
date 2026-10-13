# Contact form email

The footer submits to `POST /api/contact`. The server sends a plain-text email to
`ekosrekja@gmail.com`, with the visitor's address as Reply-To. The form clears only
after the SMTP server accepts the email; failures preserve the visitor's message.

Set these environment variables on the **backend** before starting it:

```text
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-sending-account@gmail.com
SMTP_PASSWORD=your-google-app-password
```

Use a Google app password for Gmail. Keep credentials in your deployment's secret
settings or local environment, never in frontend variables or committed files.
Other authenticated SMTP providers supporting STARTTLS on port 587 also work.
Restart the backend after changing these settings.

Without credentials the endpoint returns 503 and the UI shows an error rather than
claiming the message was sent. Automated tests mock email delivery and send no email.
