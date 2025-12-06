package com.openbrewerydb.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Brewery {
    private final String id;
    private final String name;
    private final String breweryType;
    private final String city;

    @JsonAlias("state")
    private final String stateProvince;
    private final String state;
    private final String country;
    private final String postalCode;

    @JsonAlias("address_1")
    private final String street;
    private final String address1;
    private final String address2;
    private final String address3;
    private final String phone;
    private final String websiteUrl;
    private final Double longitude;
    private final Double latitude;

    @JsonCreator
    public Brewery(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("brewery_type") String breweryType,
            @JsonProperty("city") String city,
            @JsonProperty("state_province") String stateProvince,
            @JsonProperty("state") String state,
            @JsonProperty("country") String country,
            @JsonProperty("postal_code") String postalCode,
            @JsonProperty("street") String street,
            @JsonProperty("address_1") String address1,
            @JsonProperty("address_2") String address2,
            @JsonProperty("address_3") String address3,
            @JsonProperty("phone") String phone,
            @JsonProperty("website_url") String websiteUrl,
            @JsonProperty("longitude") Double longitude,
            @JsonProperty("latitude") Double latitude
    ) {
        this.id = id;
        this.name = name;
        this.breweryType = breweryType;
        this.city = city;
        this.stateProvince = stateProvince;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
        this.street = street;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.phone = phone;
        this.websiteUrl = websiteUrl;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
