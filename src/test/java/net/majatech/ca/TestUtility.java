package net.majatech.ca;

import net.majatech.ca.authority.certificate.DistinguishedName;
import net.majatech.ca.authority.signing.CertificateSigningRequest;
import net.majatech.ca.controller.api.model.CsrForm;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class TestUtility {

    public CsrForm getTestCsrForm(String commonName) {
        CsrForm csrForm = new CsrForm();
        csrForm.setCommonName(commonName);
        csrForm.setLocality("Default Locality");
        csrForm.setState("Default State");
        csrForm.setCountry("Default Country");
        csrForm.setOrganization("Default Org");
        csrForm.setOrganizationalUnit("Default Org Unit");

        return csrForm;
    }

    public CsrForm getDefaultTestCsrForm() {
        return getTestCsrForm("default-cn");
    }

    public CertificateSigningRequest getDefaultTestCsr() {
        return CertificateSigningRequest.using(getDefaultTestDn(getDefaultTestCsrForm()));
    }

    public DistinguishedName getDefaultTestDn(CsrForm csrForm) {
        return DistinguishedName.newBuilder()
                .setCommonName(csrForm.getCommonName())
                .setLocality(csrForm.getLocality())
                .setState(csrForm.getState())
                .setCountry(csrForm.getCountry())
                .setOrganization(csrForm.getOrganization())
                .setOrganizationalUnit(csrForm.getOrganizationalUnit())
                .build();
    }

    public void assertDistinguishedNamesAreEqual(X500Name x500Name, DistinguishedName dn) {
        assertThat(dnFieldValue(x500Name, BCStyle.CN)).isEqualTo(dn.getCommonName());
        assertThat(dnFieldValue(x500Name, BCStyle.L)).isEqualTo(dn.getLocality());
        assertThat(dnFieldValue(x500Name, BCStyle.ST)).isEqualTo(dn.getState());
        assertThat(dnFieldValue(x500Name, BCStyle.C)).isEqualTo(dn.getCountry());
        assertThat(dnFieldValue(x500Name, BCStyle.O)).isEqualTo(dn.getOrganization());
        assertThat(dnFieldValue(x500Name, BCStyle.OU)).isEqualTo(dn.getOrganizationalUnit());
    }

    private String dnFieldValue(X500Name x500Name, ASN1ObjectIdentifier oid) {
        return IETFUtils.valueToString(x500Name.getRDNs(oid)[0].getFirst().getValue());
    }
}
