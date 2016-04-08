import org.crsh.cli.Command;

import org.apache.commons.io.FileUtils;
import org.apache.logging.slf4j.Log4jLogger;

import org.apache.logging.log4j.spi.ExtendedLogger;

import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.support.oauth.token.AccessTokenImpl;
import org.jasig.cas.support.oauth.token.RefreshTokenImpl;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.Module.SetupContext;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class oauth {

    /**
     * Delete all files and dirs in EXPORT path
     *
     * @return
     */
    @Command
    public void CLEAN() {
        Settings settings = new Settings();
        settings.cleanCache(settings.getExportPath());
    }

    /**
     * Export current database to json file
     *
     * @return
     */
    @Command
    public String EXPORT() {
        def entityManagerFactory = context.attributes.beans["entityManagerFactory"];
        def entityManager = entityManagerFactory.createEntityManager();

        def tx = entityManager.getTransaction();
        tx.begin();
        List<TicketGrantingTicketImpl> ticketGrantingTicketList = entityManager.createQuery("select t from TicketGrantingTicketImpl t", TicketGrantingTicketImpl.class).getResultList();
        List<ServiceTicketImpl> serviceTicketList = entityManager.createQuery("select t from ServiceTicketImpl t", ServiceTicketImpl.class).getResultList();
        List<RefreshTokenImpl> refreshTokenList = entityManager.createQuery("select t from RefreshTokenImpl t", RefreshTokenImpl.class).getResultList();
        List<AccessTokenImpl> accessTokenList = entityManager.createQuery("select t from AccessTokenImpl t", AccessTokenImpl.class).getResultList();
        tx.commit();

        //  Note: import/export/tweak should use the same ObjectMapper settings
        Settings settings = new Settings();
        settings.cleanCache(settings.getExportPath());

        int totalPersonalATs = 0;

        for (TicketGrantingTicketImpl ticket: ticketGrantingTicketList) {
            jacksonSerializerTicketGrantingTicket(ticket, settings.getExportPath());
        }
        for (ServiceTicketImpl ticket: serviceTicketList) {
            jacksonSerializerServiceTicket(ticket, settings.getExportPath());
        }
        for (RefreshTokenImpl token: refreshTokenList) {
            jacksonSerializerRefreshToken(token, settings.getExportPath());
        }
        for (AccessTokenImpl token: accessTokenList) {
            jacksonSerializerAccessToken(token, settings.getExportPath());
            if (!token.getId().startsWith("AT-")) {
                totalPersonalATs ++;
            }
        }

        int totalExports = ticketGrantingTicketList.size() + serviceTicketList.size() + refreshTokenList.size() + accessTokenList.size();
        return totalExports + " files exported\n" + totalPersonalATs + " personal access tokens found";
    }

    /**
     * Import json object from tweaked file and write to database
     *
     * @return
     */
    @Command
    public String IMPORT() {

        Settings settings = new Settings();
        settings.cleanCache(settings.getImportPath());

        List<TicketGrantingTicketImpl> ticketGrantingTicketList = new ArrayList<TicketGrantingTicketImpl>();
        List<ServiceTicketImpl> serviceTicketList = new ArrayList<ServiceTicketImpl>();
        List<RefreshTokenImpl> refreshTokenList = new ArrayList<RefreshTokenImpl>();
        List<AccessTokenImpl> accessTokenList = new ArrayList<AccessTokenImpl>();
        List<AccessTokenImpl> personalAccessTokenList = new ArrayList<AccessTokenImpl>();

        for (File file : (new File(settings.getTweakPath())).listFiles()) {
            if (!file.isDirectory()) {
                if(file.getName().startsWith("TGT-")) {
                    ticketGrantingTicketList.add(jacksonDeserializerTicketGrantingTicket(file));
                }
                else if(file.getName().startsWith("ST-")) {
                    serviceTicketList.add(jacksonDeserializerServiceTicket(file));
                }
                else if(file.getName().startsWith("RT-")) {
                    refreshTokenList.add(jacksonDeserializerRefreshToken(file));
                }
                else if(file.getName().startsWith("AT-")) {
                    accessTokenList.add(jacksonDeserializerAccessToken(file));
                }
                else {
                    personalAccessTokenList.add(jacksonDeserializerAccessToken(file));
                }
            }
        }

        //  Write data to DB

        def entityManagerFactory = context.attributes.beans["entityManagerFactory"];
        def entityManager = entityManagerFactory.createEntityManager();

        def tx = entityManager.getTransaction();
        tx.begin();
        for (TicketGrantingTicketImpl ticket: ticketGrantingTicketList) {
            entityManager.persist(ticket);
        }
        tx.commit();
        tx.begin();
        for (ServiceTicketImpl ticket: serviceTicketList) {
            entityManager.persist(ticket);
        }
        tx.commit();
        tx.begin();
        for (RefreshTokenImpl token: refreshTokenList) {
            entityManager.persist(token);
        }
        tx.commit();
        tx.begin();
        for (AccessTokenImpl token: accessTokenList) {
            entityManager.persist(token);
        }
        tx.commit();
        tx.begin();
        for (AccessTokenImpl token: personalAccessTokenList) {
            entityManager.persist(token);
        }
        tx.commit();

        StringBuilder returnMessage = new StringBuilder();
        returnMessage.append(ticketGrantingTicketList.size() + "\tTGTs\n");
        returnMessage.append(serviceTicketList.size() + "\tSTs\n");
        returnMessage.append(refreshTokenList.size() + "\tRTs\n");
        returnMessage.append(accessTokenList.size() + "\tATs\n");
        returnMessage.append(personalAccessTokenList.size() + "\tPersonal ATs\n");
        int totalImports = ticketGrantingTicketList.size() + serviceTicketList.size() + refreshTokenList.size() + accessTokenList.size() + personalAccessTokenList.size();
        returnMessage.append("---------------\n");
        returnMessage.append(totalImports + " files imported in total\n");
        return returnMessage.toString();
    }

    /**
     * Import json objects from file, tweak them and then export
     *
     * @return
     */
    @Command
    public String TWEAK() {
        settings = new Settings();
        settings.cleanCache(settings.getTweakPath());

        int count = 0;
        for (File file : (new File(settings.getExportPath())).listFiles()) {
            if (!file.isDirectory()) {
                jacksonTweakJson(file, settings);
                count ++;
            }
        }

        return count + " files tweaked";
    }

    /**
     *
     * @param file
     * @param settings
     */
    private void jacksonTweakJson(File file, Settings settings) {
        ObjectMapper mapper = initMapper();
        File tweaked = initFile((String) (settings.getTweakPath() + File.separator + file.getName() + ".json"));

        ObjectNode root = mapper.readValue(file, ObjectNode.class);

        //  There is no type info in variable declaration
        //  Because NullNode is different from null and cannot be casted from ArrayNode

        //  TGT, ST
        expirationPolicy = root.get("expirationPolicy");
        if (expirationPolicy != null && expirationPolicy.getClass() != NullNode) {
            removeLogger(expirationPolicy);
        }
        //  TGT
        authentication = root.get("authentication");
        if (authentication != null && authentication.getClass() != NullNode) {
            addClass(authentication);
        }
        //  ST, RT, AT
        ticketGrantingTicket = root.get("ticketGrantingTicket");
        if (ticketGrantingTicket != null && ticketGrantingTicket.getClass() != NullNode) {
            removeLogger(ticketGrantingTicket.get(1).get("expirationPolicy"));
            addClass(ticketGrantingTicket.get(1).get("authentication"));
        }

        //  RT, AT
        serviceTicket = root.get("serviceTicket");
        if (serviceTicket != null && serviceTicket.getClass() != NullNode) {
            removeLogger(serviceTicket.get(1).get("expirationPolicy"));
            ticketGrantingTicket = serviceTicket.get(1).get("ticketGrantingTicket");
            if (ticketGrantingTicket != null && ticketGrantingTicket.getClass() != NullNode) {
                removeLogger(ticketGrantingTicket.get(1).get("expirationPolicy"));
                addClass(ticketGrantingTicket.get(1).get("authentication"));
            }
        }

        mapper.writeValue(tweaked, root);

        //  For the purpose of verification
//        File imported = initFile((String) (settings.getImportPath() + File.separator + file.getName() + ".json"));
//        if(file.getName().startsWith("TGT-")) {
//            mapper.writeValue(imported, jacksonDeserializerTicketGrantingTicket(tweaked));
//        }
//        else if(file.getName().startsWith("ST-")) {
//            mapper.writeValue(imported, jacksonDeserializerServiceTicket(tweaked));
//        }
//        else if(file.getName().startsWith("RT-")) {
//            mapper.writeValue(imported, jacksonDeserializerRefreshToken(tweaked));
//        }
//        else if(file.getName().startsWith("AT-")) {
//            mapper.writeValue(imported, jacksonDeserializerAccessToken(tweaked));
//        }
    }

    /**
     *
     * @param root
     */
    private void removeLogger(ArrayNode root) {
        root.get(1).remove("logger");
        root.get(1).get("oAuthRefreshTokenExpirationPolicy").get(1).remove("logger");
        root.get(1).get("oAuthAccessTokenExpirationPolicy").get(1).remove("logger");
        root.get(1).get("sessionExpirationPolicy").get(1).remove("logger");

        ArrayNode rememberMePolicy = root.get(1).get("sessionExpirationPolicy").get(1).get("rememberMeExpirationPolicy");
        ArrayNode sessionExpirationPolicy = root.get(1).get("sessionExpirationPolicy").get(1).get("sessionExpirationPolicy");
        while (true) {
            if (rememberMePolicy != null) {
                rememberMePolicy.get(1).remove("logger");
            }
            if (sessionExpirationPolicy != null) {
                sessionExpirationPolicy.get(1).remove("logger");
                rememberMePolicy = sessionExpirationPolicy.get(1).get("rememberMeExpirationPolicy");
                sessionExpirationPolicy = sessionExpirationPolicy.get(1).get("sessionExpirationPolicy");
            }
            else {
                break;
            }
        }
    }

    /**
     *
     * @param root
     */
    private void addClass(ArrayNode root) {
        oldNode = root.get(1).get("successes").get(1).get("OpenScienceFrameworkAuthenticationHandler");
        if(oldNode != null && oldNode.getClass() == ObjectNode.class) {
            ArrayNode newNode = new ArrayNode(JsonNodeFactory.instance);
            newNode.add("org.jasig.cas.authentication.DefaultHandlerResult");
            newNode.add(oldNode);
            root.get(1).get("successes").get(1).set("OpenScienceFrameworkAuthenticationHandler", newNode);
        }
        oldNode = root.get(1).get("successes").get(1).get("OAuthCredentialsAuthenticationHandler");
        if(oldNode != null && oldNode.getClass() == ObjectNode.class) {
            ArrayNode newNode = new ArrayNode(JsonNodeFactory.instance);
            newNode.add("org.jasig.cas.authentication.DefaultHandlerResult");
            newNode.add(oldNode);
            root.get(1).get("successes").get(1).set("OAuthCredentialsAuthenticationHandler", newNode);
        }
    }

    /**
     *
     * @param file
     * @return
     */
    private TicketGrantingTicketImpl jacksonDeserializerTicketGrantingTicket(File file) {
        ObjectMapper mapper = initMapper();
        TicketGrantingTicketImpl ticket = mapper.readValue(file, TicketGrantingTicketImpl.class);
        return ticket;
    }

    /**
     *
     * @param file
     * @return
     */
    private ServiceTicketImpl jacksonDeserializerServiceTicket(File file) {
        ObjectMapper mapper = initMapper();
        ServiceTicketImpl ticket = mapper.readValue(file, ServiceTicketImpl.class);
        return ticket;
    }

    /**
     *
     * @param file
     * @return
     */
    private RefreshTokenImpl jacksonDeserializerRefreshToken(File file) {
        ObjectMapper mapper = initMapper();
        RefreshTokenImpl token = mapper.readValue(file, RefreshTokenImpl.class);
        return token;
    }

    /**
     *
     * @param file
     * @return
     */
    private AccessTokenImpl jacksonDeserializerAccessToken(File file) {
        ObjectMapper mapper = initMapper();
        AccessTokenImpl token = mapper.readValue(file, AccessTokenImpl.class);
        return token;
    }

    /**
     *
     * @param ticket
     * @param settings
     */
    private void jacksonSerializerTicketGrantingTicket(TicketGrantingTicketImpl ticket, String path) {
        ObjectMapper mapper = initMapper();
        File jsonFile = initFile((String)(path + File.separator + ticket.getId() + ".json"));
        String jsonStr = tweakJson(mapper.writeValueAsString(ticket));
        jsonFile.write(jsonStr);
    }

    /**
     *
     * @param ticket
     * @param settings
     */
    private void jacksonSerializerServiceTicket(ServiceTicketImpl ticket, String path) {
        ObjectMapper mapper = initMapper();
        File jsonFile = initFile((String)(path + File.separator + ticket.getId() + ".json"));
        String jsonStr = tweakJson(mapper.writeValueAsString(ticket));
        jsonFile.write(jsonStr);
    }

    /**
     *
     * @param ticket
     * @param settings
     */
    private void jacksonSerializerRefreshToken(RefreshTokenImpl ticket, String path) {
        ObjectMapper mapper = initMapper();
        File jsonFile = initFile((String)(path + File.separator + ticket.getId() + ".json"));
        String jsonStr = tweakJson(mapper.writeValueAsString(ticket));
        jsonFile.write(jsonStr);
    }

    /**
     *
     * @param ticket
     * @param settings
     */
    private void jacksonSerializerAccessToken(AccessTokenImpl ticket, String path) {
        ObjectMapper mapper = initMapper();
        File jsonFile = initFile((String)(path + File.separator + ticket.getId() + ".json"));
        String jsonStr = tweakJson(mapper.writeValueAsString(ticket));
        jsonFile.write(jsonStr);
    }

    /**
     *
     * @return
     */
    private ObjectMapper initMapper() {
        ObjectMapper mapper = new ObjectMapper();

        //  use mixin + creator to handle default constructor missing in deserialization
        mapper.registerModule(new JacksonMixinModule());

        //  polymorphic type handling: add enough type information so that
        //  deserializer can instantiate correct subtype of a value,
        //  even if declaration of the field/setter/creator method only has single type (supertype) defined
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);

        //  enable serialization for a type without accessors (when there are no annotations)
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);

        //  data binding: bind all fields, public or private
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        //  enable indentation and use default pretty printer
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        return mapper;
    }

    /**
     *
     * @param path
     * @return
     */
    private File initFile(String path) {
        File jsonFile;
        try {
            jsonFile = new File(path);
        }
        catch (IOException e) {
            System.err.println("IO Exception: cannot open file");
            throw e;
        }
        return jsonFile;
    }

    /**
     *
     * @param jsonStr
     * @return
     */
    private String tweakJson(String jsonStr) {
        //  the issues below cannot be solved by using Jackson MixIn/Annotation:

        //  java.util.Collections$UnmodifiedMap does not have a deserializable constructor,
        //  modify the class to its private one: java.util.HashMap.
        String tweakedJsonStr = jsonStr.replaceAll("java.util.Collections" + Matcher.quoteReplacement("\$") + "UnmodifiableMap", "java.util.HashMap");

        //  org.slf4j.impl.JDK14LoggerAdapter no longer available,
        //  modify the class to org.apache.logging.slf4j.Log4jLogger
        tweakedJsonStr = tweakedJsonStr.replaceAll("org.slf4j.impl.JDK14LoggerAdapter", "org.apache.logging.slf4j.Log4jLogger");
        return tweakedJsonStr;
    }
}



public class Settings {

    public Settings() {
        File dirExport, dirTweak, dirImport;
        try {
            dirExport = new File("serialization-export");
            if (!dirExport.exists()) {
                dirExport.mkdir();
            }
            dirTweak = new File("serialization-tweak");
            if (!dirTweak.exists()) {
                dirTweak.mkdir();
            }
            dirImport = new File("serialization-import");
            if (!dirImport.exists()) {
                dirImport.mkdir();
            }
        }
        catch (IOException e) {
            System.err.println("IO Exception: cannot initiate working directory");
            throw e;
        }
        this.exportPath = dirExport.getCanonicalPath();
        this.importPath = dirImport.getCanonicalPath();
        this.tweakPath = dirTweak.getCanonicalPath();
    }

    private String exportPath;
    private String tweakPath;
    private String importPath;

    public String getExportPath() {
        return this.exportPath
    }
    public String getTweakPath() {
        return this.tweakPath
    }
    public String getImportPath() {
        return this.importPath
    }

    public void cleanCache(String path) {
        try {
            FileUtils.cleanDirectory( new File(path));
        }
        catch (IOException e) {
            System.err.println("IO Exception: cannot clean working directory");
            throw e;
        }
    }
}



public class JacksonMixinModule extends SimpleModule {
    public JacksonMixinModule() {
        super("JacksonMixinModule", new Version(0, 0, 1, null));
    }

    @Override
    public void setupModule(SetupContext context) {
        context.setMixInAnnotations(SimpleWebApplicationServiceImpl.class, SimpleWebApplicationServiceImplMixIn.class);
        context.setMixInAnnotations(Log4jLogger.class, Log4jLoggerMixIn.class);
    }
}



abstract class SimpleWebApplicationServiceImplMixIn {
    @JsonCreator
    public SimpleWebApplicationServiceImplMixIn(@JsonProperty("id") String id) {}
}



abstract class Log4jLoggerMixIn {
    @JsonCreator
    public Log4jLoggerMixIn(@JsonProperty("logger") ExtendedLogger logger, @JsonProperty("name") String name) {}
}
