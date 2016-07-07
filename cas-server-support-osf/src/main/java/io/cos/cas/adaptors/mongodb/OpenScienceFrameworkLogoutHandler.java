package io.cos.cas.adaptors.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.validation.constraints.NotNull;

public class OpenScienceFrameworkLogoutHandler {

    @NotNull
    private static MongoOperations MongoTemplate;

    public void setMongoTemplate(MongoOperations mongoTemplate) {
        MongoTemplate = mongoTemplate;
    }

    @Document(collection="node")
    private static class OpenScienceFrameworkInstitution {
        @Id
        private String id;
        @Field("institution_id")
        private String institutionId;
        @Field("institution_auth_url")
        private String institutionLoginUrl;
        @Field("institution_logout_url")
        private String institutionlogoutUrl;
        @Field("institution_domains")
        private String institutionDomains;
        @Field("title")
        private String institutionName;
        @Field("institution_logo_name")
        private String institutionLogoName;
        @Field("institution_email_domains")
        private String institutionEmailDomains;
        @Field("institution_banner_name")
        private String instituionBannerName;
        @Field("description")
        private String description;
        @Field("is_deleted")
        private Boolean isDeleted;

        public String getId() { return id; }

        public String getInstitutionId() { return institutionId; }

        public String getInstitutionLoginUrl() { return institutionLoginUrl; }

        public String getInstitutionlogoutUrl() { return institutionlogoutUrl; }

        public String getInstitutionDomains() { return institutionDomains; }

        public String getInstitutionName() { return institutionName; }

        public String getInstitutionLogoName() { return institutionLogoName; }

        public String getInstitutionEmailDomains() { return institutionEmailDomains; }

        public String getInstituionBannerName() { return instituionBannerName; }

        public String getDescription() { return description; }

        public Boolean isDeleted() { return isDeleted; }

        public void setId(String id) { this.id = id; }

        public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }

        public void setInstitutionLoginUrl(String institutionLoginUrl) { this.institutionLoginUrl = institutionLoginUrl; }

        public void setInstitutionlogoutUrl(String institutionlogoutUrl) { this.institutionlogoutUrl = institutionlogoutUrl; }

        public void setInstitutionDomains(String institutionDomains) { this.institutionDomains = institutionDomains; }

        public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

        public void setInstitutionLogoName(String institutionLogoName) { this.institutionLogoName = institutionLogoName; }

        public void setInstitutionEmailDomains(String institutionEmailDomains) { this.institutionEmailDomains = institutionEmailDomains; }

        public void setInstituionBannerName(String instituionBannerName) { this.instituionBannerName = instituionBannerName; }

        public void setDescription(String description) { this.description = description; }

        public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    }

    public static String FindInstitutionLogoutUrlById(String institutionId) {
        OpenScienceFrameworkInstitution institution = MongoTemplate.findOne(
            new Query(Criteria
                .where("institution_id").is(institutionId)
                .and("isDeleted").is(Boolean.FALSE)
            ), OpenScienceFrameworkInstitution.class
        );
        return institution.getInstitutionlogoutUrl();
    }
}
