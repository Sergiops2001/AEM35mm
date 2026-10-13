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
public class CarouselBannerModel {


    @ChildResource
    private List<CarouselBannerContent> carouselBannerContent;



    public List<CarouselBannerContent> getCarouselBannerContent() {
        return carouselBannerContent;
    }


    // clase para definir el contenido de carouselbanner
    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class CarouselBannerContent{
        @ValueMapValue
        private String fileReference;

        public String getFileReference() {
            return fileReference;
        }
        @ValueMapValue
        private String carouselText;

        public String getCarouselText() { return this.carouselText; }

    }
}

