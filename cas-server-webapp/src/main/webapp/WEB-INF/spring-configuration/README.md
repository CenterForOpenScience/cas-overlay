### INTRODUCTION

The spring-configuration directory is a "convention-over-configuration" option for CAS developers and deployers. It allows you to drop a Spring XML configuration file into this directory and have CAS automatically find it (after the typical application restart). It eliminates the need for you to register that file in the `web.xml`, which should already have set `contextConfigLocation` to `/WEB-INF/spring-configuration/*.xml`.

```xml
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>
        /WEB-INF/spring-configuration/*.xml
        classpath*:/META-INF/spring/*.xml
    </param-value>
</context-param>
```

### ADVANTAGES

By automatically breaking the configuration into smaller "bite-sized" pieces you can easily override small components of CAS without worrying about merging huge pieces of configurations files together later. The configuration-over-convention option also allows you to add new configuration options without editing existing configuration files. This should make tracking changes and maintaining local modifications easier.

### GOTCHAS AND THINGS TO WATCH OUT FOR

If you name a local bean and an existing bean the same thing, there will be a major collision. Deployment will fail. The sky will fall! (okay that last part isn't true). Spring will be merging all of these files together so every bean must have unique names.  The only way around this is if you override the file completely. (i.e. override the ticketRegistry.xml allows you to re-use the "ticketRegistry" id.) In addition, if there is a typographical/XML parsing error in a file, the application will not deploy.

### References

[Here](https://github.com/apereo/cas/blob/4.1.x/cas-server-webapp/src/main/webapp/WEB-INF/spring-configuration/README.txt) is the original Spring configuration `README.txt` for [Apereo CAS 4.1.x](https://apereo.github.io/cas/4.1.x/index.html).
