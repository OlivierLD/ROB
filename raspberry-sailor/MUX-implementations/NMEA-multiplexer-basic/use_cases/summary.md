# Summary of the build process

<table>
    <tr>
        <th width="50%">Machine A</th>
        <th width="50%">Machine B</th>
    </tr>
    <tr>
        <td width="50%">The git repo is or will be cloned on this machine, the build will happen on this one.</td>
        <td width="50%">No git repo here, only parts required at runtime will be available here.</td>
    </tr>
    <tr>
        <td style="text-align: center"><img src="../doc_resources/laptop.jpeg"></td>
        <td style="text-align: center"><img src="../doc_resources/raspberrypi.jpeg"></td>
    </tr>
    <tr>
        <td>Make sure you got <code>git</code> and the right version of <code>java</code></td>
        <td>On a new image, install the right version of Java, and <code>librxtx-java</code></td>
    </tr>
    <tr><td>Clone the repo</td><td></td></tr>
    <tr><td>Build the Project</td><td></td></tr>
    <tr><td>Package it for Production, create an archive <img src="../doc_resources/zip.archive.jpeg" height="60" style="vertical-align: middle;"></td><td></td></tr>
    <tr><td colspan="2">Send the archive to the Raspberry Pi (using <code>scp</code>)</td></tr>
    <tr>
        <td colspan="2" style="text-align: center;">
            <img src="../doc_resources/laptop.jpeg" style="vertical-align: middle;">
            <img src="../doc_resources/wifi.png" height="100" style="transform: rotate(90deg); vertical-align: middle;">
            <img src="../doc_resources/zip.archive.jpeg" height="60" style="vertical-align: middle;">
            <img src="../doc_resources/wifi.png" height="100" style="transform: rotate(90deg); vertical-align: middle;">
            <img src="../doc_resources/raspberrypi.jpeg" style="vertical-align: middle;">
        </td>
    </tr>
    <tr><td></td><td><img src="../doc_resources/zip.archive.jpeg" height="60" style="vertical-align: middle;">Unzip the received archive</td></tr>
    <tr><td></td><td>Plug in the GPS, link the serial port, and give it a try ! With <code>./mux.sh nmea.mux.gps.yaml</code> for example.</td></tr>
    <tr><td></td><td>Finally, you can setup the Raspberry Pi to start everything at boot, in <code>/etc/rc.local</code>, and a HotSpot.</td></tr>
</table>

---
_OlivSoft never stops_