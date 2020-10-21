# cMb

Coda message browser

"ant instal"l will build the package and install it in the $CODA/common/jar.

*cmb* will start the UI that has the following options:

*cmb* -< option > < value >

<p>< option >
<p>-h or -help        :   prints help
<p>-title             :   title of the GUI.
<p>-host              :   platform host.
<p>-port              :   platform port.
<p>-subject           :   subject of the subscription.
<p>-type              :   type of the subscription.
<p>-archive           :   direct path to the archive dir.
<p>-queueSize         :   stored message queue size.
  

Using the new option -archive user  will specify a direct path to a directory where the cMb will archive daLog messages.
In the required path the cMb will create a **type(codaClass)/message-severity** subdirectory structures, where it will store appropriate messages in a  component_name.cmb file.
<p>Messages will be appended to component files with a proper time stamps.
<p>The rest of the functionality of the UI is not changed. It will still show coda messages in 3 dedicated message windows (tabs): “Selection”,”daLog" and "Message Space".
<p>The "Message Space" will show all Coda messages: subject = * and type = *.
<p> The "Selection" message table shows messages from a  specific subject/type: options -subject, -type, also selection made from the "Message Space" table.
<p>The “daLog” message table always shows messages with the subject = * and the type = "rc/report/dalog”.
<p>To define/configure a message archiving choice a user needs to follow:
Control -> "Message Filter and Archive” -> and define the component name, as well as specific severity of the messages that is required to archive. The selection of a type is not required for archiving.


