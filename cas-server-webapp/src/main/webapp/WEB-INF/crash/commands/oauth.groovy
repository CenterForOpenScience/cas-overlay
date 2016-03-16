import org.crsh.cli.Command
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

        StringBuilder builder = new StringBuilder();

        // De/Serialization Test for TicketGrantingTicketImpl

        for (TicketGrantingTicketImpl ticket: ticketGrantingTicketList) {
            System.out.println(this.JacksonSerializationTestTicketGrantingTicket(ticket));
        }
        builder.append(ticketGrantingTicketList.toString());

        //  De/Serialization Test for ServiceTicketImpl instance

        for (ServiceTicketImpl ticket: serviceTicketList) {
            System.out.println(this.JacksonSerializationTestServiceTicket(ticket));
        }
        builder.append(serviceTicketList.toString());

        //  De/Serialization Test for RefreshTokenImpl instance

        for (RefreshTokenImpl token : refreshTokenList) {
            System.out.println(this.JacksonSerializationTestRefreshToken(token));
        }
        builder.append(refreshTokenList.toString());

        //  De/Serialization Test for AccessTokenImpl instance
        for(AccessTokenImpl token : accessTokenList) {
            System.out.println(this.JacksonSerializationTestAccessToken(token));
        }
        builder.append(accessTokenList.toString());

        return builder.toString();
    }


    @Command
    public String IMPORT() {
        return "This Command Has Not Been Implemented";
    }


    @Command
    public String CLEAN() {
        return "This Command Has Not Been Implemented";
    }


    private String JacksonSerializationTestRefreshToken(RefreshTokenImpl token) {

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

        File jsonFile, jsonFileProcessed, jsonFileNew;
        try {
            jsonFile = new File("token-" + token.getId() + ".json");
            jsonFileProcessed = new File("token-" + token.getId() + "-processed.json");
            jsonFileNew = new File("token-" + token.getId() + "-new.json");
        }
        catch (Exception e) {
            System.out.println("Fail to open file for writing");
            return false;
        }


        String jsonStr = mapper.writeValueAsString(token);
        //  java.util.Collections$UnmodifiedMap does not have a deserializable constructor,
        //  and the above issue cannot be solved by using Jackson MixIn/Annotation,
        //  so we modify the class to its private one: java.util.HashMap.
        String processedStr = jsonStr.replaceAll("java.util.Collections" + Matcher.quoteReplacement("\$") + "UnmodifiableMap", "java.util.HashMap");
        jsonFileProcessed.write(processedStr);
        RefreshTokenImpl newToken = mapper.readValue(jsonFileProcessed, RefreshTokenImpl.class);
        mapper.writeValue(jsonFile, token);
        mapper.writeValue(jsonFileNew, newToken);

        //  will return false even all fields and super fields are equal,
        //  not sure why, same with RefreshToken
        return token.equals(newToken);
    }


    private String JacksonSerializationTestAccessToken(AccessTokenImpl token) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JacksonMixinModule());
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        File jsonFile, jsonFileProcessed, jsonFileNew;
        try {
            jsonFile = new File("token-" + token.getId() + ".json");
            jsonFileProcessed = new File("token-" + token.getId() + "-processed.json");
            jsonFileNew = new File("token-" + token.getId() + "-new.json");
        }
        catch (Exception e) {
            System.out.println("Fail to open file for writing");
            return false;
        }

        String jsonStr = mapper.writeValueAsString(token);
        String processedStr = jsonStr.replaceAll("java.util.Collections" + Matcher.quoteReplacement("\$") + "UnmodifiableMap", "java.util.HashMap");
        jsonFileProcessed.write(processedStr);
        AccessTokenImpl newToken = mapper.readValue(jsonFileProcessed, AccessTokenImpl.class);
        mapper.writeValue(jsonFile, token);
        mapper.writeValue(jsonFileNew, newToken);

        //  will return false even all fields and super fields are equal,
        //  not sure why, same with RefreshToken
        return token.equals(newToken);
    }



    private String JacksonSerializationTestTicketGrantingTicket(TicketGrantingTicketImpl ticket) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JacksonMixinModule());
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        File jsonFile;
        try {
            jsonFile = new File("ticket-" + ticket.getId() + ".json");
            jsonFileProcessed = new File("ticket-" + ticket.getId() + "-processed.json");
            jsonFileNew = new File("ticket-" + ticket.getId() + "-new.json");
        }
        catch (Exception e) {
            System.out.println("Fail to open file for writing");
            return false;
        }

        String jsonStr = mapper.writeValueAsString(ticket);
        String processedStr = jsonStr.replaceAll("java.util.Collections" + Matcher.quoteReplacement("\$") + "UnmodifiableMap", "java.util.HashMap");
        jsonFileProcessed.write(processedStr);
        TicketGrantingTicketImpl new_ticket = mapper.readValue(jsonFileProcessed, TicketGrantingTicketImpl.class);
        mapper.writeValue(jsonFile, ticket);
        mapper.writeValue(jsonFileNew, newTicket);

        return ticket.equals(new_ticket);
    }


    private String JacksonSerializationTestServiceTicket(ServiceTicketImpl ticket) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JacksonMixinModule());
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        File jsonFile, jsonProcessedFile, jsonNewFile;
        try {
            jsonFile = new File("ticket-" + ticket.getId() + ".json");
            jsonProcessedFile = new File("ticket-" + ticket.getId() + "-processed.json");
            jsonNewFile = new File("ticket-" + ticket.getId() + "-new.json");
        }
        catch (Exception e) {
            System.out.println("Fail to open file for writing");
            return false;
        }

        mapper.writeValue(jsonFile, ticket);
        String jsonStr = mapper.writeValueAsString(ticket);
        String processedStr = jsonStr.replaceAll("java.util.Collections" + Matcher.quoteReplacement("\$") + "UnmodifiableMap", "java.util.HashMap");
        jsonProcessedFile.write(processedStr);

        ServiceTicketImpl newTicket = mapper.readValue(jsonProcessedFile, ServiceTicketImpl.class);
        mapper.writeValue(jsonNewFile, newTicket);

        return ticket.equals(newTicket);
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

