# shellyscanner-fwupdater
CLI/GUI devices firmware updater from local file

**Download new firmware;**  
if you use http://archive.shelly-tools.de/  
you get an url such as ``http://192.168.1.100/ota?url=http://archive.shelly-tools.de/version/v1.9.4/SHSW-1.zip``;
use only the parameter (in this case ``http://archive.shelly-tools.de/version/v1.9.4/SHSW-1.zip``) to download the file locally.  
Alternatively you can use official archive ``https://api.shelly.cloud/files/firmware`` for first generation devices.

**Run the following command from terminal:**  
``java -jar shellyscanner-fwupdater-1.x.x.jar <shelly ip> <firmware file>``  
e.g. ``C:\Users\Antonio\util\shellyscanner-fwupdater-1.0.0.jar 192.168.1.55 C:\Users\Antonio\Downloads\SHSW-1.zip``
or just ``java -jar shellyscanner-fwupdater-1.x.x.jar`` to use GUI (you can also double click on the jar file)

**You get approximately the following messages:**  
Temporary server now responding at 192.168.1.4:8000  
Updating firmware ...  
Device response to update command:  
{"status":"updating","has_update":false,"new_version":"20221027-091427/v1.12.1-ga9117d3","old_version":"20221027-091427/v1.12.1-ga9117d3"}  
Press ctrl^C when firmware update is complete  
DO NOT close terminal before

Again: **wait until the update process is complete**  
(use Shelly Scanner to check or ``http://<shelly ip>/shelly`` from a browser)
