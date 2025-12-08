# Information security - CIA

- Confidentiality - bizalmasság: az információ csak az arra felhatalmazottak számára legyen elérhető
- Integrity - sértetlenség: információk és a feldolgozási módszerek teljességének és helyességének
- Availability - rendelkezésre állás: felhasználók mindig hozzáférjenek az információkhoz

# JCA

Java Cryptography Architecture

https://docs.oracle.com/en/java/javase/25/security/java-security-overview1.html#GUID-2EF91196-D468-4D0F-8FDC-DA2BEA165D10
https://www.jtechlog.hu/2011/02/05/elektronikus-alairas-es-alkalmazasa.html
https://www.jtechlog.hu/2008/12/02/minositett-elektronikus-alairas.html

## Elektronikus aláírás

Céljai:

- hitelesség (authenticity): a dokumentum származásának igazolása
- sérthetetlenség, integritás (integrity): a dokumentum tartalma nem változott annak aláírása óta
- letagadhatatlanság (non-repudiation): az aláíró nem tudja letagadni, hogy ő írta alá a dokumentumot, és az aláíró személy kiléte jogilag is bizonyítható eredjű

Titkosítás célja:

- titkosság (privacy, confidentiality): a kommunikáló feleken kívül más nem szerezhet tudomást a dokumentum tartalmáról

## Provider alapú architektúra

`ProvidersMain`

## BASE64

`Base64Main`

Parancssor:

```shell
echo -n "Hello World!" | base64
echo "SGVsbG8gV29ybGQh" | base64 -d
```

https://www.base64decode.org/

## HexFormat

```java
HexFormat hex = HexFormat.of();
System.out.println(hex.formatHex(bytes));
```

## Hash

Hash:

- Lavinahatás: ha a dokumentum változik, annak hash lenyomata is szignifikánsan változzon
- Ütközésmentesség: lehetetlen vele megegyező hash-ű értelmes dokumentumot legyártani

Milyen algoritmus használható: NIST – SP 800-131A (Transitioning the Use of Cryptographic Algorithms)

- MD5 tilos
- SHA-1 tilos
- SHA-256-től ajánlott

Algoritmusokra nincsenek konstansok definiálva

`HashMain`

## Random generator

- Pseudo-Random Number Generators (PRNG)
- Cloudflare - lava lamp pool, [Lavarand](https://en.wikipedia.org/wiki/Lavarand)
- Enhanced Pseudo-Random Number Generators, `java.util.random` csomag - JEP 356

```java
RandomGeneratorFactory.all()
                .map(fac -> fac.group() + ":" + fac.name()
                        + " {"
                        + (fac.isSplittable() ? " splittable" : "")
                        + (fac.isStreamable() ? " streamable" : "")
                        + (fac.isJumpable() ? " jumpable" : "")
                        + (fac.isArbitrarilyJumpable() ? " arbitrarily-jumpable" : "")
                        + (fac.isLeapable() ? " leapable" : "")
                        + (fac.isHardware() ? " hardware" : "")
                        + (fac.isStatistical() ? " statistical" : "")
                        + (fac.isStochastic() ? " stochastic" : "")
                        + " stateBits: " + fac.stateBits()
                        + " }"
                )
                .sorted().forEach(System.out::println);
```

```java
var generator = RandomGeneratorFactory.of("Legacy:Random").create();
```

- Nem thread-safe
- `create()` metódusnak paraméterül átadható seed is
- `ints()`, `longs()`, `doubles()` metódusok Streammel térnek vissza
- `nextInt()`, `nextInt(bound)`, `nextInt(origin, bound)`
- `nextBoolean()`, `nextBytes()`, `nextLong()`, `nextFloat()`, `nextDouble()`

Változott Random osztály

- `implements RandomGenerator`
- `@RandomGeneratorProperties`
- Sok default metódus a `RandomGenerator` interfészben, `RandomSupport`-nak átadja önmagát

```java
default int nextInt(int origin, int bound) {
    RandomSupport.checkRange(origin, bound);

    return RandomSupport.boundedNextInt(this, origin, bound);
}
```

- `new SecureRandom()`
  - Létrehoz egy alapértelmezett SecureRandom példányt.
  - Az algoritmust a platform dönti el (SHA1PRNG gyakori a Java-ban, de rendszerenként változhat).
  - Nem feltétlenül a legerősebb entropiaforrást használja.
  - Nem blokkolja a szálat inicializáláskor – azonnal létrehozható.
  - Gyorsabb inicializálás, de bizonyos környezetekben kevésbé biztonságos lehet kriptográfiai szempontból.
- `SecureRandom.getInstanceStrong()`
  - Lekéri a platform legerősebb elérhető `SecureRandom` implementációját.
  - A `java.security` konfiguráció szerint az erős algoritmus általában `/dev/random` Linuxon vagy Windows CNG API.
  - Blokkolhat az inicializáláskor, ha az operációs rendszer szerint nincs elég entropia.
  - Biztonságilag erősebb, kriptográfiai célokra ajánlott (pl. kulcsgenerálás).
  - Ha többször kell, érdemes cache-elni, pl. threadlocal-ban

`SecureRandomMain`

## Cipher

- Szimmetrikus titkosítás

Security by Obscurity kerülendő: algoritmus nem titkos

Confusion és Diffusion:

- Kriptoanalízist lassító tulajdonságok (pl statisztika)
- Segítenek elrejteni a kulcs és a rejtjelezett üzenet közötti kapcsolatot
- Confusion: a rejtjelezett üzenet egy bitje a kulcs több részétől függjön
- Diffusion: az adat egy bitjének a változása esetén a rejtjelezett üzenet fele változzon meg

Algoritmusok:

- AES - blokkos - ajánlott
- ChaCha20 — streamen - ajánlott
- RC4 - tilos
- Blowfish, Twofish régebbi
- DES - tilos
- 3DES - tilos

Kulcs generálása

AES esetén IV (Initialization Vector - nonce):

- Ugyanazt az üzenetet ne küldjük ugyanazzal az IV-vel
- IV nem titkosított, át kell küldeni a titkosított résszel együtt
- Pl. összekonkatenálva, vagy JSON-ben külön mezőben

Electronic Codebook (ECB) az egyik legegyszerűbb blokkrejtési üzemmód:

- A titkosítandó adatot fix méretű blokkokra bontja
- Minden blokkot ugyanazzal a kulccsal, egymástól függetlenül titkosít.
- Az ECB legnagyobb hátránya, hogy mintázatokat megőriz. [The ECB Penguin](https://words.filippo.io/the-ecb-penguin/)

GCM – Galois/Counter Mode

- Teljes neve: Galois/Counter Mode
- Típus: Működési mód (mode of operation) az AES blokkkódoláshoz
- Fő jellemzői:
  - Counter Mode (CTR) alapú titkosítás
    - Minden blokkhoz egy számláló (counter) tartozik
    - Ezt titkosítja
    - Ezzel XOR-olja a blokkot
    - Párhuzamosítható
  - Galois Field Authentication
    - Biztosítja az integritást és hitelességet (authentication), nem csak a titkosítást.
    - Minden üzenethez generál egy auth tag-et (pl. 128 bit), ami ellenőrizhető dekódoláskor.
  - NoPadding kompatibilitás
    - Mivel CTR alapú, a blokkméret nem kell 16 bájtra kerekíteni, így NoPadding-et lehet használni.

`CipherMain`

## MAC

- Aláírás szimmetrikus kulccsal

`MACMain`

- A küldő is aláírja, és a fogadó oldalon a fogadó is aláírja a titkos kulccsal, és összehasonlítja
- Összehasonlítás a `MessageDigest.isEqual()` metódussal
  - Ez is hosszt és bájtokat hasonlít
  - Időzítésmentes összehasonlítás: ne álljon le korábban akkor sem, ha már eltérést talált, ekkor
    ugyanis az eltelt idő ugyanis információtartalommal bír a támadó számára

## PKI, kulcspárok

- Modulus két nagy prím szám szorzata: `n=p×q`
- Publikus kulcs
  - `(n, e)`, e - public exponent
- Privát kulcs
  - `(n, d)` - private exponent

publikus kulcs X.509 formátumban

```
SubjectPublicKeyInfo ::= SEQUENCE {
    algorithm AlgorithmIdentifier,
    subjectPublicKey BIT STRING
}
```

privát kulcs PKCS#8 formátumban

`-----BEGIN PRIVATE KEY-----`

```
PrivateKeyInfo ::= SEQUENCE {
    version Version,
    privateKeyAlgorithm PrivateKeyAlgorithmIdentifier,
    privateKey PrivateKey,
    attributes [0] IMPLICIT Attributes OPTIONAL
}
```

`KeyPairMain`

## Tanúsítvány generálása

- JCE nem tartalmaz, ezért kell a BouncyCastle
- Tanúsítvány adatokból először egy hash készül, és az kerül aláírásra a titkos kulccsal
- X509 alapú tanúsítvány formátumok
  - PEM (Privacy Enhanced Mail) formátum:
    - Legelterjedtebb
    - BASE64, fejléc + lábléc
    - `-----BEGIN CERTIFICATE-----`
    - Windows nem tud megnyitni
    - [PEM decoder](https://report-uri.com/home/pem_decoder)
    - Lehet benne tanúsítvány, privát kulcs, lánc
    - Linux, webkiszolgálók (Apache, NGINX), OpenSSL
  - DER
    - Windows meg tud nyitni
    - Bináris
- X.500 szabvány hálózati szabványok gyűjtője, ebből egy a X.500 distinguished name
  - hierarchikus struktúrát lehet megadni, ezek közül a leggyakoribbak az ország (C - country), állam vagy tartomány (ST - state or province), város (city or locality - L), szervezet (O - organization), szervezeti egység (OU - organization unit) és általános név (CN - common name)

`CertificateBouncyCastleMain`

## Kulcstár

- Formátumok:
  - JKS - Java specifikus, deprecated, `.jks` kiterjesztés
  - PKCS12 - `.p12` kiterjesztés, Windows be tudja importálni

`CertificateBouncyCastleMain`

## Aláírás

- Érdemes lenne csak a publikus kulcsot egy külön kulcstárba felvenni

`SignMain`, `VerifySignMain`

## Tanúsítványlánc

CA:

- Man in the middle: ha Bobnak nem Alice adja oda a publikus kulcsát, hanem egy rosszindulatú harmadik személy adja át a sajátját Alice nevében, akkor az Alice-nek szánt dokumentumokat képes ő kibontani.
- Ezért egy megbízható harmadik félre van szükség, aki igazolja, hogy az adott publikus kulcs kihez tartozik.

Tanúsítvány aláírása másik tanúsítványhoz tartozó publikus kulccsal

`CertificateChainMain`

## Quantum-Resistant Module

[JEP 496: Quantum-Resistant Module-Lattice-Based Key Encapsulation Mechanism](https://openjdk.java.net/jeps/496)
[JEP 497: Quantum-Resistant Module-Lattice-Based Digital Signature Algorithm](https://openjdk.java.net/jeps/497)

# JAR fájl aláírása

## Aláírás

Kulcspár generálás, kulcstárba mentés

- Warning: Different store and key passwords not supported for PKCS12 KeyStores. Ignoring user-specified -keypass value.

```shell
keytool -genkeypair -dname "cn=Trainer, ou=Training, c=HU" -alias mykey -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore mykeystore.p12 -storepass storepass -validity 180
```

Generált kulcstár ellenőrzés

```shell
keytool -list -keystore mykeystore.p12 -storepass storepass -v
```

Projekt generálás

```shell
mvn archetype:generate -DgroupId=training -DartifactId=hello-world -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

Csomagolás

```shell
mvn package
```

Aláírás

```shell
jarsigner -storetype PKCS12 -keystore mykeystore.p12 -storepass storepass -signedjar target\signed-jar-1.0-signed-SNAPSHOT.jar  target\signed-jar-1.0-SNAPSHOT.jar mykey
```

- `META-INF/MANIFEST.MF` feleírja a fájlokhoz tartozó hash értéket
- `MYKEY.SF` tartalmazza a `MANIFEST.MF` hash-ét
- Ezt írja alá, aláírás a `MYKEY.RSA` fájlban

## Ellenőrzés

```shell
jarsigner -verify -verbose -certs target\signed-jar-1.0-signed-SNAPSHOT.jar
```

Ha átírtjuk pl. a `META-INF/maven/training/hello-world/pom.properties` fájl tartalmát:

```
C:\trainings\java-sec\hello-world>jarsigner -verify -verbose -certs target/hello-world-1.0-signed-SNAPSHOT.
jarsigner: java.lang.SecurityException: SHA-384 digest error for META-INF/maven/training/hello-world/pom.properties
```

## Ellenőrzés tanúsítvány alapján

Tanúsítvány alapján akarom ellenőrizni:

Először exportálni kell a publikus kulcsot PEM formában (`-rfc` kapcsoló):

```shell
keytool -exportcert -rfc -keystore mykeystore.p12 -storetype PKCS12 -alias mykey -file mycert.pem -storepass storepass
```

(Lehet importálni DER fájlba is, ekkor megnézhető Windowsban.)

```shell
keytool -exportcert -keystore mykeystore.p12 -storetype PKCS12 -alias mykey -file mycert.der -storepass storepass
```

Beimportálni egy külön kulcstárba:

```shell
keytool -importcert -file mycert.pem -keystore mycertstore.p12 -storetype PKCS12 -alias mycert -storepass storepass
```

```shell
jarsigner -verify -keystore mycertstore.p12 -storetype PKCS12 -storepass storepass -verbose -certs target\signed-jar-1.0-SNAPSHOT.jar
```

```xml
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jarsigner-plugin</artifactId>
        <version>3.1.0</version>

        <executions>
            <execution>
                <id>sign</id>
                <phase>package</phase> <!-- vagy verify -->
                <goals>
                    <goal>sign</goal>
                </goals>
                <configuration>
                    <keystore>${project.basedir}/mykeystore.p12</keystore>
                    <storepass>storepass</storepass>
                    <alias>mykey</alias>
                </configuration>
            </execution>
        </executions>
    </plugin>
</plugins>
```

- Verziókezelőben nem tárolunk kulcstárat és jelszót sem, ki kell tenni CI/CD eszköz secretbe

## HashiCorp Vault Backend

- Tárolni: tokenek, jelszavak, tanúsítványok, kulcsok
- Webes felület, CLI, HTTP API

```shell
docker run -d --cap-add=IPC_LOCK -e VAULT_DEV_ROOT_TOKEN_ID=myroot -p 8200:8200 --name=vault hashicorp/vault
```

- `IPC_LOCK` - érzékeny adatokat ne swappelje a diskre
- http://localhost:8200
- Token bejelentkezés, `myroot`

```shell
docker exec -it vault sh
```

Vault CLI lokális telepítése:

```shell
curl -fsSL https://apt.releases.hashicorp.com/gpg | sudo apt-key add -
sudo apt-add-repository "deb [arch=amd64] https://apt.releases.hashicorp.com $(lsb_release -cs) main"
sudo apt update && sudo apt install vault
```

```script
vault --version
```

```shell
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='myroot'
```

Rövid élettartamú token generálása:

```shell
vault token create -policy=root -display-name="bootstrap-root-$(date +%F)" -ttl=1h -orphan
```

Wrappelt token, csak 5 percen belül oldható fel

```shell
vault token create -policy=root -display-name="bootstrap-root-$(date +%F)" -ttl=1h -orphan -wrap-ttl=5m
```

```shell
vault unwrap [wrapping_token értéke]
```

```shell
vault kv put secret/build/storepass data="storepass"
vault kv get secret/build/storepass
vault kv get -format=json secret/build/storepass | jq -r '.data.data.data'
```

```shell
base64 -w0 mykeystore.p12 > mykeystore.p12.b64
vault kv put secret/build/keystore data="$(cat mykeystore.p12.b64)"
```

```shell
vault kv get -format=json secret/build/keystore | jq -r '.data.data.data' | base64 -d > mykeystore-exported.p12
```

## Teljes CI/CD megoldás

- CI/CD esetén nem root tokent, nem hosszú életű statikus tokent, hanem gépi authentikációs mechanizmust érdemes használni
- OIDC: CI/CD OIDC JWT authentication, és a Vault automatikusan ad egy friss, rövid TTL-es tokent minden futáskor

Pl. GitHub esetén:

- Vaultban regisztrálni kell JWT alapú autentikációt, és meg kell adni a repo/branch nevét
- Generál egy OIDC tokent, melybe beleteszi a repo/branch nevét - subject clame-ben (majd aláírja)
- A Vault ellenőrzi az aláírást, és a repo/branch nevét
- Ha stimmel, kiad egy ID_TOKEN JWT-t
- Az ID_TOKEN-nel a GitHub kér egy friss, rövid lejáratú tokent

# TLS

- SSH protokoll utódja, de az már nem biztonságos
- Más protokollok titkosítására (HTTPS, SMTPS, IMAPS, stb.)
- X.509 tanúsítványok
- Elliptic Curve Diffie–Hellman (ECDHE) kulcscsere
- Tökéletes továbbítási titkosság, Perfect Forward Secrecy (PFS)
  - Ha a szerver hosszú távú kulcsa (pl. privát RSA kulcsa) valaha kiszivárog, a korábban rögzített titkosított forgalom akkor sem fejthető vissza.
  - Hiszen minden sessionhöz új kulcsot generálnak
- Modern Cypher: AES-GCM, ChaCha20-Poly1305

OpenSSL implementáció

- TLS/SSL protokollok implementációja (HTTPS, SMTPS, IMAPS, stb.)
- Kulcsgenerálás (RSA, EC)
- CSR (Certificate Signing Request) készítés
- Tanúsítványok kezelése
- Kriptográfiai műveletek (hash, encrypt/decrypt)
- PKI feladatok (CA funkciók)

Történelmi áttekintés

- SSL 1.0 - sosem jelent meg
- SSL 2.0 - 1995, sebezhető
- SSL 3.0 - 1996, sebezhető (POODLE támadás)
- TLS 1.0 - 1999, elavult
- TLS 1.1 - 2006, elavult
- TLS 1.2 - 2008, széles körben használt
- TLS 1.3 - 2018, modern

```shell
openssl req -x509 -subj "/CN=demo-cert-1" -keyout demo.key -out demo.crt -sha256 -days 365 -nodes -newkey rsa
```

Ez PEM formátumú tanúsítványokat gyárt:

- `demo.crt` - certificate, tanúsítvány (publikus kulccsal)
- `demo.key` - privát kulcs

## Spring Boot HTTPS

- Inkább reverse proxy végezze a végződtetést

## mkcert

```shell
choco install mkcert
```

```shell
mkcert -install
```

https://github.com/FiloSottile/mkcert

- Felhasználói tanúsítványok kezelése
  - Megbízható legfelső szintű hitelesítésszolgáltatók > Tanúsítványok > mkcert ...

```shell
cd cert2
mkcert example.com "*.example.com" example.test localhost 127.0.0.1 ::1
```

```yaml
spring:
  ssl:
    bundle:
      pem:
        demo:
          reload-on-update: true
          keystore:
            certificate: "certs2/example.com+5.pem"
            private-key: "certs2/example.com+5-key.pem"
```

`https://localhost:8443/`

A kapcsolat biztonságos, tanúsítványlánc megnézhető

## Swag, Let's encrypt

- 90 naponta meg kell újítani, `certbot`
- Domain névhez kötöttek a tanúsítványok
- `linuxserver/letsencrypt` image deprecated, ugyanis a névvel jogi bajok voltak
- `linuxserver/swag` egy "ready-to-use", Docker‑alapú megoldás, amin belül együtt van Nginx + Certbot + SSL + automatikus renewal + reverse proxy + fail2ban

```yaml
services:
  swag:
    image: linuxserver/swag
    container_name: swag
    cap_add:
      - NET_ADMIN
    environment:
      - TZ=Europe/London
      - URL=apps.learnwebservices.com
      - VALIDATION=http
    volumes:
      - ./apps.learnwebservices.com.conf:/config/nginx/site-confs/apps.learnwebservices.com.conf
    ports:
      - 443:443
      - 80:80
    restart: unless-stopped
```

# SSH

- SSH (Secure Shell) egy titkosított hálózati protokoll
- Távoli bejelentkezés (pl. szerverek kezelése)
- Parancsok futtatása távolról
- Fájlok biztonságos másolása (SCP, SFTP)
- Tunnelezés / port forwardolás (pl. adatbázishoz kapcsolódni titkosított csatornán keresztül)
- Saját protokollt használ
- Nyilvános–privát kulcspárt használ
- SSH hitelesítése tipikusan kulcslistákon alapul (authorized_keys)
- Elején Diffie–Hellman (DH) kulcscsere
  - két fél egy közös titkos kulcsot állítson elő úgy, hogy közben soha nem küldik el a titkot a hálózaton
  - SSH ezt használja a titkosított csatorna felépítéséhez
  - SSH-ban sokszor nem "klasszikus" DH van, hanem ECDH (Elliptic Curve Diffie–Hellman) - gyorsabb, kisebb kulcsok, nagyobb biztonság
  - Ebből származtat session kulcsot
  - Ezután jön a hitelesítés: jelszó vagy nyilvános kulcs

Implementációk:

- OpenSSH
- Putty

## Bejelentkezés

Windowson MINGW64-val megy

```shell
sudo apt install openssh-server
sudo service ssh start
hostname -I
```

Roottal:

```shell
sudo nano /etc/ssh/sshd_config
```

```
PermitRootLogin yes
```

```shell
systemctl restart ssh
```

Kulcspár:

```
C:\Users\<felhasználóneved>\.ssh\
```

```shell
ssh-keygen -t rsa -b 4096
ssh-copy-id localhost
```

```
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys
```

Létrehozott fájlok:

- `.ssh` könyvtár
  - `id_rsa`
  - `id_rsa.pub`

Privát kulcs, saját kulcsformátuma, nem szabványos

```
-----BEGIN OPENSSH PRIVATE KEY-----
...
-----END OPENSSH PRIVATE KEY-----
```

- RSA (régebbi, lassúbb)
- ECDSA / Ed25519 (korszerű, gyors, ajánlott)

```shell
ssh-keygen -t ed25519
```

- `~/.ssh/id_ed25519`

## SSH agent

- Betölti és memóriában tartja a privát kulcsaidat
- Csak egyszer kell beírni a kulcs jelszavát

```
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_rsa
```

## Fájlmásolás

- scp kerülendő, biztonságos problémák vannak vele
- sftp-t érdemes használni
- modern verziókban az scp alatt is sftp van
- rsync: gyorsabb, okosabb, delta-transzfert használ

## Tunel

- Local forward (helyi port → távoli gép)

```
python -m http.server 8000
```

```
ssh -L 8080:localhost:80 user@server
```

- Remote forward (távoli port → helyi gép)

```
ssh -v -R 8001:localhost:8000 iviczian@172.18.164.168
```

# Spring Security

## Spring Security

- Keretrendszer
  - Java nyelven írt, nyílt forrású
  - Hitelesítés (authentication): annak ellenőrzése, hogy a felhasználó valóban az, akinek mondja magát (pl. jelszóval, kétfaktoros hitelesítéssel)
  - Engedélyezés (authorization): annak eldöntése, hogy egy hitelesített felhasználó milyen erőforrásokhoz férhet hozzá
  - Védelmi mechanizmusok a gyakori támadási módok ellen
  - Spring alkalmazásokhoz könnyen illeszthető, akár Servlet alapú, akár Reactive alkalmazásokhoz
  - Integráció más eszközökhöz, pl. Spring Data, JSP/Thymeleaf/stb. template
  - Könnyen tesztelhető

## Gyakori használati esetek hitelesítés esetén

- Webes alkalmazás felhasználónév és jelszó alapú autentikációval, sessionnel
- REST API-val rendelkező alkalmazás, JWT token kiadásával és ellenőrzésével, állapotmentes módon
- OAuth 2.0 és OpenID Connect

## Főbb jellemzők

- Űrlap alapú bejelentkezés, Remember me funkcionalitás
- Basic authentication
- Jelszó hash-elés
- Tanúsítvány alapú hitelesítés
- Felhasználók tárolása adatbázisban
- Felhasználók tárolása LDAP szerveren

## Engedélyezés

- URL alapú védelem
- Metódus szintű védelem (annotációk használatával)
- Access Controll List (ACL)

## Védelmi mechanizmusok

- Security Headers, pl. HTTP Strict Transport Security (HSTS), Content Security Policy (CSP), stb.
- Cross Site Request Forgery (CSRF)
- Illegális kérések kiszűrése

# OAuth 2.0

## &nbsp;

## OAuth 2.0

- Nyílt szabány **erőforrás-hozzáférés** kezelésére (Open Authorization)
- Engedélyezésre (authorization) koncentrál, és nem a hitelesítésre (authentication)
- Fő használati területe a web, de kitér az asztali alkalmazásokra, mobil eszközökre, okos eszközökre, stb.
- **Elválik**, hogy hol történik a felhasználó hitelesítése (Authorization Server) és hol kíván erőforráshoz hozzáférni (Resource Server)
  - Google, GitHub, Facebook vagy saját szerver
- Authorization Server a hitelesítés után Access Tokent állít ki
- A Resource Server az Access Token alapján adja meg az engedélyt az adott erőforrás elérésére, pl. egy REST API-n elérhető alkalmazás
- A Client további alkalmazás, mely a felhasználó nevében próbál hozzáférni a Resource Serveren lévő erőforráshoz, pl. webalkalmazás vagy mobilalkalmazás

The OAuth 2.0 Authorization Framework ([RFC 6749](https://datatracker.ietf.org/doc/html/rfc6749))

## Token

- Karaktersor, melynek birtokában az erőforrás elérhető
- Bearer Token, mely HTTP kéréskor a `Authorization` fejlécben küldhető át
- Opaque token: felhasználója számára nem tartalmaz információt
  - Információkat az Authorization Servertől a token birtokában a Token Introspection végponton lehet lékérni (`/token/introspect`)
- Non-opaque token: felhasználója közvetlenül ki tudja olvasni az adatot
  - Lehet JWT formátumú, JSON formátumban, BASE64-gyel kódolva

OAuth 2.0 Token Introspection ([RFC 7662](https://datatracker.ietf.org/doc/html/rfc7662))

## OAuth 2.0 Grant Types

- Authorization Code: URL-ben az Authorization Servertől egy Authorization Code-ot kapunk, mellyel lekérhető háttérben az Access Token
- Implicit: mobil alkalmazások, vagy SPA-k használták, azonnal Access Tokent kap URL-ben (deprecated)
- Resource Owner Password Credentials: ezt olyan megbízható alkalmazások használják, melyek maguk kérik be a jelszót, nem kell átirányítás (deprecated)
- Client Credentials: ebben az esetben nem a felhasználó kerül azonosításra, hanem az alkalmazás önmaga

## PKCE

- Ha a támadó valamilyen módon megszerzi az Authorization Code-ot (code interception támadás), kérhet vele Access Tokent
- A PKCE egy biztonsági mechanizmus, mely plusz kódok és azok ellenőrzésével ezt kivédi
- Public client: mobil alkalmazások, SPA alkalmazások, ahol a client secret nem tárolható
  - Különösen fontos
  - Kiváltja az Implicit Grant Type-ot
- Confidental client: webes alkalmazás, client secret tárolható
  - Ennél is ajánlott
- OAuth 2.1 alapértelmezett része

Proof Key for Code Exchange by OAuth Public Clients (PKCE) ([RFC 7636](https://datatracker.ietf.org/doc/html/rfc7636))

## Refresh token

- Access token rövid életű, hogyha a támadó megszerzi, lejárat után nem tudja használni
- Refresh token hosszú életű, segítségével új Access token kérhető
- Nem használható API hívásokban, kizárólag Access token lekérésére

# Authorization Code Grant Type

## &nbsp;

## Authorization Code Grant Types lépések

<img src="images/oauth-auth-code-grant-type.drawio.svg" width="600"/>

## További paraméterek

<img src="images/oauth-auth-code-grant-type_params.drawio.svg" width="600"/>

## Paraméterek leírása

- Authorization Endpoint kérésben
  - `response_type=code`: Authorization Code Grant Type
  - `client_id`
  - `scope`: `openid`
  - `state`: CSRF támadás ellen, átirányítás előtt elmenti a Client (pl. session), majd visszairányításnál visszakapja és ellenőrzi (OAuth 2.0 protokoll része)
  - `redirect_uri`: milyen címre irányítson vissza
  - `nonce` (OpenID Connect része) - Client generálja, auth server beleteszi a tokenbe, amit a client ellenőrizni tud
- Authorization Endpoint válaszban:
  - `state`: ugyanaz, ami a kérésben elküldésre került
  - `session_state`: [OpenID Connect Session Management](https://openid.net/specs/openid-connect-session-1_0.html)
  - `iss`: Issuer, OAuth 2.0 Authorization Server Issuer Identification ([RFC 9207](https://www.rfc-editor.org/rfc/rfc9207.html))
  - `code`: Authorization Code

## Token claimek

- `iss`: Issuer
- `azp`: Authorized party, megegyezik a `client_id` értékével
- `nonce`: Megegyezik a küldött `nonce` értékével

class: inverse, center, middle

# PKCE

## &nbsp;

## PKCE lépések

<img src="images/oauth-auth-code-grant-type_pkce.drawio.svg" width="600"/>

## PKCE paraméterek leírása

- Bejelentkezés előtt a kliens generál egy véletlen kódot: `code_verifier`
- Ebből készít egy `code_challenge`-t, SHA-256 hash algoritmussal
- Authorization Code kérésekor elküldi paraméterben, Base64 és URL encode után:
  - `code_challenge`
  - `code_challenge_method`: `S256`
- Mikor a code használatával tokent kér le, ezt is el kell küldenie `code_verifier` (form) paraméterként

# OWASP Top 10

## Information security - CIA

- Confidentiality - bizalmasság: az információ csak az arra felhatalmazottak számára legyen elérhető
- Integrity - sértetlenség: információk és a feldolgozási módszerek teljességének és helyességének
- Availability - rendelkezésre állás: felhasználók mindig hozzáférjenek az információkhoz

## Módszertan

- Nonprofit szervezet
- Adatgyűjtés különböző szervezetektől, biztonsági cégektől, kutatóktól
- Sérülékenységeket Common Weakness Enumeration- (CWE) azonosítókkal látják el
- Súlyozzák gyakoriság, kihasználhatóság egyszerűsége és hatás alapján
- Újraértékelik
  - OWASP Top 10:2021
  - OWASP Top 10:2025 - Release Candidate 2025. november 6-án

## OWASP Top 10:2021

- A01 Broken Access Control
- A02 Cryptographic Failures
- A03 Injection
- A04 Insecure Design
- A05 Security Misconfiguration
- A06 Vulnerable and Outdated Components
- A07 Identification and Authentication Failures
- A08 Software and Data Integrity Failures
- A09 Security Logging and Monitoring Failures
- A10 Server Side Request Forgery (SSRF)

## OWASP Top 10:2025

- A01 Broken Access Control
- A02 Security Misconfiguration
- A03 Software Supply Chain Failures
- A04 Cryptographic Failures
- A05 Injection
- A06 Insecure Design
- A07 Authentication Failures
- A08 Software or Data Integrity Failures
- A09 Logging and Alerting Failures
- A10 Mishandling of Exceptional Conditions

## A01 Broken Access Control (Hibás hozzáférés-kezelés)

- Felhasználók jogosultságainak megkerülése.
- Privát adatokhoz való jogosulatlan hozzáférés.
- Funkciók kihasználása, amelyek nem engedélyezettek az adott felhasználónak.

Gyakorlat:

- Spring Security - Link megjelenítése szerepkör alapján a webes felületen
  - `USER` is hozzáfér a `/create-employee` oldalhoz az URL ismeretében
- Spring Security - `/create-employee` letiltása URL alapján
  - Sorrend
- Spring Security - Metódus szintű jogosultságkezelés
- Spring Security Paraméterek és visszatérési értékek
- Spring Security - Spring Data integráció
- Spring Security - ACL

- RBAC – Role-Based Access Control: felhasználónak szerepköre van, szerepkörhöz vannak jogosultságok rendelve. Egy felhasználóhoz több szerepkör is tartozhat.
- ABAC – Attribute-Based Access Control: döntés alapján születik
  - felhasználó attribútumai (department=HR, clearance=secret)
  - erőforrás attribútumai (doc.classification=confidential)
  - környezeti attribútumok (location=EU, time=business_hours)
  - Ezeket felhasználva komplex szabályok alapján: pl. "Engedélyezd, ha (user.department == resource.department) és time < 18:00."
- ACL - Minden objektumhoz (fájl, endpoint, adatbázis-rekord stb.) tartozik egy lista, amely megmondja, ki (mely felhasználó vagy csoport) mit tehet vele.

## A02:2021 – Cryptographic Failures (Kriptográfiai hibák)

- Gyenge vagy elavult titkosítási algoritmusok használata (pl. MD5, SHA1).
- Adatok titkosítás nélküli tárolása az alkalmazásban (pl. jelszavak, érzékeny adatok).
- Nem megfelelő kulcskezelés, kulcsok kódba égetése.
- TLS/HTTPS hiánya vagy hibás konfiguráció.

Gyakorlat:

- Jelszó hash algoritmusok
- DelegatingPasswordEncoder - lehetővé teszi a változtatást, több passwordencoder-hez is továbbítani tudja a kérést (ez azért jó, ha változtatunk az algoritmuson)
- Környezeti változók
- Jasypt - de neki környezeti változóként kell a jelszót átadni, ennyi erővel az adatbázis jelszót is át lehet adni
- Docker, Kubernetes (SecretMap)
- Config Server + Vault
- Jelszavak CI/CD-ben
- HTTP Strict Transport Security (HSTS) (Spring Security anyag)

### Jelszavak hash-elése

- GPU támadás: grafikus kártyával (GPU‑val) próbál jelszóhasheket brute-force-olni
- több ezer párhuzamos számítást tudnak futtatni, ezért sokkal gyorsabbak, mint a CPU egy jelszóhash tömeges számításában
- a GPU-n nincs elég gyors memória minden szálhoz

side-channel támadás

- Nem az algoritmust támadja, hanem a környezetet
- Formái:
  - Időzítéses támadás (timing attack) - időt mér, és abból von le következtetést
  - Cache támadás - mely memóriaterületeket cache‑eli a processzor
  - Power analysis - méri, hogyan változik az áramfelvétel a CPU-ban

Kevésbé használtak:

- PBKDF2
- SHA-crypt

### bcrypt

- Hash algoritmus jelszavak biztonságos tárolására, amelyet 1999-ben fejlesztettek ki a Blowfish titkosító algoritmus alapján
- salt, így ugyanaz a jelszó mindig más hash-t ad, ezzel megelőzve a rainbow table támadásokat
- Adaptív komplexitás: Bcrypt beállítható úgy, hogy lassítsa a hash-elést, ami megnehezíti a brute-force támadásokat
  - Emiatt CPU intenzív
- Technikai limit kb. 72 karakter

```
$2b$12$eIX0E6rGv2G7Q6PtDqWjxuZ5Yv7wY8s5vF3t8T3x6A0Wz6h0rQ9tC
```

Részei:

- Verzió
- Cost factor, azaz a hash számítás nehézségi szintje (2^12 iteráció)
- Salt
- Hash

DoS-olható, védekezési lehetőségek:

- Rate limit
- Account lockout

### scrypt

- Memória-intenzív is
- Java `SCryptPasswordEncoder`

### Argon2

Argon2 a modern jelszó-hash algoritmusok legújabb sztenderdje

- Argon2d – főleg GPU-támadás ellen véd, CPU-intenzív, memóriát kevésbé használ.
- Argon2i – főleg side-channel támadások ellen véd, memóriát jobban használ.
- Argon2id – hibrid, egyszerre védi a brute-force és a side-channel támadások ellen. Ez a legajánlottabb a jelszó-hash-re.

Tulajdonságai

- Adaptív: beállítható a CPU idő, memóriahasználat és párhuzamos szálak száma.
- Memória-intenzív: memóriát is használ, így GPU/ASIC támadás kevésbé hatékony, nem csak CPU-ra épül.
- Salt: minden jelszóhoz egyedi salt.

```
$argon2id$v=19$m=65536,t=3,p=4$<salt>$<hash>
```

- m=65536 → memóriahasználat (KB)
- t=3 → iterációk száma
- p=4 → párhuzamos szálak száma

```java
PasswordEncoder encoder = new Argon2PasswordEncoder();
```

## A03:2021 – Injection

- SQL injection a leggyakoribb, de command, XML, JSON, reflection, template, más nyelv, NoSQL, logging, deserialization, XPath, LDAP
  - Serialization ráadásul: Java serialization, XML, JSON, Protobuf, AVRO
- XSS
- Path traversal

XSS:

- reflected: csak visszajátsza, nem tárolja
- persistent: adatbázisba is kerül

Gyakorlat:

- Thymeleaf: `th:text` helyett `th:utext` támadható, `standalone-form` alkalmazáson demonstrálható
- AntySamy lib használata, `AntySamyMain`
- Content Security Policy (CSP), más néven Frontend Security Policy Header (Spring Security)

## A04:2021 – Insecure Design (Biztonságilag nem megfelelő tervezés)

## A05:2021 – Security Misconfiguration (Biztonsági hibás konfiguráció)

- Alapértelmezett fiókok, jelszavak használata.
- Felesleges szolgáltatások, endpointok engedélyezve.
- Hibás HTTP fejléc konfiguráció (pl. CORS, HSTS hiánya).

Gyakorlat:

- Fejlécek (Spring Security)
- Cross Site Request Forgery (CSRF) (Spring Security), megoldása a token
- CORS (Spring Security)
- Actuator biztonságossá tétele külön FilterChainnel (Spring Security)
- DevTools
- `XxeDemo` osztály
- CXF tesztelés

## A06:2021 – Vulnerable and Outdated Components (Sebezhető és elavult komponensek)

- Ismert sérülékenységek kihasználhatóak
  - [CVE](https://www.cve.org/) - a sérülékenységgel kapcsolatos tények
  - [NVD](https://nvd.nist.gov/) - CVE kiegészítése részletes adatokkal, súlyosság, kategórizálás, stb.
- Komponensek automatikus frissítésének hiánya
- Licensz vagy verzióellenőrzés hiánya

Gyakorlat:

- Spring Boot SBOM
- grype, SBOM feldolgozásra (`bom.md`)
- syft, image SBOM

## A07:2021 – Identification and Authentication Failures (Azonosítási és hitelesítési hibák)

- Brute force elleni védelem
- Gyenge jelszavak, nem megfelelő jelszópolitikák.
- Session fixáció vagy session hijacking lehetősége. (ezt a WebGoat máshova írja)
- Multifaktoros hitelesítés hiánya kritikus funkciókhoz.
- JWT vagy tokenek hibás kezelése.

Gyakorlat:

- Tomcat által kiosztott cookie, `HttpOnly`, `Secure`
- KeePassXC Database / Database Reports HIBP, SHA-1 hash, és annak az első 5 karakterét küldi el, és nem a jelszót

## A08:2021 – Software and Data Integrity Failures (Szoftver- és adatintegritási hibák)

- Alkalmazás frissítések nem ellenőrzött forrásból.
- Kód vagy csomag manipulációk, supply chain támadások.
- Nem digitálisan aláírt frissítések.
- Kritikus konfigurációk vagy adatok manipulálhatósága.

## A09:2021 – Security Logging and Monitoring Failures (Biztonsági naplózás és monitoring hiányosságok)

- Biztonsági események nem naplózottak.
- Nincs riasztás a kritikus támadásokra.
- Audit és monitoring hiánya megnehezíti a támadás észlelését.
- Rendszeresen nem ellenőrzik a logokat.

## A10:2021 – Server-Side Request Forgery (SSRF)

- Szerver kérések manipulálása felhasználói inputból.
- Belső hálózati erőforrásokhoz való hozzáférés.
- Védtelen URL fetch vagy HTTP hívások.
- Input validáció hiánya külső/belső URL-eknél.

# Secure Coding Guidelines for Java SE

[Secure Coding Guidelines for Java SE](https://www.oracle.com/java/technologies/javase/seccodeguide.html)

- Túlcsordulás
- Path traversal
- Hozzáférési módosítók
- Reflection
- Serialization
- Kivételkezelés
- Referenciák
- Immutable
- Öröklődés
