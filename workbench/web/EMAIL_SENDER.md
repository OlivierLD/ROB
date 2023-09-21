# EMail Sender
This one reworks a cvs file (saved from a spreadsheet) with the Java class `utils.CSVtoJSON.java` to produce a `json` file.  
Then this `members.json` can be used from the web page `email.sender.html` (look into the code) to generate the `bcc` member of a `mailto` URL.
That URL is invoking the EMail client of the host to send an email to all the bcc members.

> _Note_: On many email clients, the bcc cannot have more than 100 elements. This is why we produce - when needed - several URLs.

To run the web page, we use here a `NodeJS` server. As we refer to an "external" `json` ressource, a `file://` protocol would trigger
a CORS error.

To start the server and open the web page, run the script names `./run.sh`.

Then all you have to do is to cliek the link - this would open the local EMail client - and flesh out the content of the email (subject, content, attachments, etc).

---
