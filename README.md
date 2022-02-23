# OpenAuthMod
**Open source minecraft authentication protocol for proxies**

## What does this mod
This mod provides an interface for proxies to authenticate to an online mode server. This works by forwarding the authentication request to the client and letting it do the authentication.

## Compatibility
* Fabric 1.14 - 1.18.2
* Forge 1.14 - 1.18.2
* Forge 1.8 - 1.12.2

## Projects where this mod can be used
* [ViaProxy](https://discord.gg/dCzT9XHEWu): ViaProxy is a closed source proxy which lets players join on every classic, alpha, beta and release server. To try it out join *viaproxy.raphimc.net* with a minecraft 1.8 - latest client. OpenAuthMod allows you to join online mode servers over that proxy.
* [VIAaaS](https://github.com/ViaVersion/VIAaaS): VIAaaS is a standalone ViaVersion proxy.

## Building
To run this mod in a development environment you simply need to import the project as a gradle project into your IDE.\
To build it using the command line execute `gradlew build`

## Protocol definition
### Modern (>= 1.13)
1. Proxy sends a `0x04` LoginCustomPayload (Channel: `openauthmod:join`) packet which contains the hashed server id used for authentication.
2. Client receives packet and calls the `joinServer` method. If the client authenticated successfully it responds with an `0x02` LoginCustomPayloadResponse and empty data. If authentication failed the client will send the same packet and no data.
3. Proxy continues normal login state packet flow and lets the user join the server.

### Legacy (<= 1.12.2)
1. Proxy sends a `0x00` LoginDisconnect packet which is seperated by `\n` in three parts. The first part contains a message which the user sees if the OpenAuthMod is not installed. The second part contains the server hash used for authentication. The last part is used for identifying the packet as an authentication request. It is equal to:`String OPENAUTHMOD_LEGACY_MAGIC = new String(new byte[]{2, 20, 12, 3}, StandardCharsets.UTF_8)`
2. Client receives packet and calls the `joinServer` method. If the client authenticated successfully it responds with an `0x00` LoginHello packet. The data (username string) starts with `OPENAUTHMOD_LEGACY_MAGIC` and after that contains `true` or `false` depending on wether authentication succeeded or failed.
3. Proxy continues normal login state packet flow and lets the user join the server.
