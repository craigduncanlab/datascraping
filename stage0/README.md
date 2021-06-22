# Overview of FTP utility

Built in 2017.

Returns log of directory structure and contents of a specific FTP server: ftp.dec.wa.gov.au

# Usage

After compilation run with filename where you want log of files to be stored:

```
java EDOmain filename

```

If no filename is used, defaults to 'Permits.txt'

Output file will return file information with this structure:

{"count","pathname","size","download total"}

# EDOclient

Retrieves file information.

Directories are navigated to produce written file, with a log of what is on server.

