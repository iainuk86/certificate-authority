package net.majatech.ca;

import net.majatech.ca.authority.certificate.DistinguishedName;
import net.majatech.ca.authority.signing.CertificateSigningRequest;
import net.majatech.ca.controller.api.model.CsrForm;
import org.springframework.stereotype.Component;

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
}
