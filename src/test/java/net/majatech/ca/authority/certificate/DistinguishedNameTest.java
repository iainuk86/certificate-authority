package net.majatech.ca.authority.certificate;

import org.junit.jupiter.api.Test;
import org.thymeleaf.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class DistinguishedNameTest {

    @Test
    public void testBuilder() {
        DistinguishedName dn = DistinguishedName.newBuilder()
                .setCommonName("New CN")
                .setLocality("Some Crater")
                .setState("Dark Side")
                .setCountry("The Moon")
                .setOrganization("NASA")
                .setOrganizationalUnit("Apollo")
                .build();

        assertThat(dn.getCommonName()).isEqualTo("New CN");
        assertThat(dn.getLocality()).isEqualTo("Some Crater");
        assertThat(dn.getState()).isEqualTo("Dark Side");
        assertThat(dn.getCountry()).isEqualTo("The Moon");
        assertThat(dn.getOrganization()).isEqualTo("NASA");
        assertThat(dn.getOrganizationalUnit()).isEqualTo("Apollo");
    }

    @Test
    public void testBuildingWithPartialFields() {
        DistinguishedName dn = DistinguishedName.newBuilder()
                .setState("Dark Side")
                .setCountry("The Moon")
                .build();

        assertThat(dn.getState()).isEqualTo("Dark Side");
        assertThat(dn.getCountry()).isEqualTo("The Moon");

        assertThat(dn.getCommonName()).isNull();
        assertThat(dn.getLocality()).isNull();
        assertThat(dn.getOrganization()).isNull();
        assertThat(dn.getOrganizationalUnit()).isNull();
    }

    @Test
    public void testAllFieldsAreOptional() {
        DistinguishedName dn = DistinguishedName.newBuilder().build();

        assertThat(dn.getCommonName()).isNull();
        assertThat(dn.getLocality()).isNull();
        assertThat(dn.getState()).isNull();
        assertThat(dn.getCountry()).isNull();
        assertThat(dn.getOrganization()).isNull();
        assertThat(dn.getOrganizationalUnit()).isNull();
    }

    @Test
    public void testEquals_NamesAreTheSame() {
        String cn = StringUtils.randomAlphanumeric(16);
        String l = StringUtils.randomAlphanumeric(16);
        String st = StringUtils.randomAlphanumeric(16);
        String c = StringUtils.randomAlphanumeric(16);
        String o = StringUtils.randomAlphanumeric(16);
        String ou = StringUtils.randomAlphanumeric(16);

        DistinguishedName thisDn = DistinguishedName.newBuilder()
                .setCommonName(cn)
                .setLocality(l)
                .setState(st)
                .setCountry(c)
                .setOrganization(o)
                .setOrganizationalUnit(ou)
                .build();

        DistinguishedName thatDn = DistinguishedName.newBuilder()
                .setCommonName(cn)
                .setLocality(l)
                .setState(st)
                .setCountry(c)
                .setOrganization(o)
                .setOrganizationalUnit(ou)
                .build();

        assertThat(thisDn).isEqualTo(thatDn);
    }

    @Test
    public void testEquals_NamesAreDifferent() {
        DistinguishedName thisDn = DistinguishedName.newBuilder()
                .setCommonName("New CN")
                .setLocality("Some Crater")
                .setState("Dark Side")
                .setCountry("The Moon")
                .setOrganization("NASA")
                .setOrganizationalUnit("Apollo")
                .build();

        // Create a DN where the CN is different by 1 character
        DistinguishedName thatDn = DistinguishedName.newBuilder()
                .setCommonName("New C")
                .setLocality("Some Crater")
                .setState("Dark Side")
                .setCountry("The Moon")
                .setOrganization("NASA")
                .setOrganizationalUnit("Apollo")
                .build();

        assertThat(thisDn).isNotEqualTo(thatDn);
    }

    @Test
    public void testHashCode_NamesAreTheSame() {
        String cn = StringUtils.randomAlphanumeric(16);
        String l = StringUtils.randomAlphanumeric(16);
        String st = StringUtils.randomAlphanumeric(16);
        String c = StringUtils.randomAlphanumeric(16);
        String o = StringUtils.randomAlphanumeric(16);
        String ou = StringUtils.randomAlphanumeric(16);

        DistinguishedName thisDn = DistinguishedName.newBuilder()
                .setCommonName(cn)
                .setLocality(l)
                .setState(st)
                .setCountry(c)
                .setOrganization(o)
                .setOrganizationalUnit(ou)
                .build();

        DistinguishedName thatDn = DistinguishedName.newBuilder()
                .setCommonName(cn)
                .setLocality(l)
                .setState(st)
                .setCountry(c)
                .setOrganization(o)
                .setOrganizationalUnit(ou)
                .build();

        assertThat(thisDn.hashCode()).isEqualTo(thatDn.hashCode());
    }

    @Test
    public void testHashCode_NamesAreDifferent() {
        DistinguishedName thisDn = DistinguishedName.newBuilder()
                .setCommonName("New CN")
                .setLocality("Some Crater")
                .setState("Dark Side")
                .setCountry("The Moon")
                .setOrganization("NASA")
                .setOrganizationalUnit("Apollo")
                .build();

        // Create a DN where the CN is different by 1 character
        DistinguishedName thatDn = DistinguishedName.newBuilder()
                .setCommonName("New C")
                .setLocality("Some Crater")
                .setState("Dark Side")
                .setCountry("The Moon")
                .setOrganization("NASA")
                .setOrganizationalUnit("Apollo")
                .build();

        assertThat(thisDn.hashCode()).isNotEqualTo(thatDn.hashCode());
    }
}
