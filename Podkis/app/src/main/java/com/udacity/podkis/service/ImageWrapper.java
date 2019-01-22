package com.udacity.podkis.service;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

import static com.udacity.podkis.service.ImageWrapper.SERIALIZED_PODCAST_EPISODE_IMAGE;

@Root(name = SERIALIZED_PODCAST_EPISODE_IMAGE, strict = false)
public class ImageWrapper implements Serializable {

    static final String SERIALIZED_PODCAST_EPISODE_IMAGE = "image";
    private static final String SERIALIZED_PODCAST_IMAGE_URL_ATTRIBUTE = "href";
    private static final String SERIALIZED_PODCAST_IMAGE_URL_ELEMENT = "url";

    @Attribute(name = SERIALIZED_PODCAST_IMAGE_URL_ATTRIBUTE, required = false)
    public String imageUrl;


    @Element(name = SERIALIZED_PODCAST_IMAGE_URL_ELEMENT, required = false)
    public String url;
}
