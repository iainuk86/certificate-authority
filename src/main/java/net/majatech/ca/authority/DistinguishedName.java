package net.majatech.ca.authority;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.util.StringUtils;

public class DistinguishedName {
    private final String commonName;
    private final String locality;
    private final String state;
    private final String country;
    private final String organization;
    private final String organizationalUnit;

    // Created using the DistinguishedName.Builder defined below
    private DistinguishedName(String commonName, String locality, String state, String country, String organization,
                              String organizationalUnit) {
        this.commonName = commonName;
        this.locality = locality;
        this.state = state;
        this.country = country;
        this.organization = organization;
        this.organizationalUnit = organizationalUnit;
    }

    /**
     * Convert the DN fields to a BouncyCastle X500Name object
     */
    public X500Name toX500Name() {
        X500NameBuilder nameBuilder = new X500NameBuilder();

        if (StringUtils.hasLength(organizationalUnit)) {
            nameBuilder.addRDN(BCStyle.OU, organizationalUnit);
        }

        if (StringUtils.hasLength(organization)) {
            nameBuilder.addRDN(BCStyle.O, organization);
        }

        if (StringUtils.hasLength(country)) {
            nameBuilder.addRDN(BCStyle.C, country);
        }

        if (StringUtils.hasLength(state)) {
            nameBuilder.addRDN(BCStyle.ST, state);
        }

        if (StringUtils.hasLength(locality)) {
            nameBuilder.addRDN(BCStyle.L, locality);
        }

        if (StringUtils.hasLength(commonName)) {
            nameBuilder.addRDN(BCStyle.CN, commonName);
        }

        return nameBuilder.build();
    }

    /**
     * Entrypoint to create instances of DistinguishedName from the rest of the package.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    public String getCommonName() {
        return commonName;
    }

    public String getLocality() {
        return locality;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getOrganization() {
        return organization;
    }

    public String getOrganizationalUnit() {
        return organizationalUnit;
    }

    /**
     * Builder class for DistinguishedName. Builder pattern chosen as all DN fields are technically optional.
     */
    public static class Builder {
        private String commonName;
        private String locality;
        private String state;
        private String country;
        private String organization;
        private String organizationalUnit;

        public Builder setCommonName(String commonName) {
            this.commonName = commonName;
            return this;
        }

        public Builder setLocality(String locality) {
            this.locality = locality;
            return this;
        }

        public Builder setState(String state) {
            this.state = state;
            return this;
        }

        public Builder setCountry(String country) {
            this.country = country;
            return this;
        }

        public Builder setOrganization(String organization) {
            this.organization = organization;
            return this;
        }

        public Builder setOrganizationalUnit(String organizationalUnit) {
            this.organizationalUnit = organizationalUnit;
            return this;
        }

        public DistinguishedName build() {
            return new DistinguishedName(commonName, locality, state, country, organization, organizationalUnit);
        }
    }
}
