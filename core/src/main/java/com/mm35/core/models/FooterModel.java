package com.mm35.core.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.List;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class FooterModel {

    @ValueMapValue
    private String footerText;

    public String getFooterText() {
        return footerText;
    }

    @ValueMapValue
    private String companyLogo;

    public String getCompanyLogo() {
        return companyLogo;
    }

    @ChildResource
    private List<LegalLinks> legalLinks;

    public List<LegalLinks> getLegalLinks() {
        return legalLinks;
    }

    @ChildResource
    private List<HelpNavigation> helpNavigation;



    public List<HelpNavigation> getHelpNavigation() {
        return helpNavigation;
    }

    @ChildResource
    private List<RedesSociales> redesSociales;



    public List<RedesSociales> getRedesSociales() {
        return redesSociales;
    }


    // clase para definir el contenido de LegalLinks
    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class LegalLinks{
        @ValueMapValue
        private String legalLinkText;

        public String getLegalLinkText(){
            return legalLinkText;
        }
        @ValueMapValue
        private String legalLink;

        public String getLegalLink(){return this.legalLink;}
    }
    // clase para definir el contenido de HelpNavigation
    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class HelpNavigation{
        @ValueMapValue
        private String helpNavigationText;

        public String getHelpNavigationText(){
            return helpNavigationText;
        }
        @ValueMapValue
        private String helpNavigationLink;

        public String getHelpNavigationLink(){return this.helpNavigationLink;}
    }

    // clase para definir el contenido de redes sociales
    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class RedesSociales{
        @ValueMapValue
        private String fileReference;

        public String getFileReference() {
            return fileReference;
        }
        @ValueMapValue
        private String link;

        public String getLink() { return this.link; }

    }
}

