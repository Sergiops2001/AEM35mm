package com.mm35.core.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.List;

@Model(adaptables = { SlingHttpServletRequest.class,
        Resource.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)

public class BannerModel {

    @ValueMapValue
    private String bannerTitle;

    public String getBannerTitle() {
        return bannerTitle;
    }

    @ValueMapValue
    private String bannerDescription;

    public String getbannerDescription() {
        return bannerDescription;
    }


}
