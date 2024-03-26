package net.majatech.ca.authority.certificate;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Class to represent the Distinguished Name for either a certificate subject or issuer
 */
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

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Convert the DN fields to a BouncyCastle X500Name object
     * <br><br>
     * BouncyCastle reverses the order in which the DN fields are configured. So as we want the CommonName to be
     * displayed first we need to add that field to the builder last, as shown below
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
     * Builder class for DistinguishedName. Builder pattern chosen as all DN fields are technically optional
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DistinguishedName that = (DistinguishedName) o;
        return Objects.equals(commonName, that.commonName)
                && Objects.equals(locality, that.locality)
                && Objects.equals(state, that.state)
                && Objects.equals(country, that.country)
                && Objects.equals(organization, that.organization)
                && Objects.equals(organizationalUnit, that.organizationalUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commonName, locality, state, country, organization, organizationalUnit);
    }
}
