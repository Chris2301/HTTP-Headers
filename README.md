# Http Header

A deeper look into what http headers do for us

## 1. Authentication

### WWW-Authenticate

**Type:** Response Header

**Directives:**
- Scheme: authentication schema like **basic**, **bearer**, **digest**
- realm [optional]: information about which part of the application is used e.g. 'Admin configuration'. 
Normally nothing is used due to phishing and if mandatory the hostname. 
- token68 [optional]: aka b64token, where you can put the access token.

This is the base of it, because each type can add its own specific directives.

**Examples**

`WWW-Authenticate: Bearer realm="https://example.com/login", error="invalid_token", error_description="The access token expired"`

`WWW-Authenticate: Basic realm="Access to the admin site"`

**Use**

Normally the framework that you use would make sure this is included in the right format. Like with Spring Security for example.

### Authorisation

**Type:** Request header

**Directives**
- Scheme: authorisation scheme like **basic**, **bearer**, **digeest**

**examples**

`Authorization: Basic RXhhbXBsZTphaQ==` (base64 encoded username:password)

`Authorization: Bearer RXhhbXBsZTphaQ==` (JWT encoded bearer token)

**Use**

The right client library creates the jwt for you. In Angular the angular-oauth2-oidc library (or similar) can help you do it.
On the server side Spring Security framework helps you to implement authentication/authorisation.

### Proxy-authenticate & Proxy-authorisation

**Type:** request and response

The Authorisation request - WWW-Authenticate response is almost the same as these two. 
The difference is that it is possible to configure a proxy server between client and the server that checks authentication.
If there is no authorisation header a 407 Proxy Authentication Required is thrown.

Noteworthy is that when you configure a proxy server the proxy-authorisation header has to be set. 

## 2. Caching

### Age

**Type:** Response header

It shows time in seconds it spends in proxy caches, content delivery systems (CDN) or reverse proxy servers in between the server and the client. So normally without proxies `age: 0`
and each proxy adds time to age if it stays there for a moment. This is done automatically by these proxies.

### Cache-control

**Type:** Request en Response Header

**Use** 

The General use is to control the cache of the browser and proxies. Think of streaming media where replay should be quick for at least 2 minutes.

**Request Directives**

- max-age: setting the max time in seconds a proxy can cache a response from the server, serving the same response for the rest of this set time. When it is after the time, the request is sent 
to the backend server to check if the cache is still fresh. NOTE: that if this is done by the client, this should be handled with care. 
Potentially you can set the max-age with each request and push the max-age constantly further in time and the cache is always considered fresh and will never go to the server.
- max-stale: if the cache max-age is reached, but the server is offline, this directive can be used to respond with the old cache responses. 
- min-fresh: it sets the clients min-freshness it needs from responses from the cache, since max-age is typically set by the server. 
After it exceeds the min-fresh time, the client can trigger the cache to refresh by sending a **If-Modified-Since** or **If-None-Match** conditional header. 
- no-cache: set this to make sure the client always gets a response from the server, not matter what cache is on the way. NOTE: caches are allowed to cache it. 
- no-store: whereas no-cache fetches from the server and caches are allowed to cache. No store fetches from the server and tells caches not to cache.
- no-transform: on the way from the client to the server no transformations are allowed, like compression of images or transcoding images to other size or types.
- only-if-cached: tells that it only wants fresh, no exceeding max-age responses from the cache and nothing from the server. So also no stale or unfresh responses.
If there is no cached response available it normally responses with `502 Bad Gateway` or `504 Gateway Timeout`.

**Response Directives**

- max-age: the server sets the max-age of responses to be chached in a proxy. This is often better than the client telling the max-age. 
NOTE: remember to not always set the max-age, because then the max-age is always active.
- no-cache: set this to make sure no proxy or browser caches the response.
- no-store: tells caches not to cache the response anywhere
- no-transform: on the way from the server to the client no transformations are allowed, like compression of images or transcoding images to other size or types.
- s-maxage: means shared max-age and sets it for a shared cache and it will always outweigh the **max-age** and **Expire** directive. 
Shared cache is all the public caches, so no private caches, under te same URL.
- must-understand: must be coupled with no-store directive, so it can be ignored when it does not understand. Understanding means the proxy knows how to handle the response code in the message. 
- private: response may only be stored in a private cache such as a browser's cache.
- public: explicitly tells it may be stored in a shared cache. For example if you want to put a Authorization header in the shared cache (this is normally private cache) 
- immutable: the server tells the client that the response will not be updated. Handy for static assets such as images, css files, javascript you would think. 
Think twice because it will not expire unless the cache is removed. That means versioning these assets with hashes or version numbers..... 
- stale-while-revalidate: setting a time to keep a cache stale while revalidation with the server is busy. If it exceeds this period, clients just get an 502/504 error. 
During this period the client receives the stale cache and later when revalidation is completed receives a second response. So asynchronous.
- must-revalidate: must revalidate tells caches that they must revalidate with the server after freshness (**max-age** / **Expire**) expires. 
- proxy-revalidate: same as must-revalidate only specific for proxy servers and CDNs
- stale-if-error: tells that the stale caches may be used when one of the following error is thrown by the server **500 Internal Server Error**, **502 Bad Gateway**, **503 Service Unavailable**, **504 Gateway Timeout**

### Clear-Site-Data

Important Server-side HTTP Header used to clear certain or all browser's data. Used with one or more quoted strings to clear:
- `Clear-Site-Data: "cache"`: clear all cache resources (e.g. images, css, html, js. files) and Cache API (service worker cache)
- `Clear-Site-Data: "cookies"`: clear all cookies which is often done for security and privacy related use-cases like logout, account deletion or invalidate all sessions on security breach.
- `Clear-Site-Data: "storage"`: clear the local/session storages and indexedDB's
- `Clear-Site-Data: "executionContexts"`: reload all tabs and iframes etc.  
- `Clear-Site-Data: "*"`: clear all of the above  

### Expires

Informs the client when a response expires e.g. `Expires:  Wed, 01 Jun 2022 08:00:00 GMT`. Noteworthy; `0` means that the response is already expired

### Pragma

Legacy header that has `no-cache` as its only option. 
It is used in http1.0 to make sure the client always gets a fresh response and not a cached response. 
Same as the no-cache header in `Cache-Control`. 

## 3. Client Hints

### Accept-CH

A HTTPS only header that is not supported by all browsers and servers.
It is a response header where the server tells the client it accepts Client Hints (CH) and lists (comma serated) which hints it would like to get.
`Accept-CH: width`

### Device hints

- `Sec-CH-Device-Memory`: tells the amount of RAM the device has
- `Sec-CH-DPR`: tells the Devices Pixel Ratio where 0 is the default (e.g. 100x200 px) and if 2 is received the value gets doubled
- `Sec-CH-Width`: inform about the width of the screen, also integer value that compares to greater or equal to zero
- `Sec-CH-Viewport-Width`: viewport width equal or greater than zero
- `Sec-CH-Viewport-Height`: viewport width equal or greater than zero

### Network hints

- `Sec-CH-Save-Data`: Hint that tells the server that this client prefers that minimal data is sent
- `Sec-CH-RTT`: Short for Round Trip Time measured in milliseconds to inform client of latency
- `Sec-CH-Downlink`: Client downstream speed measured MB's per second 
- `Sec-CH-ECT`: Effective Connection Type that describes the ranges of RTT/Downlink 
  - Slow-2g: Min RTT 2000ms, Max Downlink 50Kbps. Well suited for small data exchanges such as a text document. 
  - 2g: Min RTT 1400ms, Max Downlink 70Kbps. This level of network performance is suitable for transferring small image files.
  - 3g: Min RTT 270ms, Max Downlink 700Kbps. This type of network can support high-quality audio and video transmission. 
  - 4g: Min RTT 0ms, No Max Downlink. A network with this performance can handle HD video and real-time streaming.

### User Hints

- `Sec-CH-Prefers-Color-Scheme`: indicates the client’s preferred color scheme. Valid options are “light” and “dark”.
- `Sec-CH-Prefers-Reduced-Motion`: indicates if the clients wants less motion in media with valid parameters “no-preference” and “reduce”
- `Sec-CH-Prefers-Transparency`: indicates if the client wants less transparancy in media on the page with valid parameters are “no-preference” and “reduce”

These are 3 of [many more](https://http.dev/client-hints)

## 4. Conditionals

### Last-modified, If-Modified-Since, 
Response header `Last-modified` that probably does exactly what you think. It shows the timestamp the server thinks te response is last modified
`Last-modified: Wed, 01 Jun 2022 08:00:00 GMT`

It works closely together with the `If-Modified-Since` requests header because it then checks the timestamp and conditionally do something. 
Next to the `Last-modified` header, the header is used to refresh cashes that do not have ETag.
`If-Modified-Since: Wed, 01 Jun 2022 08:00:00 GMT`

Consequently, the `If-Unmodified-Since` header also checks if a request is unmodified since. Commonly used to check if a PUT request does not overwrite another request.
`If-Unmodified-Since: Wed, 01 Jun 2022 08:00:00 GMT`

### ETag
Short for Entity tag which can be done Strong `ETag: "<ETag-value>"` and weak `ETag: W/"<ETag-value>"`. 
Normally the value is filled with an identifier like UUID, MD5, version or something else. Weak ETags can be used when less significant information is communicated.
Strong ETags are used when byte-for-byte comparisons are needed to check resources are exactly the same.

### If-match
Request header that is sent to the server and checks if the ETag header matches or else `412 Precondition Failed`. 
`If-Match: "1234567890"`

### If-none-match
Request header that looks at ETag headers and succeeds when None-match the value that is in the header. `If-None-Match: "1234567890"`

### Vary
Response header that keeps track of all relevant HTTP headers that were included in the request. So if a `accept-language` header is used, the vary header also shows 
```
HTTP/1.1 200 OK
Vary: Accept-Language
```

### Delta-base

It is an indicator when the content of the response is no data, but in delta (increase performance) and Delta-base indicates which version of the delta it represents so the right data can be updated.
Below you can see that the requests says that it supports delta through the `IM` header.  The response then react by saying hey I have a new delta based on v1 and the new version is in the ETag v2.
What happens is that the data is updated and set to v2 so next time a newer version is requested.  
```
GET /resource HTTP/1.1
Host: example.com
IM: delta
If-None-Match: v1

HTTP/1.1 226 IM Used
Content-Type: application/json
Content-Length: 256
ETag: "v2"
Delta-Base: "v1"

{
"delta": "delta content representing changes"
}
```

## 5. Connection Management
This has the `connection` header with two options `close` and `keep-alive` with another header with `Keep-alive` options, but this is ignored or rejected by most browsers. 
So do not use this functionality is all I can say

## 6. Content negotiation

### Accept
Request header that tells the server which MIME types are allowed based on `type/subtype` so;

```html
Accept:text/* ##Type text is allowed of all subtypes (text/html and text/plain)
```

```html
Accept:*/* ##All types and all subtypes are allowed
```

Lastly a `q` parameter can be included to indicate which priority is set to a header. Where the default is 1 with most priority and 0,9 etc is less so less priority.
```html
Accept: text/html, text/plain;q=0.9, text/*;q=0.8, */*;q=0.7
```

### Accept-encoding, Content-Encoding

Request header `Accept-Encoding` and the response header `Content-Encoding` shows which encodings are requested and used. 

A short list of commonly used encodings:
- `indentiy`: plain text and no encodings 
- `gzip`: compress and decompresses message
- `br(brotli)`: compress/decompress message (should be quicker as gzip)
- `zstd(zStandard)`: compress/decompress message (often quikcer than br and gzip)

This header can also be specified with the `q` option
`Accept-Encoding: gzip, deflate, br;q=1.0, identity;q=0.5, *;q=0.25`

### A-IM and IM
Request header `A-IM` is also a header that tells the server about accepted encodings and its response `IM`. It also uses the `q` option to indicate priorities

It has the same encodings as the @Accept-Encodings and some more;
- vcdiff: delta using vcdiff encode format
- diffe: output of unix diff-e command
- gdiff: encodings format
- deflate: encodings format
- deflate-raw: encodings format like deflate but without the metadata
- range: nice to indicate how requests can be done in portions e.g. `Range: bytes=500-999`. Nice for streaming media.

### Accept-language, Content language

Request header `Accept-Language` that tells which languages the client accepts and in the response header the `Content-Language` is set to tell them what is returned. 
Again the `q` option can be used to tell the server the priority

```html
Accept-Language: en-UK, en, de;q=0.5
```

## 7. Controls

### Expect
The request header `Expect` is used to check if the HTTP request meets the requirements based on all its headers. 
It sents a `Expect: 100-continue` and expects a `100-continue` response or `417 Expectation Failed`. 
It can be used before the client tries to sent large data messages, so that the request will not fail after 5 min of transferring for example.
Next to transfers, sometimes local storages cannot store anymore and sents a `417` when the a `Content-Length` header is too large.

### Max-forwards

The request header `Max-Forwards` is a header that is used for the HTTP TRACE and OPTIONS requests. In all other requests this header will be ignored. 
The value is a number which is decremented, each intermediary its passes until it is zero and cannot be forwarded anymore. 

`Max-Forwards: 3`

## 8. Cookies, Set-Cookie

The request header `Cookie` is used to send the data the client stores for the server. A server uses the `Set-Cookie` data on the client and expects it in each request. 

Response
```html
HTTP/1.1 200 OK
Content-Type: text/html
Set-Cookie: items=16
Set-Cookie: headercolor=blue
Set-Cookie: footercolor=green
Set-Cookie: screenmode=dark, Expires=Sun, 1 Jan 2023 12:00:00 GMT
Set-Cookie: job=111; Max-Age: 3600; Secure; HttpOnly
```
Request
```html
GET / HTTP/1.1
Host: www.example.re
Cookie: items=16; headercolor=blue; screenmode=dark; job=111
```

## 9. CORS

### Access-Control-Allow-Origin
Response header that states the allowed origins for the response. E.g. a server allows a url `Access-Control-Allow-Origin: https://example.re`
With `*` all origins are allowed and with `null` it shows the absence of a origin network (Client local storage e.g. file:// or locally open HTML file on pc)

When credentials are send together with the `*`, you always get a CORS error

### Access-Control-Allow-Credentials
When in the request `credentials: include` is set, the request has the credentials sent. 
When this header is set to `true` (it is never anything else) the credentials are also allowed by the server

`Access-Control-Allow-Credentials: true`

### Access-Control-Allow-Headers
Response header that tells the client which headers are allowed to send in the request to the server e.g. `Access-Control-Allow-Headers: Accept, X-User-Addr`. 
The `*` is the wildcard and allows all headers (of requests without credentials)

### Access-Control-Expose-Headers
Resonse header that tells the client which headers are included(exposed) in the response. So which headers can be read from the response.

### Access-Control-Max-Age
Response header that tells the client how long its preflight request's response can be cached (default 5 sec)
`Access-Control-Max-Age: 600`

preflight request are requests that a client makes before the actual request like GET/PUT/POST

### Access-Control-Request-Headers
Request header that the client sends to the server to tell in a preflight request what header will be sent. 

### Access-Control-Request-Method
Request header that the client sends to the server to tell in a preflight request what HTTP Method will be sent
`Access-Control-Request-Method: GET`

### Timing-Allow-Origin
Response header that grants an ORIGIN the rights to use the `Resource Timing API` which has information about performance data such as loading times of images. 
It is set to a CORS url or with `*` and is adviced to be used for analytic environments.

## 10. Downloads

### Content-disposition
Response header to tell the client how the content should be handled in the browser. 
There are 3 directives of which one is solo and the other 2 are linked. `inline` tells the client to use the response inside the webpage.
`attachment` is to tell the client that it should be downloaded and the `filename` directive adds a filename.

`Content-Disposition: attachment; filename="document.doc"`

However, there is also Mutlipart Response body where the body is of type `multipart/form-data`. In this case `form-data` directive is used.
`Content-Disposition: form-data; name="<field-name>"`. There is more to it, but putting multiple files in a single field is hardly used.

## 11. Message body information

### Content-Length
Body length expressed in bytes
### Content-Type
Body type with 3 directives possible. The `media type` or `MIME` type, the `charset` and the `boundary` indicator when it is a mutlipart body
`Content-Type: multipart/form-data; boundary=-----`.
### Content-Location
Indicates an url to another location where a resource is available. `Content-Location: <resource_url>`

## 12. Proxies
### Forwarded
Request header with information about the client and proxy. `Forwarded: for=192.168.0.1;proto=https;by=192.168.1.100;host=example.com`
`for` option indicates the `ip/unknown/other identifier` about the client and `by` option does the same about the proxy. 
`proto` is short for protocol used and the host is clients url.

### Via
The route a request or response travels through proxies described by the following options; `protocol name`(optional), `protocol version`, `host`, `port`(optional) and `alias`(optional).

Example; `Via: HTTP/1.1 proxy.example.re, 1.1 edge_1`

## 13. Redirects
### Location

The HTTP Location header is used to inform the client of where to find the resource either after a redirection or the creation of new content.

## 14. Request context
### From
`From: mail-list@example.re` is a request header to inform the server about client's owner

### Host
Request header with the url of the client host

### Referer (sic)
Request header that does the same as the Host header and added all the other parts of the request such as query strings. 
Next to the fact that it is misspelled, the header is not to be used because it is subject to privacy issues.

### Referrer-Policy
* no-referrer: tells no referer header is sent
* no-referrer-when-downgrade: indicates that when security goes from https to http no referer header is included.
* origin: indicates only the origin host will be included
* origin-when-cross-origin: indicates that only origin will be sent when security is downgraded to http or cross-origin requests are made
* same-origin: indicates the referer header is not sent when cross-origin
* strict-origin: origin will only be sent if the security level stays the same
* strict-origin-when-cross-origin: this is the default policy and is the same as strict origin, but also does not allow cross-origin
* unsafe-url: no secure policy and allows every part of the url (origin, path, and query string)

### User-Agent
Information about the client's browser e.g. `User-Agent: Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)`

## 15. Response context

### Allow
Response header that tells the client which HTTP methods are allowed e.g. `Allow: GET, PUT` for a specific resource/api/endpoint

### Server
Response header that tells the client which type of software is handling the request e.g. `Server: nginx`. 

## 16. Range requests

### Accept-Ranges
Response header that has only `none` and `bytes` as options. Only the `bytes` option has meaning namely to tell the client it can receive partial resources 
in the requests such as files to download. It is common used to indicate that it is possible for the client to continue with partial transferred file transfers. 

### Range & Content-Range
Request header that actually requests a partial download 

```html
GET /largeimage.jpg HTTP/1.1
Host: www.example.re
Range: bytes=0-999
```

Here a large image is requested partially, the first 1000 bytes of the file.
```html
HTTP/1.1 206 Partial Content
Content-Type: image/jpeg
Content-Length: 1000
Content-Range: bytes 0-999/25000

<1000 bytes of image data are included in the message body>
```
The response is a 206, shows the `Content-Length`, the `Content-Range` to verify the first 1000 bytes of the total of 25000 bytes (`*` when unknown is also allowed)

### If-Range
Request header that indicates a condition. When the condition is met, the server should accept the `range` else the server throws a `412 Precoondition failed`. 
The header should contain a `ETag` or `Last-modified` condition

`If-Range: Wed, 01 Jun 2022 08:00:00 GMT`

## 17. Security
### Cross-Origin-Embedder-Policy (COEP)
Response header that indicates that it is granted access (`unsafe-none`) to cross-origin without using the CORS protocol (this is the default). 
It can be set to `require-corp` which determines the client should have the same origin or should make use of the CORS protocol and be allowed.

````html
Cross-Origin-Embedder-Policy: require-corp
Cross-Origin-Opener-Policy: same-origin
````

### Cross-Origin-Opener-Policy (COOP)
Response header that tells the client if everything is allowed (`unsafe-none`), which is the default. 
The other options are that a client can share its browsing context (`same-origin-allow-popups`) in the same origin and popups tabs/screens, 
the `same-origin` limits it to the initial screen.

`Cross-Origin-Opener-Policy: same-origin`

### Cross-Origin-Resource-Policy (CORP)
Response header that instructs the client to protect against certain resources. Most strict is the `same-origin` which makes sure the same origin is used. 
So example.com vs dev.example.com is not allowed. The `same-site` directive is allowing the same site, so previous example is allowed here. `cross-origin` removes the 
restrictions of eh resource.

`Cross-Origin-Resource-Policy: same-origin`

### Content-Security-Policy (CSP)

This response header allows servers an amount of control over how the resources can be used by clients. Delimiter is `;`

#### Document Directives
* `base-uri`: Restricts URLs for the <base> element in the index.html so no other uri's can be used `Content-Security-Policy: base-uri 'self'`.
* `sandbox`: Creates a sandbox for the requested resource, similar to the iframe sandbox attribute. 
When set directly it is a real sandbox and nothing is allowed, but when you use tokens you can specify it
  * allow-forms: Allows form submissions.
  * allow-modals: Allows the use of modal dialogs.
  * allow-orientation-lock: Allows locking the screen orientation.
  * allow-pointer-lock: Allows the use of the Pointer Lock API.
  * allow-popups: Allows popups (e.g., window.open, target="_blank" links).
  * allow-popups-to-escape-sandbox: Allows sandboxed content to open new browsing contexts without inheriting the sandboxing.
  * allow-presentation: Allows starting a presentation session.
  * allow-same-origin: Treats the content as being from the same origin instead of a unique origin, allowing access to resources like cookies and storage.
  * allow-scripts: Allows the execution of scripts.
  * allow-storage-access-by-user-activation: Allows the document to access storage (e.g., cookies, localStorage) in response to user activation.
  * allow-top-navigation: Allows the document to navigate the top-level browsing context.
#### Fetch Directives
* `child-src`: Defines sources for nested browsing contexts and web workers.
* `connect-src`: Restricts URLs that can be loaded using scripts.
* `default-src`: Acts as a fallback for all other fetch directives.
* `font-src`: Specifies valid sources for fonts using CSS @font-face.
* `frame-src`: Specifies valid sources for nested browsing contexts like frame and iframe.
* `img-src`: Specifies valid sources for images and favicons.
* `manifest-src`: Specifies valid sources for application manifest files.
* `media-src`: Specifies sources for loading media, including audio, video, and track elements.
* `object-src`: Specifies valid sources for object, embed, and applet elements.
* `prefetch-src`: Specifies sources to be prefetched or prerendered.
* `script-src`: Specifies valid sources for JavaScript.
* `script-src-elem`: Specifies valid sources for JavaScript script elements.
* `script-src-attr`: Specifies sources for JavaScript inline event handlers.
* `style-src`: Specifies valid sources for CSS stylesheets.
* `style-src-elem`: Specifies valid sources for stylesheet style elements and link elements with rel="stylesheet".
* `style-src-attr`: Specifies valid sources for inline styles applied to DOM elements.
* `worker-src`: Applies to Worker, SharedWorker, and ServiceWorker scripts.
#### Navigation Directives
* `form-action`: Restricts URLs for form submission targets.
* `frame-ancestors`: Specifies valid parents that can embed the page using frame, iframe, object, embed, or applet.
* `navigate-to`: Restricts URLs the document can navigate to.
#### Reporting Directives
* `report-uri`: Specifies where to send reports of content security policy violations.
* `report-to`: Specifies where to report violations.

```json
Report-To: {
  "group": "default-reporting-group",
  "max_age": 86400,
  "endpoints": [
    { "url": "https://example.com/report-endpoint" }
  ],
  "include_subdomains": true
}
```

#### Other Directives
* `require-sri-for`: Requires Subresource Integrity (SRI) for scripts or styles.
* `require-trusted-types-for`: Enforces use of Trusted Types policies for DOM XSS injections.
* `trusted-types`: Specifies Trusted Types policies to restrict DOM XSS injections.
* `upgrade-insecure-requests`: Treats all URLs as secure, even if served over HTTP.


### Content-Security-Policy-Report-Only
A response header for developers to test content security policies and just report them. With the following directives;
* The `blocked-uri` directive indicates the URI of the resource that was blocked by the content security policy. If the origin differs from the document-uri then it is truncated to just the scheme, host, and port.
* The `document-uri` directive is the URI of the document that caused the violation.
* The `disposition` directive is set to either report or enforce. This is reflective of whether the Content-Security-Policy-Report-Only header is set.
* The `effective-directive` directive refers to the directive that was violated, or whose enforcement led to the policy violation.
* The `original-policy` directive indicates the original policy, as specified by the HTTP Content-Security-Policy-Report-Only header.
* The `referrer` directive contains the referrer of the document that caused the policy violation.
* The `script-sample` directive contains the first 40 characters of the code that caused the violation. This may be the beginning of an inline script, event handler, or style.
* The `status-code` directive refers to the HTML status code of the relevant resource.
* The `violated-directive` directive contains the name of the policy section that was violated.

### Strict-Transport-Security (HSTS)
Reponse header that tells that all connections should be HTTPS or will be upgraded to HTTPS. It has 2 directives that tells something about the HTTPS connection

`Strict-Transport-Security: max-age=31536000; includeSubDomains`

`max-age` here states that HTTPS is enforced for still one year and also includes all it subdomains (`includeSubDomains`)

### Upgrade-Insecure-Requests
This requests header from the client tells the server to upgrade the response to https which is authenticated and encrypted

### X-Content-Type-Options
`X-Content-Type-Options: nosniff` which is set to enforce that the response MIME type (e.g. `application/json`) so it will not be changed.

## 18. Fetch metadata request headers

### Sec-Fetch-Site

This request header to tell the server what relationship there is between the client and the resource.
`cross-site` mean two different sites and `same-site` the opposite. Then there is, `same-origin` that tells that the scheme, host, port is all the same. 
Lastly, `none` means a direct request by a user via a bookmark or just typing the url in the address bar.

```html
Sec-Fetch-User: ?1
Sec-Fetch-Dest: image
Sec-Fetch-Mode: navigate
Sec-Fetch-Site: same-origin
```
In the example all the metadata request headers are used 

### Sec-Fetch-Mode

The request header which tells the server something about what kind of request is made. The directives are;
- `no-cors`: tells NO CORS protocol is used in this request
- `cors`: tells the CORS protocol is used in this request
- `navigate`: indicates the request is initiated by navigation within a html document
- `same-origin`: the same scheme/host/port is used so it must be from same origin
- `websocket`: A clients requests a websocket connection

### Sec-Fetch-User

This requests headers only use is when to indicate a user initiated the request. So again the address bar, hyperlink or browser's bookmark. 
In Angular for example, all requests are typically initiated programmatically so by default these request never include this header. 

### Sec-Fetch-Dest

This request header is to tell the server the intended use. So all the directives are types of intended use:

- `audio`: The intended usage is audio data.
- `audioworklet`: The resource is intended for use by an audio worklet.
- `document` : After clicking a link, the user expects to use the resource as an HTML or XML document.
- `embed`: The destination type for this resource is embedded content.
- `empty`: An empty string is the intended destination, although it is used for destinations that do not have a value of their own. Examples of this are WebSocket, XMLHttpRequest, and more.
- `font`: The destination type is a font, which may have originated from a CSS.
- `frame`: The destination is a frame, which may have originated from a CSS.
- `iframe`: The destination is an iframe, which may have originated from a CSS.
- `image`: The intended use is as an image.
- `manifest`: The intended use is as a manifest.
- `object`: The destination type is an object.
- `paintworklet`: The intended use is as a paint worklet.
- `report`: The destination is a report, such as a content security policy report.
- `script`: The intended use is as a script.
- `serviceworker`: The destination type is a service worker.
- `sharedworker`: The destination type is a shared worker.
- `style`: The destination is a style, possibly originating from a CSS.
- `track`: The track type is an HTML text track.
- `video`: The resource is expected to be used as a video.
- `worker`: The destination type is a worker.
- `xslt`: The intended use is as an XLST transform.

## 19. Server-Sent events

### NEL

NEL stands for Network Error Logging and is a response header. The server informs the client via this header which reports and network request information to collect.

As you can see in the example the header has a JSON value and in the example all required fields are commented

```html
NEL: {
"report_to": "default-reporting-group", //required
"max_age": 86400, //required
"include_subdomains": true,
"success_fraction": 0.0,
"failure_fraction": 1.0,
"request_headers": ["Sec-Fetch-Mode", "Sec-Fetch-Site"],
"response_headers": ["Content-Type", "Content-Length"]
}
```

- `report_to`: works together with the report-to header in section 17. If that group-name is for example `default-reporting-group` here we also use that name
- `max_age`: lifetime of the policy in seconds and 0 indicates the policy should be removed
- `include_subdomains`: which is always false when not included or a false value. So the only value here should be `true` when we want to include subdomains
- `success_fraction`: value between 0.0 and 1.0 which is a sampling rate. For succesful requests the 0.0 is quite normal as there requests are less important to report or are less data to consume. 
- `failure_fraction`: same sampling rate on the failed requests which can be set to 1.0 (all request) or lower to consume less data traffic to the reporting endpoint.
- `request_headers`: of the request/response that is failed/succeeded we can specify which headers we would like to include of the request
- `response_headers`: of the request/response that is failed/succeeded we can specify which headers we would like to include of the response


## 20. WebSockets

### sec-websocket-accept
This response header tells the client the server accepts the websocket request. The value of the header is a hashed key

`Sec-WebSocket-Accept: OWQ4NjdjMDFhN2FiOGUwYzA4ZjYxNDc5MWFlMDQ2ZTViZDA1OWIzOA==`

## 21. Other

### Alt-Svc
This response header is useful to let clients know that for future request another service can be used. This is done to handle the traffic load more efficiently. 
It has the following directives and can have multiple headers to indicate multiple alternative services:

- `clear`: invalidate previous alternative services
- `protocol-id`: this directive is normally `h2` for secured http/2 (https) - but since a few years also `h3-23` (at time of writing) for http/3
- `alt-authority`: the directive to direct to the host:port of the alternative service
- `ma`: `max-age` directive that is normally 24 hours when not set
- `persist`: tells the client to persist the alternative service even when the ip/port/transport-protocol/network-path changes and continue using it until the `ma` expires or a `clear` directive has been used.

`Alt-Svc: h3-23=":443"; ma=3600`

### Date

This header is used in the request and response headers. It sets the date in the following format:
`Date:  Wed, 01 Jun 2022 08:00:00 GMT`

### Link
These Link header relations provide hints and directives to browsers and search engines for optimizing resource loading, prefetching, rendering, and SEO considerations.

NOTE: the `canonical` header, which is handy when you make use of subdomains.

`Link: <https://example.com>; rel=preconnect` - Specifies resources that the page will need very soon, like establishing a TCP connection
`Link: <https://example.com>; rel=dns-prefetch` - Hints to the browser to perform DNS resolution for specified domains in advance.
`Link: <https://example.com/resource>; rel=prefetch` - Indicates resources that will be needed in the future, encouraging the browser to fetch them early.
`Link: <https://example.com/page>; rel=prerender` - Suggests that the browser should render and load the specified resource in the background.
`Link: <https://example.com/style.css>; rel=preload; as=style` - Directs the browser to fetch and cache the specified resource as soon as possible, without rendering.
`Link: <https://example.com/canonical>; rel=canonical` - Specifies the preferred URL for a resource to avoid duplicate content issues in search engines.
`Link: <https://example.com>; rel="alternate"; hreflang="es"` - Indicates the language and optional geographical restrictions of a document’s alternative URLs.

### Retry-After
This response header tells the client in seconds how long it should wait to try a resource again.

`Retry-After: 30`

### Server-Timing

A metric header which can be used in both request and response to tell something about some timing. It is build with <name>;desc="description";dur=15.1 and can look like this:

```html
Server-Timing: cache
Server-Timing: cache;desc=”Read from Cache”
Server-Timing: cache;desc=”Read from Cache”;dur=15.1
```

### Sourcemap

An url to a source map so that the client can download the source code to render the website. the URL can be relative or absolute;
`SourceMap: /documents/src/show.js.map`
