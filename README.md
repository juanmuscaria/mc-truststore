# mc-truststore
Simple universal mod to use newer java cacerts alongside the (possibly outdated) current cacerts.

## Usage
Install it like any other mod for your favorite loader, it should support the following loaders:

* **LaunchWrapper**: Bootstrap through LaunchWrapper, compatible with forge from 1.6.4 to 1.12.2
* **ModLauncher**: Bootstrap through ModLauncher, compatible with (neo)forge from 1.13.2 to latest
* **Fabric Loader**: Bootstrap through Fabric Loader, compatible with any Minecraft version using fabric

The mod will attempt to inject a custom TrustManager in the default SSLContext that merges the default TrustManager and a new TrustManager using the keystore from newer java versions.
If the mod loads successfully it should now be possible to request URLs from servers using Let's Encrypt and DigiCert as their CA.

This should fix problems with mods requesting from a service using let's encrypt and minecraft unable to authenticate on ancient java versions.

Unless you are looking for a solution for those issues, you likely does not need the mod, however nothing should break if the mod is installed unnecessarily.


### Extra
Future reference for when I'm dealing with JSSE again https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html