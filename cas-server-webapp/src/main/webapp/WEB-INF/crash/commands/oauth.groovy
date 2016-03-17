import org.crsh.cli.Command
import org.apache.commons.io.FileUtils
import org.apache.logging.slf4j.Log4jLogger;
import org.apache.logging.log4j.spi.ExtendedLogger
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.support.oauth.token.RefreshTokenImpl;
import org.jasig.cas.support.oauth.token.AccessTokenImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.Module.SetupContext;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class oauth {

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

        //  Note: for successful import, export and import should use the same settings
        Settings settings = new Settings();
        settings.cleanCache();

        for (TicketGrantingTicketImpl ticket: ticketGrantingTicketList) {
            jacksonSerializerTicketGrantingTicket(ticket, settings);
        }
        for (ServiceTicketImpl ticket: serviceTicketList) {
            jacksonSerializerServiceTicket(ticket, settings);
        }
        for (RefreshTokenImpl token: refreshTokenList) {
            jacksonSerializerRefreshToken(token, settings);
        }
        for (AccessTokenImpl token: accessTokenList) {
            jacksonSerializerAccessToken(token, settings);
        }

        int totalExports = ticketGrantingTicketList.size() + serviceTicketList.size() + refreshTokenList.size() + accessTokenList.size();
        return totalExports + " files exported";
    }


    @Command
    public String IMPORT() {

        Settings settings = new Settings();

        List<TicketGrantingTicketImpl> ticketGrantingTicketList = new ArrayList<TicketGrantingTicketImpl>();
        List<ServiceTicketImpl> serviceTicketList = new ArrayList<ServiceTicketImpl>();
        List<RefreshTokenImpl> refreshTokenList = new ArrayList<RefreshTokenImpl>();
        List<AccessTokenImpl> accessTokenList = new ArrayList<AccessTokenImpl>()

        for (File file : (new File(settings.getSerializationPath())).listFiles()) {
            if (!file.isDirectory()) {
                if(file.getName().startsWith("TGT")) {
                    ticketGrantingTicketList.add(jacksonDeserializerTicketGrantingTicket(file));
                }
                else if(file.getName().startsWith("ST")) {
                    serviceTicketList.add(jacksonDeserializerServiceTicket(file));
                }
                else if(file.getName().startsWith("RT")) {
                    refreshTokenList.add(jacksonDeserializerRefreshToken(file));
                }
                else if(file.getName().startsWith("AT")) {
                    accessTokenList.add(jacksonDeserializerAccessToken(file));
                }
            }
        }

        //  to michael: add code here for database update

        int totalImports = ticketGrantingTicketList.size() + serviceTicketList.size() + refreshTokenList.size() + accessTokenList.size();
        return totalImports + " files imported";

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
    private void jacksonSerializerTicketGrantingTicket(TicketGrantingTicketImpl ticket, Settings settings) {
        ObjectMapper mapper = initMapper();
        File jsonFile = initFile((String)(settings.getSerializationPath() + File.separator + ticket.getId() + ".json"));
        String jsonStr = tweakJson(mapper.writeValueAsString(ticket));
        jsonFile.write(jsonStr);
    }

    /**
     *
     * @param ticket
     * @param settings
     */
    private void jacksonSerializerServiceTicket(ServiceTicketImpl ticket, Settings settings) {
        ObjectMapper mapper = initMapper();
        File jsonFile = initFile((String)(settings.getSerializationPath() + File.separator + ticket.getId() + ".json"));
        String jsonStr = tweakJson(mapper.writeValueAsString(ticket));
        jsonFile.write(jsonStr);
    }

    /**
     *
     * @param ticket
     * @param settings
     */
    private void jacksonSerializerRefreshToken(RefreshTokenImpl ticket, Settings settings) {
        ObjectMapper mapper = initMapper();
        File jsonFile = initFile((String)(settings.getSerializationPath() + File.separator + ticket.getId() + ".json"));
        String jsonStr = tweakJson(mapper.writeValueAsString(ticket));
        jsonFile.write(jsonStr);
    }

    /**
     *
     * @param ticket
     * @param settings
     */
    private void jacksonSerializerAccessToken(AccessTokenImpl ticket, Settings settings) {
        ObjectMapper mapper = initMapper();
        File jsonFile = initFile((String)(settings.getSerializationPath() + File.separator + ticket.getId() + ".json"));
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
        //  tweak 1:
        //  java.util.Collections$UnmodifiedMap does not have a deserializable constructor,
        //  and the above issue cannot be solved by using Jackson MixIn/Annotation,
        //  so we modify the class to its private one: java.util.HashMap.
        return jsonStr.replaceAll("java.util.Collections" + Matcher.quoteReplacement("\$") + "UnmodifiableMap", "java.util.HashMap");
        //  tweak 2:
        //  to michael: add tweak 2 here for migration
    }

}



public class Settings {

    public Settings() {
        File file;
        try {
            file = new File("./serialization-cache");
            if (!file.exists()) {
                file.mkdir();
            }
        }
        catch (IOException e) {
            System.err.println("IO Exception: cannot initiate working directory");
            throw e;
        }
        this.serializationPath = file.getCanonicalPath();
    }

    private serializationPath;

    public getSerializationPath() {
        return this.serializationPath;
    }

    public cleanCache() {
        try {
            FileUtils.cleanDirectory( new File(this.serializationPath));
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

