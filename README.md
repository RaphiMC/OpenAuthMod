# OpenAuthMod
**Open source minecraft authentication protocol for proxies**

## What does this mod
This mod provides an easy and secure interface for proxies to authenticate to an online mode server. This works by forwarding the authentication request to the client and letting it do the authentication.

**When using this mod, your session or private keys WILL NOT be sent to the server at any point. Everything is done clientside and with your confirmation.**

## Compatibility
* Fabric 1.14 - 1.19
* Forge 1.14 - 1.19
* Forge 1.8 - 1.12.2

## Projects where this mod can be used
* [ViaProxy](https://discord.gg/dCzT9XHEWu): ViaProxy is a closed source proxy which lets players join on every classic, alpha, beta and release server. To try it out join *viaproxy.raphimc.net* with a minecraft 1.8 - latest client. OpenAuthMod allows you to join online mode servers over that proxy.
* [VIAaaS](https://github.com/ViaVersion/VIAaaS): VIAaaS is a standalone ViaVersion proxy.

## Building
To run this mod in a development environment you simply need to import the project as a gradle project into your IDE.\
To build it using the command line execute `gradlew build`

## Protocol definition
TODO: Has to be redone (Relevant code is inside the Shared module)
